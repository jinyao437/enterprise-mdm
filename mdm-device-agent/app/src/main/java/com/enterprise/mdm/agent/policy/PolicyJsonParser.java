package com.enterprise.mdm.agent.policy;

/**
 * 策略 JSON 解析（纯 Java，不依赖 Android SDK，可单元测试）。
 *
 * <p>避免引入额外 JSON 库，用轻量字符串解析覆盖 agent 需要的几个键。
 */
public final class PolicyJsonParser {

    private PolicyJsonParser() {
    }

    /** 解析 passwordMinLength，缺省返回 6 */
    public static int passwordMinLength(String json) {
        int value = intValue(json, "passwordMinLength");
        return value < 0 ? 6 : value;
    }

    /** 解析 cameraDisabled，缺省 false */
    public static boolean cameraDisabled(String json) {
        return boolValue(json, "cameraDisabled");
    }

    /** 解析一个整型键，不存在返回 -1 */
    public static int intValue(String json, String key) {
        if (json == null) {
            return -1;
        }
        String quoted = "\"" + key + "\"";
        int idx = json.indexOf(quoted);
        if (idx < 0) {
            return -1;
        }
        int colon = json.indexOf(':', idx + quoted.length());
        if (colon < 0) {
            return -1;
        }
        int start = colon + 1;
        while (start < json.length() && Character.isWhitespace(json.charAt(start))) {
            start++;
        }
        int end = start;
        if (end < json.length() && json.charAt(end) == '-') {
            end++;
        }
        while (end < json.length() && Character.isDigit(json.charAt(end))) {
            end++;
        }
        if (end == start || (end == start + 1 && json.charAt(start) == '-')) {
            return -1;
        }
        try {
            return Integer.parseInt(json.substring(start, end));
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /** 解析一个布尔键，不存在返回 false */
    public static boolean boolValue(String json, String key) {
        if (json == null) {
            return false;
        }
        String quoted = "\"" + key + "\"";
        int idx = json.indexOf(quoted);
        if (idx < 0) {
            return false;
        }
        int colon = json.indexOf(':', idx + quoted.length());
        if (colon < 0) {
            return false;
        }
        int rest = colon + 1;
        while (rest < json.length() && Character.isWhitespace(json.charAt(rest))) {
            rest++;
        }
        return json.regionMatches(rest, "true", 0, 4);
    }
}
