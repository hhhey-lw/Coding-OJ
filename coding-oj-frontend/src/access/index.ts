import router from "@/router";
import store from "@/store";
import ACCESS_ENUM from "@/access/accessEnum";
import checkAccess from "@/access/checkAccess";

router.beforeEach(async (to, from, next) => {
  console.log("登录用户信息", store.state.user.loginUser);
  let loginUser = store.state.user.loginUser;
  
  // 如果之前没登录过，检查 localStorage 中的 token
  if (!loginUser || !loginUser.userRole || loginUser.userRole === ACCESS_ENUM.NOT_LOGIN) {
    await store.dispatch("user/getLoginUser");
    loginUser = store.state.user.loginUser;
  }
  
  const needAccess = (to.meta?.access as string) ?? ACCESS_ENUM.NOT_LOGIN;
  
  // 要跳转的页面需要登录
  if (needAccess !== ACCESS_ENUM.NOT_LOGIN) {
    // 检查是否有 token
    const token = localStorage.getItem('token');
    
    // 如果没有 token，跳转到登录页面
    if (!token) {
      next(`/user/login?redirect=${to.fullPath}`);
      return;
    }
    
    // 如枟有 token 但是权限不足，跳转到无权限页面
    if (loginUser && loginUser.userRole && !checkAccess(loginUser, needAccess)) {
      next("/noAuth");
      return;
    }
  }
  
  next();
});
