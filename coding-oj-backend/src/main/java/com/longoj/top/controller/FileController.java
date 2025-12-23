package com.longoj.top.controller;

import com.longoj.top.controller.dto.BaseResponse;
import com.longoj.top.controller.dto.file.UploadFileRequest;
import com.longoj.top.domain.entity.enums.FileUploadBizEnum;
import com.longoj.top.domain.service.FileService;
import com.longoj.top.infrastructure.exception.BusinessException;
import com.longoj.top.infrastructure.exception.ErrorCode;
import com.longoj.top.infrastructure.utils.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

/**
 * 文件接口
 */
@Slf4j
@RestController
@RequestMapping("/file")
public class FileController {

    @Resource
    private FileService fileService;

    /**
     * 文件上传
     */
    @PostMapping("/upload")
    public BaseResponse<String> uploadFile(@RequestPart("file") MultipartFile multipartFile,
                                           @RequestBody UploadFileRequest uploadFileRequest) {
        FileUploadBizEnum fileUploadBizEnum = FileUploadBizEnum.getByEnCode(uploadFileRequest.getBiz());
        if (fileUploadBizEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "业务类型错误");
        }
        String url = fileService.uploadFile(multipartFile, fileUploadBizEnum);
        return ResultUtils.success(url);
    }

}
