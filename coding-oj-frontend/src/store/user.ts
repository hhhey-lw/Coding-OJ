// initial state
// @ts-ignore
import { StoreOptions, Commit } from "vuex";
import ACCESS_ENUM from "@/access/accessEnum";
import { userLogout } from "@/api/user";

interface UserState {
  loginUser: {
    userName: string;
    userRole?: string;
    token?: string;
    [key: string]: any;
  };
}

export default {
  namespaced: true,
  state: () => ({
    loginUser: {
      userName: "未登录",
    },
  }),
  actions: {
    // 获取登录用户信息（从 localStorage 检查登录状态）
    async getLoginUser({ commit, state }: { commit: Commit; state: UserState }) {
      // 检查是否已经有用户信息
      if (state.loginUser.userName !== "未登录") {
        return;
      }
      
      // 检查 localStorage 中是否有 token
      const token = localStorage.getItem('token');
      if (!token) {
        // 没有 token，设置为未登录状态
        commit("updateUser", {
          userName: "未登录",
          userRole: ACCESS_ENUM.NOT_LOGIN,
        });
        return;
      }
      
      // 有 token，尝试从 localStorage 恢复用户信息
      const userInfoStr = localStorage.getItem('userInfo');
      if (userInfoStr) {
        try {
          const userInfo = JSON.parse(userInfoStr);
          commit("updateUser", userInfo);
        } catch (e) {
          console.error('解析用户信息失败', e);
          commit("updateUser", {
            userName: "未登录",
            userRole: ACCESS_ENUM.NOT_LOGIN,
          });
        }
      } else {
        // 没有用户信息，设置为未登录
        commit("updateUser", {
          userName: "未登录",
          userRole: ACCESS_ENUM.NOT_LOGIN,
        });
      }
    },
    
    // 登录后保存用户信息和 token
    async setLoginUser({ commit }: { commit: Commit }, loginUserVO: any) {
      if (loginUserVO.token) {
        // 保存 token 到 localStorage
        localStorage.setItem('token', loginUserVO.token);
      }
      // 保存用户信息（不包含 token）
      const { token, ...userInfo } = loginUserVO;
      // 保存到 localStorage
      localStorage.setItem('userInfo', JSON.stringify(userInfo));
      // 保存到 store
      commit("updateUser", userInfo);
    },
    
    // 退出登录
    async logout({ commit }: { commit: Commit }) {
      const res = await userLogout();
      if (res !== undefined) {
        // 清除 token 和用户信息
        localStorage.removeItem('token');
        localStorage.removeItem('userInfo');
        // 重置用户信息
        commit("updateUser", {
          userName: "未登录",
          userRole: ACCESS_ENUM.NOT_LOGIN,
        });
      }
    },
  },
  mutations: {
    updateUser(state: UserState, payload: UserState['loginUser']) {
      state.loginUser = payload;
    },
  },
} as StoreOptions<any>;
