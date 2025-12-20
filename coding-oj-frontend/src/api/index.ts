import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios';

// 创建 axios 实例
const service: AxiosInstance = axios.create({
  baseURL: 'http://localhost:8101/api', 
  timeout: 10000, // 请求超时时间
  withCredentials: true, // 跨域请求时是否发送 cookie
});

// 请求拦截器
service.interceptors.request.use(
  (config: any) => {
    // 在发送请求之前做些什么，例如添加 token
    // const token = localStorage.getItem('token');
    // if (token) {
    //   config.headers['Authorization'] = token;
    // }
    return config;
  },
  (error: any) => {
    // 对请求错误做些什么
    return Promise.reject(error);
  }
);

// 响应拦截器
service.interceptors.response.use(
  (response: AxiosResponse) => {
    // 对响应数据做点什么
    const res = response.data;
    // 假设后端返回的数据结构是 { code: number, data: any, message: string }
    if (res.code !== 0) {
      // 40100: 未登录
      if (res.code === 40100) {
          // 不做任何处理，直接返回空或者null，让业务层自己处理，或者跳转登录页
          return null;
      }
      // 处理错误，弹窗提示
      Message.error(res.message || 'Error');
      return Promise.reject(new Error(res.message || 'Error'));
    }
    return res.data;
  },
  (error: any) => {
    // 对响应错误做点什么
    console.error('err' + error); // for debug
    Message.error(error.message || '请求失败');
    return Promise.reject(error);
  }
);

export default service;
