package com.PTU.utils;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;

@Data
@AllArgsConstructor
@Slf4j
public class AliOssUtil {

    private String endpoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;

    /**
     * 文件上传
     *
     * @param bytes
     * @param objectName
     * @return
     */
    public String upload(byte[] bytes, String objectName) throws ClientException, OSSException {
        String clientEndpoint = normalizeClientEndpoint(endpoint);
        OSS ossClient = new OSSClientBuilder().build(clientEndpoint, accessKeyId, accessKeySecret);
        try {
            ossClient.putObject(bucketName, objectName, new ByteArrayInputStream(bytes));
        } finally {
            ossClient.shutdown();
        }

        // 虚拟主机风格公网访问：https://BucketName.EndpointHost/ObjectName
        // endpoint 若配置为 https://oss-xxx.aliyuncs.com，不能直接拼进 host，需先去掉协议
        String hostOnly = stripScheme(endpoint);
        String publicUrl = "https://" + bucketName + "." + hostOnly + "/" + objectName;
        log.info("文件上传到: {}", publicUrl);
        return publicUrl;
    }

    /** 供 SDK 使用：无协议时补全 https:// */
    private static String normalizeClientEndpoint(String ep) {
        if (ep == null || ep.trim().isEmpty()) {
            return ep;
        }
        String s = ep.trim();
        if (s.startsWith("http://") || s.startsWith("https://")) {
            return s;
        }
        return "https://" + s;
    }

    /** 拼浏览器访问 URL 时只保留 oss-cn-xxx.aliyuncs.com 这一段 */
    private static String stripScheme(String ep) {
        if (ep == null) {
            return "";
        }
        String s = ep.trim();
        if (s.startsWith("https://")) {
            return s.substring(8);
        }
        if (s.startsWith("http://")) {
            return s.substring(7);
        }
        return s;
    }
}
