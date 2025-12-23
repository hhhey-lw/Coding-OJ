package com.longoj.top.domain.service.impl;

import cn.hutool.core.io.FileUtil;
import com.aliyun.oss.OSS;
import com.aliyun.oss.model.PutObjectRequest;
import com.google.common.collect.Sets;
import com.longoj.top.domain.entity.User;
import com.longoj.top.domain.entity.enums.FileUploadBizEnum;
import com.longoj.top.domain.service.FileService;
import com.longoj.top.infrastructure.config.OssClientConfig;
import com.longoj.top.infrastructure.exception.BusinessException;
import com.longoj.top.infrastructure.exception.ErrorCode;
import com.longoj.top.infrastructure.utils.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.util.Set;

@Slf4j
@Service
public class FileServiceImpl implements FileService {

    public static final long UPLOAD_FILE_MAX_SIZE = 10 * 1024 * 1024L;
    public static final Set<String> AVATAR_FILE_TYPE = Sets.newHashSet("jpeg", "jpg", "png");

    @Resource
    private OssClientConfig ossClientConfig;

    @Resource
    private OSS ossClient;

    @Override
    public String uploadFile(MultipartFile multipartFile, FileUploadBizEnum fileUploadBizEnum) {
        // 1. 校验文件
        validFile(multipartFile, fileUploadBizEnum);

        // 2. 构建文件路径
        User loginUser = UserContext.getUser();
        String uuid = RandomStringUtils.randomAlphanumeric(8);
        String filename = uuid + "." + FileUtil.getSuffix(multipartFile.getOriginalFilename());
        String filepath = String.format("%s/%s/%s", fileUploadBizEnum.getEnCode(), loginUser.getId(), filename);

        // 3. 上传文件
        File file = null;
        try {
            file = File.createTempFile(filepath.replace("/", "_"), "");
            multipartFile.transferTo(file);
            putObject(filepath, file);
            // 返回可访问地址
            return ossClientConfig.getOssHost() + "/" + filepath;
        } catch (Exception e) {
            log.error("文件上传失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        } finally {
            if (file != null) {
                boolean deleted = file.delete();
                if (!deleted) {
                    log.warn("临时文件删除失败: {}", file.getAbsolutePath());
                }
            }
        }
    }

    /**
     * 校验文件
     */
    private void validFile(MultipartFile multipartFile, FileUploadBizEnum fileUploadBizEnum) {
        if (FileUploadBizEnum.USER_AVATAR.equals(fileUploadBizEnum)) {
            if (multipartFile.getSize() > UPLOAD_FILE_MAX_SIZE) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小不能超过 10M");
            }
            if (!AVATAR_FILE_TYPE.contains(FileUtil.getSuffix(multipartFile.getOriginalFilename()))) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件类型错误");
            }
        }
    }

    /**
     * 上传对象到阿里云 OSS
     *
     * @param key  唯一键
     * @param file 文件
     */
    private void putObject(String key, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(
                ossClientConfig.getOss().getBucketName(), key, file);
        ossClient.putObject(putObjectRequest);
    }
}
