<template>
  <a-row id="globalHeader" style="width: 80%;" align="center" :wrap="false">
    <a-col flex="auto">
      <a-menu mode="horizontal" :selected-keys="selectedKeys" @menu-item-click="doMenuClick">
        <a-menu-item key="0" :style="{ padding: 0, marginRight: '38px' }" disabled>
          <div class="title-bar">
<!--            <img class="logo" src="../assets/favicon-32x32.png" />-->
            <div class="title">𝐂𝐨𝐝𝐢𝐧𝐠 𝐎𝐉</div>
          </div>
        </a-menu-item>
        <a-menu-item v-for="item in visibleRoutes" :key="item.path">
          <template #icon v-if="item.meta?.icon">
            <component :is="item.meta.icon" />
          </template>
          {{ item.name }}
        </a-menu-item>
      </a-menu>
    </a-col>
    <a-col flex="100px">
      <!-- 未登录状态 -->
      <bottom v-if="!store.state.user.loginUser.id"
              class="login-btn-class"
              @click="gologin"><icon-user/>登录
      </bottom>

      <!-- 已登录状态 -->
      <a-popover
          v-else
          trigger="click"
          position="bottom"
          class="user-avatar-popover"
      >
        <a-link class="user-avatar-link">
          <!-- 显示用户头像，如果不存在则显示默认头像 -->
          <a-avatar
              v-if="store.state.user.loginUser.userAvatar"
              :image-url="store.state.user.loginUser.userAvatar"
              :size="32"
          />
          <a-avatar v-else :size="32">
            <icon-user />
          </a-avatar>
          <span class="user-name" style="color: black; margin-left: 0.5REM">
          {{ store.state.user.loginUser.userName || "无名" }}
          </span>
        </a-link>

        <template #content>
          <div class="user-menu">
            <div class="user-info">
              <a-avatar
                  v-if="store.state.user.loginUser.userAvatar"
                  :image-url="store.state.user.loginUser.userAvatar"
                  :size="48"
              />
              <a-avatar v-else :size="48">
                <icon-user />
              </a-avatar>
              <div class="user-details">
                <p class="user-name">{{ store.state.user.loginUser.userName || "无名" }}</p>
                <p class="user-email" v-if="store.state.user.loginUser.email">
                  {{ store.state.user.loginUser.email }}
                </p>
              </div>
            </div>
            <a-divider margin="8px 0" />
            <a-link class="menu-item" @click="goProfile">
              <icon-user /> 个人中心
            </a-link>
            <a-link class="menu-item logout" @click="logout">
              <icon-export /> 退出登录
            </a-link>
          </div>
        </template>
      </a-popover>
    </a-col>
  </a-row>
</template>

<script setup lang="ts">
import { routes } from "../router/routes";
import { useRouter } from "vue-router";
import { computed, ref } from "vue";
import { useStore } from "vuex";
import checkAccess from "@/access/checkAccess";
import ACCESS_ENUM from "@/access/accessEnum";

import {
  IconHome,
  IconList,
  IconComputer,
  IconPalette,
  IconUser,
  IconExport,
  IconTrophy,
  IconSettings
} from '@arco-design/web-vue/es/icon';

const router = useRouter();
const store = useStore();

// 展示在菜单的路由数组
const visibleRoutes = computed(() => {
  return routes.filter((item, index) => {
    if (item.meta?.hideInMenu) {
      return false;
    }
    // 根据权限过滤菜单
    if (
      !checkAccess(store.state.user.loginUser, item?.meta?.access as string)
    ) {
      return false;
    }
    return true;
  });
});

// 默认主页
const selectedKeys = ref(["/"]);

// 路由跳转后，更新选中的菜单项
router.afterEach((to, from, failure) => {
  selectedKeys.value = [to.path];
});


setTimeout(() => {
  store.dispatch("user/getLoginUser", {
    userName: "Admin",
    userRole: ACCESS_ENUM.ADMIN,
  });
}, 3000);

const doMenuClick = (key: string) => {
  router.push({
    path: key,
  });
};

const logout = () => {
  store.dispatch("user/logout");
  store.commit("user/clearToken");
  router.push({
    path: "/questions",
  });
  window.location.reload();
};

const goProfile = () => {
  router.push({
    path: "/infoCenter",
  });
}

const gologin = () => {
  router.push({
    path: "/user/login",
  });
};
</script>

<style scoped>
.logout {
  cursor: pointer;
}

.title-bar {
  display: flex;
  align-items: center;
}

.title {
  color: #444;
  margin-left: 16px;
}

.logo {
  height: 48px;
}
/* 新增样式以调整图标和文字间距 */
:deep(.arco-menu-item .arco-menu-icon) {
  margin-right: 4px !important; /* 将默认的 8px 或其他值调整为您希望的大小，例如 4px 或 2px */
}

.login-btn-class {
  background-color: #8ebc8e;
  border: 2px solid #2d8a55;
  border-radius: 1REM;
  height: 36px;
  color: white;
  width: 8REM;
  font-size: 16px;
  cursor: pointer;
  padding: 0.5REM;
}
</style>
