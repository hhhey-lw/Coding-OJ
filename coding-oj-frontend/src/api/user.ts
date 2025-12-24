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
 * 登录用户视图对象
 */
export interface LoginUserVO {
  id?: number;
  userName?: string;
  userAvatar?: string;
  userProfile?: string;
  userRole?: string;
  createTime?: string;
  updateTime?: string;
  token?: string;
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
    oldPwd: string;
    newPwd: string;
    confirmNewPwd: string;
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
 * @param yearMonth
 */
export function getUserCheckIn(yearMonth: string) {
    return service({
        url: '/user/check-in/info',
        method: 'get',
        params: {
            yearMonth
        }
    })
}
