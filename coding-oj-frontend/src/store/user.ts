// initial state
import { StoreOptions } from "vuex";
import ACCESS_ENUM from "@/access/accessEnum";
import { UserControllerService } from "../../generated";

export default {
  namespaced: true,
  state: () => ({
    loginUser: {
      userName: "未登录",
    },
    token: '',
  }),
  actions: {
    async getLoginUser({ commit, state }, payload) {
      if (state.loginUser.userName !== "未登录" && state.token !== '') {
        console.log("已登录");
        return;
      }
      // 从远程请求获取登录信息
      const res = await UserControllerService.getLoginUserUsingGet();
      if (res.code === 0) {
        console.log('LoginUser res.data', res.data);

        commit("updateUser", res.data);
      } else {
        commit("updateUser", {
          ...state.loginUser,
          userRole: ACCESS_ENUM.NOT_LOGIN,
        });
      }
    },
    async logout({ commit }){
      const res = await UserControllerService.userLogoutUsingPost();
      if (res.code === 0) {
      }
    },

    // 新增：手动设置 token（如从本地存储读取）
    async setTokenManually({ commit }, token: string) {
      commit("setToken", token);
    },
  },
  mutations: {
    updateUser(state, payload) {
      state.loginUser = payload;
    },

    // 新增：设置 token
    setToken(state, token: string) {
      state.token = token;
    },
    // 新增：清除 token
    clearToken(state) {
      state.token = '';
    },
  },
} as StoreOptions<any>;
