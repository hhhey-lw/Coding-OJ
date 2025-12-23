package com.longoj.top.domain.service;

import com.longoj.top.domain.entity.enums.FileUploadBizEnum;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {

    /**
     * 上传文件
     *
     * @param multipartFile 文件
     * @param biz           业务类型
     * @return 文件访问地址
     */
    String uploadFile(MultipartFile multipartFile, FileUploadBizEnum biz);
}
