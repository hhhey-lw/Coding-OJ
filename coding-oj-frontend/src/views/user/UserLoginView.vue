<template>
  <div id="userLoginView">
    <h2 style="margin-bottom: 2REM; margin-top: 5REM">用户登录</h2>
    <a-form
      :style="{ width: '80%', maxWidth: 300, margin: '0 auto' }"
      label-align="left"
      auto-label-width
      :model="form"
      @submit="handleSubmit"
    >
      <a-form-item field="userAccount" label="账号">
        <a-input v-model="form.userAccount" placeholder="请输入账号" />
      </a-form-item>
      <a-form-item field="userPassword" tooltip="密码不少于 8 位" label="密码">
        <a-input-password
          v-model="form.userPassword"
          placeholder="请输入密码"
        />
      </a-form-item>
      <a-form-item>
        <div
          style="
            display: flex;
            width: 100%;
            align-items: center;
            justify-content: space-between;
          "
        >
          <button html-type="submit" style="width: 120px" class="button-class">
            登录
          </button>
          <a-link @click="goRegister">新用户注册</a-link>
        </div>
      </a-form-item>
    </a-form>
  </div>
</template>

<script setup lang="ts">
import { reactive } from "vue";
import { userLogin, UserLoginRequest } from "@/api/user";
import message from "@arco-design/web-vue/es/message";
import { useRouter } from "vue-router";
import { useStore } from "vuex";

/**
 * 表单信息
 */
const form = reactive({
  userAccount: "",
  userPassword: "",
} as UserLoginRequest);

const router = useRouter();
const store = useStore();

/**
 * 提交表单
 * @param data
 */
const handleSubmit = async () => {
  const res:any = await userLogin(form);
  console.log(res);
  // 登录成功，保存用户信息和 token
  if (res) {
    await store.dispatch("user/setLoginUser", res);
    message.success("登录成功");
    router.push({
      path: "/",
      replace: true,
    });
  } else {
    message.error("登录失败");
  }
};

const goRegister = async () => {
    router.push("/user/register");
  }
</script>

<style>
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
