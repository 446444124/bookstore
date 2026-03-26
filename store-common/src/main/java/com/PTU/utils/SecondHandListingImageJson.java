package com.PTU.utils;

import com.alibaba.fastjson.JSON;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 二手书用户上传成色参考图：库内存 JSON 数组字符串，对外为 List&lt;String&gt; URL。
 */
public final class SecondHandListingImageJson {

    private static final int MAX_IMAGES = 5;

    private SecondHandListingImageJson() {
    }

    public static List<String> parse(String json) {
        if (json == null || json.trim().isEmpty()) {
            return Collections.emptyList();
        }
        String s = json.trim();
        // 字段被整体再包一层 JSON 字符串时，先解一层
        if (s.length() >= 2 && s.charAt(0) == '"' && s.charAt(s.length() - 1) == '"') {
            try {
                s = JSON.parseObject(s, String.class);
            } catch (Exception e) {
                return Collections.emptyList();
            }
            if (s == null || s.trim().isEmpty()) {
                return Collections.emptyList();
            }
            s = s.trim();
        }
        try {
            List<String> list = JSON.parseArray(s, String.class);
            if (list != null && !list.isEmpty()) {
                return list.stream().map(SecondHandListingImageJson::fixMalformedOssUrl).collect(Collectors.toList());
            }
        } catch (Exception ignored) {
            // 非 JSON 数组：可能是单条 URL、或历史手工写入
        }
        // 整条就是一个 http(s) URL
        if (s.startsWith("http://") || s.startsWith("https://")) {
            return Collections.singletonList(fixMalformedOssUrl(s));
        }
        return Collections.emptyList();
    }

    /**
     * 历史 bug：endpoint 带 https:// 时曾拼成 https://bucket.https://oss-xxx/...
     */
    static String fixMalformedOssUrl(String url) {
        if (url == null || url.isEmpty()) {
            return url;
        }
        String u = url.trim();
        if (u.contains(".https://")) {
            return u.replaceFirst("\\.https://", ".");
        }
        return u;
    }

    public static String toJson(List<String> urls) {
        if (urls == null || urls.isEmpty()) {
            return null;
        }
        List<String> cleaned = urls.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .limit(MAX_IMAGES)
                .collect(Collectors.toList());
        if (cleaned.isEmpty()) {
            return null;
        }
        return JSON.toJSONString(cleaned);
    }
}
