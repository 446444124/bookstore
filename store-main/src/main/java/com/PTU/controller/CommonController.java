package com.PTU.controller;
import com.PTU.constant.MessageConstant;
import com.PTU.result.Result;
import com.PTU.utils.AliOssUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/user/common")
@Api(tags = "通用接口")
@Slf4j
public class CommonController {
    @Autowired
    private AliOssUtil aliOssUtil;

    @PostMapping("/upload")
    @ApiOperation("文件上传")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        log.info("文件上传：{}", file);
        try {
            if (file == null || file.isEmpty()) {
                return Result.error(MessageConstant.UPLOAD_FAILED);
            }
            String originalFilename = file.getOriginalFilename();
            String extension = ".jpg";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String objectName = UUID.randomUUID().toString() + extension;
            String path = aliOssUtil.upload(file.getBytes(), objectName);
            return Result.success(path);
        } catch (Exception e) {
            log.error("文件上传失败：{}", e.getMessage(), e);
        }
        return Result.error(MessageConstant.UPLOAD_FAILED);
    }
}
