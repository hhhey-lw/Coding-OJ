import service from './index';

/**
 * 上传文件
 * @param file
 * @param bizType
 */
export function uploadFile(file: File, bizType: string) {
  const formData = new FormData();
  formData.append('file', file);
  formData.append('bizType', bizType);
  return service({
    url: '/file/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  });
}
