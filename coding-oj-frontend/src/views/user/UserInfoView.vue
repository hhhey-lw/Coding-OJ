<template>
  <div class="user-profile-container">
    <a-tabs default-active-key="basic" :position="tabsPosition" :lazy-load="true" class="profile-tabs">
      <!-- 基本信息标签页 -->
      <a-tab-pane key="basic" title="基本信息">
        <div class="basic-info-section">
          <div class="avatar-upload-container">
            <!-- 蓝色圆形头像 -->
            <a-avatar :size="80" :style="{ backgroundColor: '#3370ff' }" class="avatar-img">
              <img v-if="userInfo.userAvatar" :src="userInfo.userAvatar" alt="avatar">
              <icon-user v-else style="font-size: 36px; color: white" />
            </a-avatar>

            <!-- 右侧上传按钮 -->
            <button
                class="upload-btn button-class"
                @click="triggerUpload"
            >
              点击上传
            </button>

            <!-- 隐藏的文件输入 -->
            <input
                ref="fileInput"
                type="file"
                accept="image/*"
                style="display: none"
                @change="handleAvatarChange"
            >
          </div>

          <!-- 昵称编辑 -->
          <div class="form-item" style="min-width: 70%">
            <h3>昵称</h3>
            <a-input v-model="userInfo.userName" placeholder="请输入昵称" />
          </div>

          <!-- 个人介绍 -->
          <div class="form-item" style="min-width: 70%">
            <h3>个人介绍</h3>
            <a-textarea
                v-model="userInfo.userProfile"
                placeholder="关于你的个性、兴趣或经验..."
                :auto-size="{ minRows: 4 }"
            />
          </div>

          <!-- 保存按钮 -->
          <div class="form-actions">
            <button class="button-class" style="width: 10REM;"  @click="saveBasicInfo">保存更改</button>
          </div>
        </div>
      </a-tab-pane>

      <!-- 账号安全标签页 -->
      <a-tab-pane key="security" title="账号安全">
        <div class="security-section">
          <h2>修改密码</h2>

          <!-- 当前密码 -->
          <div class="form-item">
            <h3>当前密码</h3>
            <a-input-password v-model="securityInfo.currentPassword" placeholder="请输入当前密码" />
          </div>

          <!-- 新密码 -->
          <div class="form-item">
            <h3>新密码</h3>
            <a-input-password v-model="securityInfo.newPassword" placeholder="请输入新密码" />
          </div>

          <!-- 确认新密码 -->
          <div class="form-item">
            <h3>确认新密码</h3>
            <a-input-password v-model="securityInfo.confirmPassword" placeholder="请再次输入新密码" />
          </div>

          <!-- 保存按钮 -->
          <div class="form-actions">
            <button class="button-class" @click="changePassword">修改密码</button>
          </div>
        </div>
      </a-tab-pane>

      <!--   每月签到页面   -->
      <a-tab-pane key="checkIn" title="每月摘要">
        <UserCheckInView/>
      </a-tab-pane>

      <!--   用户提交信息页面   -->
      <a-tab-pane key="submitQuestion" title="提交信息">
        <UserQuestionSubmitView/>
      </a-tab-pane>

      <a-tab-pane key="userFavourPost" title="收藏帖子">
        <UserFavourView/>
      </a-tab-pane>

      <a-tab-pane key="userMyPost" title="我的帖子">
        <UserMyPostView/>
      </a-tab-pane>
    </a-tabs>
  </div>
</template>

<script setup lang="ts">
// import router from "@/router";
import {computed, onMounted, ref} from 'vue';
import {
  IconUser,
  IconUpload
} from '@arco-design/web-vue/es/icon';
import {
  uploadFile
} from "@/api/file";
import {
  updateMyUser,
  updateUserPwd
} from "@/api/user";
import UserCheckInView from "@/views/user/UserCheckInView.vue";
import {Message} from "@arco-design/web-vue";
import store from "@/store";
import UserQuestionSubmitView from "@/views/user/UserQuestionSubmitView.vue";
import UserFavourView from "@/views/user/UserFavourView.vue";
import UserMyPostView from "@/views/user/UserMyPostView.vue";

// 账号安全信息
const securityInfo = ref({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
});

// 用户基本信息
let userInfo = ref({
  userAvatar: '',
  userName: '',
  userProfile: '',
  avatarFile: null as File | null
});

const fileInput = ref<HTMLInputElement | null>(null);
const loading = ref(false);

const triggerUpload = () => {
  fileInput.value?.click(); // 使用可选链操作符
};

// 处理头像变更
const handleAvatarChange = (e: Event) => {
  const file = (e.target as HTMLInputElement).files?.[0];
  if (!file) return;

  // 验证文件类型和大小
  if (!['image/jpeg', 'image/png', 'image/gif'].includes(file.type)) {
    Message.error('仅支持JPEG、PNG、GIF格式');
    return;
  }

  if (file.size > 2 * 1024 * 1024) {
    Message.error('头像大小不能超过2MB');
    return;
  }

  // 保存文件对象用于后续上传
  userInfo.value.avatarFile = file;

  // 读取为Base64用于预览
  const reader = new FileReader();
  reader.onload = (event) => {
    if (event.target?.result) {
      userInfo.value.userAvatar = event.target.result as string;
    }
  };
  reader.readAsDataURL(file);
};

// 上传头像并更新用户信息
const uploadAndUpdateUser = async () => {
  try {
    loading.value = true;
    let avatarUrl = userInfo.value.userAvatar;

    if (userInfo.value.avatarFile) {

      const uploadRes:any = await uploadFile(
          userInfo.value.avatarFile,
          'user_avatar'
      );

      if (!uploadRes) {
        throw new Error('头像上传失败');
      }
      avatarUrl = uploadRes;
    }

    // 更新用户信息
    const updateRes:any = await updateMyUser({
      userName: userInfo.value.userName,
      userAvatar: avatarUrl,
      userProfile: userInfo.value.userProfile
    });

    if (updateRes) {
      Message.success('个人信息更新成功！');
      // 保留原有 token，更新用户信息
      const currentToken = store.state.user.loginUser.token;
      const updatedUserInfo = {
        ...updateRes,
        token: currentToken
      };
      // 更新 store 和 localStorage
      await store.dispatch('user/setLoginUser', updatedUserInfo);
      // 正确重置文件输入
      if (fileInput.value) fileInput.value.value = '';
      userInfo.value.avatarFile = null;
      // 重新初始化用户信息
      initUserInfo();
    } else {
      throw new Error('用户信息更新失败');
    }
  } catch (error: any) {
    console.error('操作失败:', error);
    Message.error(`操作失败: ${error?.message ?? '未知错误'}`);
  } finally {
    loading.value = false;
  }
};

// 在模板中调用
const saveBasicInfo = async () => {
  await uploadAndUpdateUser();
};

// 修改密码
const changePassword = async () => {
  if (securityInfo.value.newPassword !== securityInfo.value.confirmPassword) {
    console.error('两次输入的新密码不一致');
    return;
  }
  console.log('修改密码:', securityInfo.value);
  // 这里可以添加API调用修改密码
  let res:any = await updateUserPwd({
    oldPwd: securityInfo.value.currentPassword,
    newPwd: securityInfo.value.newPassword,
    confirmNewPwd: securityInfo.value.confirmPassword
  });

  if (res) {
    alert("更新成功！")
    await store.dispatch("user/logout");
    // router.push({
    //   path: "/questions",
    // });
    window.location.reload();
  }else {
    alert("更新失败！");
  }

};

function initUserInfo() {
  // 深拷贝，避免和 GlobalHeader 共享数据
  const loginUser = store.state.user.loginUser;
  userInfo.value = {
    userAvatar: loginUser.userAvatar || '',
    userName: loginUser.userName || '',
    userProfile: loginUser.userProfile || '',
    avatarFile: null
  };
}

// 动态调整样式
const isMobile = ref(false);
const tabsPosition = computed(() => isMobile.value ? 'top' : 'left');
const checkScreenSize = () => {
  // console.log("动态调整样式")
  isMobile.value = window.innerWidth <= 768; // 768px为移动端断点
};

onMounted(() => {
  initUserInfo();
  checkScreenSize();
  window.addEventListener('resize', checkScreenSize);
})
</script>

<style scoped>
.user-profile-container {
  max-width: 1200px;
  margin: 0 auto;
  margin-top: 1REM;
  padding: 20px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.profile-tabs {
  min-height: 500px;
}

.basic-info-section,
.security-section {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 20px;
}

.avatar-upload-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.upload-btn {
  background-color: #3370ff;
  border-color: #3370ff;
}

.upload-btn:hover {
  background-color: #1a5bff;
  border-color: #1a5bff;
}

.avatar-img {
  overflow: hidden;
  border: 1px solid #b7b4b4;
}

.form-item {
  margin-bottom: 24px;
  max-width: 500px;
}

.form-item h3 {
  margin-bottom: 12px;
  font-size: 16px;
  color: #1d2129;
}

.form-actions {
  margin-top: 32px;
}

.security-section h2 {
  margin-bottom: 24px;
  color: #1d2129;
}

.button-class {
  white-space: nowrap;
  background-color: #8ebc8e;
  border: 2px solid #2d8a55;
  border-radius: 5%;
  height: 36px;
  color: white;
  width: 6REM;
  font-size: 16px;
}

.button-class:hover {
  background-color: #2d8a55;
  border-color: #1c5d39;
  cursor: pointer;
}
</style>