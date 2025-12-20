import service from './index';

/**
 * 登录请求参数
 */
export interface UserLoginRequest {
  userAccount?: string;
  userPassword?: string;
}

/**
 * 用户注册请求参数
 */
export interface UserRegisterRequest {
  checkPassword?: string;
  userAccount?: string;
  userPassword?: string;
}

/**
 * 用户信息更新请求参数
 */
export interface UserUpdateMyRequest {
  userAvatar?: string;
  userName?: string;
  userProfile?: string;
}

/**
 * 用户视图对象
 */
export interface UserVO {
  createTime?: string;
  id?: number;
  userAvatar?: string;
  userName?: string;
  userProfile?: string;
  userRole?: string;
}

/**
 * 用户登录
 * @param params
 */
export function userLogin(params: UserLoginRequest) {
  return service({
    url: '/user/login',
    method: 'post',
    data: params,
  });
}

/**
 * 用户注册
 * @param params
 */
export function userRegister(params: UserRegisterRequest) {
  return service({
    url: '/user/register',
    method: 'post',
    data: params,
  });
}

/**
 * 获取当前登录用户
 */
export function getLoginUser() {
  return service({
    url: '/user/get/login',
    method: 'get',
  });
}

/**
 * 用户注销
 */
export function userLogout() {
  return service({
    url: '/user/logout',
    method: 'post',
  });
}

/**
 * 更新个人信息
 * @param params
 */
export function updateMyUser(params: UserUpdateMyRequest) {
  return service({
    url: '/user/update/my',
    method: 'post',
    data: params,
  });
}

export interface UserUpdatePwdRequest {
    newPwd?: string;
    oldPwd?: string;
}

/**
 * 更新用户密码
 * @param params
 */
export function updateUserPwd(params: UserUpdatePwdRequest) {
    return service({
        url: '/user/update/pwd',
        method: 'post',
        data: params,
    });
}

/**
 * 获取用户签到情况
 * @param userId
 * @param yearMonth
 */
export function getUserCheckIn(userId: number, yearMonth: string) {
    return service({
        url: '/user-check-in/info',
        method: 'get',
        params: {
            userId,
            yearMonth
        }
    })
}
