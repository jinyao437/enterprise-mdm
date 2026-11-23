# 企业 MDM 多租户平台 — 部署与验证指南

> 本文档记录的是**真实跑通过**的步骤：2026-11-23 在 Debian 12 上用 JDK 17 + MariaDB 10.11 + Redis 7 启动后端，并通过真实 HTTP 验证了登录、设备 CRUD、策略、租户隔离、设备端专用 API。
> 未实测的部分（野火 IM、移动端、桌面端）在文末「边界」中如实说明。

## 0. 前置依赖

| 组件 | 版本 | 说明 |
|---|---|---|
| JDK | 17 | 实测用 17 编译芋道 `2026.10-jdk8-SNAPSHOT` 全工程通过 |
| Maven | 3.9+ | 编译后端 |
| MySQL / MariaDB | MySQL 8 或 MariaDB 10.11 | 实测用 MariaDB 10.11 |
| Redis | 7.x | 实测用 Redis 7.0，无密码 |
| Node / pnpm | Node 20+ / pnpm 10 | 仅前端需要 |

## 1. 启动数据库

**方式 A：Docker Compose（推荐）**

```bash
cd yudao-base/script/docker-compose
# 按同目录 README 建 .env：设置 MYSQL_ROOT_PASSWORD / MYSQL_PASSWORD / REDIS_PASSWORD
docker compose up -d     # MySQL 8.4 + Redis 7.4
```

**方式 B：系统包（无 Docker 的环境）**

```bash
apt-get install -y mariadb-server redis-server
# 初始化数据目录（容器里 /var/lib/mysql 不可写时用自定义目录）
mariadb-install-db --datadir=<你的数据目录> --auth-root-authentication-method=normal --user=root
mariadbd --datadir=<你的数据目录> --socket=/tmp/mysql.sock --port=3306 --bind-address=127.0.0.1 --user=root &
redis-server --port 6379 --bind 127.0.0.1 --save '' --appendonly no &
mysql --socket=/tmp/mysql.sock -u root -e "ALTER USER 'root'@'localhost' IDENTIFIED BY '123456';"
```

> 坑：socket 路径不能超过 107 字符。工作区路径长时把 `--socket` 指到 `/tmp/mysql.sock`，数据目录仍可放任意位置。

## 2. 建库并导入 SQL

```bash
DB=ruoyi_vue_pro_mdm
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS $DB DEFAULT CHARACTER SET utf8mb4;"

# 底座（先）
mysql -u root -p $DB < yudao-base/sql/mysql/ruoyi-vue-pro.sql
mysql -u root -p $DB < yudao-base/sql/mysql/quartz.sql

# MDM 增量建表（7 个）
for f in yudao-base/enterprise-module-mdm/src/test/java/cn/enterprise/mdm/sql/*.sql; do
  mysql -u root -p $DB < "$f"
done

# 菜单权限（3 个）
for f in admin-ui/sql/mdm_menu*.sql; do
  mysql -u root -p $DB < "$f"
done
```

导完应约 72 张表。

## 3. 配置后端

编辑 `yudao-base/yudao-server/src/main/resources/application-local.yaml`：

- `spring.datasource.dynamic.datasource.master.url`：库名改成你的 `$DB`
- `username` / `password`：改成你的数据库账号（默认配置是 root / 123456）
- `spring.data.redis.host/port/password`：Redis 地址（实测无密码，配置里 password 行保持注释）

`application.yaml` 里 `spring.profiles.active: local` 默认已激活 local，服务器端口 `48080`。

## 4. 编译并启动后端

```bash
cd yudao-base
mvn -pl yudao-server -am clean package -DskipTests
java -jar yudao-server/target/yudao-server.jar
```

首次全量编译约 4~5 分钟（8258 个 java）。看到日志 `Started YudaoServerApplication in ... seconds` 且 48080 监听即启动成功。

> 注意：启动类 `YudaoServerApplication` 已加 `cn.enterprise.mdm` 扫描，`enterprise-module-mdm` 另有 `MdmMybatisConfiguration` 扫自己的 Mapper。这两处缺一个，MDM 接口就 404 或启动失败——都已修复并提交。

## 5. 验证（实测通过的 curl）

```bash
BASE=http://127.0.0.1:48080/admin-api

# 1) 登录拿 token（注意 tenant-id 走 header，账号密码走 JSON body）
TOKEN=$(curl -s -X POST "$BASE/system/auth/login" \
  -H 'tenant-id: 1' -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"admin123"}' \
  | python3 -c 'import json,sys;print(json.load(sys.stdin)["data"]["accessToken"])')

# 2) 注册设备
curl -s -X POST "$BASE/mdm/device/create" \
  -H 'tenant-id: 1' -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"identifier":"IMEI-001","name":"测试设备","platform":"android","osVersion":"Android 14","status":0}'

# 3) 分页查询（应返回 total>=1）
curl -s "$BASE/mdm/device/page?pageNo=1&pageSize=10" \
  -H 'tenant-id: 1' -H "Authorization: Bearer $TOKEN"

# 4) 租户隔离：拿租户 1 的 token 配 tenant-id=2，应返回 403
curl -s "$BASE/mdm/device/page?pageNo=1&pageSize=10" \
  -H 'tenant-id: 2' -H "Authorization: Bearer $TOKEN"

# 5) 生成设备令牌（管理员调用）
DTOK=$(curl -s -X POST "$BASE/mdm/device/token/create?id=1&ttlDays=30" \
  -H 'tenant-id: 1' -H "Authorization: Bearer $TOKEN" | python3 -c 'import json,sys;print(json.load(sys.stdin)["data"])')

# 6) 设备端专用 API：只用设备令牌（无需管理员 token）拉策略
curl -s http://127.0.0.1:48080/mdm/device-api/policies -H "X-Device-Token: $DTOK"

# 7) 无令牌访问设备端 API，应返回 HTTP 401
curl -s -o /dev/null -w '%{http_code}\n' http://127.0.0.1:48080/mdm/device-api/policies
```

实测结果：1~3、5~6 返回 `code=0`；4 返回 403「您无权访问该租户的数据」；7 返回 401。

## 6. 前端（仅构建实测，未跑 dev server）

```bash
# Web 管理后台
cd admin-ui && pnpm install && pnpm dev     # 配置 .env.local 的 VITE_API_URL 指向后端

# uni-app（H5）
cd yudao-uniapp && pnpm install && pnpm dev:h5
```

## 7. 边界与未实测项（如实说明）

- **野火 IM 未部署**：消息模块需要自建野火服务端；未配置时消息发不出去，代码里是明确报错而非伪造消息。
- **Android agent**：`mdm-device-agent/` 是原生工程，需 Android SDK 才能编译 APK，且有 OkHttp 真实调用但未连真后端跑过。Device Owner 激活需出厂预置 / QR provisioning / adb 三选一。
- **PC 桌面端**：`mdm-desktop/` 需 GUI 环境（缺 libnss3/gtk3 时起不来）；已用 Electron 33 打包配置，未在本机启动窗口。
- **前端 dev server**：`pnpm build:local`（Web）和 `pnpm build:h5`（uni-app）构建通过。本机 `pnpm dev` 启动时 Vite 8 进程进入 D 状态、无输出（容器 I/O 限制），未能起 dev server 做浏览器联调。但已验证前端 API 链路配置正确：axios `base_url = VITE_BASE_URL + VITE_API_URL = http://localhost:48080/admin-api`，MDM 页面调用 `/mdm/device/page` 拼成 `http://localhost:48080/admin-api/mdm/device/page`，实测 HTTP 200。在正常桌面环境 `pnpm dev` 应可用。
- **租户物理隔离**：PRD 里的独立库/独立 Schema 档位未实现，当前是逻辑隔离（tenant_id 行级）+ 底座租户插件，已实测 403 防越权。
- **本文档的数据库密码、库名**是本机实测值，部署到你自己的环境请按实际改。
