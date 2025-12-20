import service from './index';

/**
 * 上传文件
 * @param file
 * @param biz
 */
export function uploadFile(file: File, biz: string) {
  const formData = new FormData();
  formData.append('file', file);
  formData.append('biz', biz);
  return service({
    url: '/file/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  });
}
