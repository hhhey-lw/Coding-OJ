<template>
  <div id="userRegisterPage">
    <h2 style="margin-bottom: 2REM; margin-top: 5REM">用户注册</h2>
    <a-form
      :model="form"
      :style="{ width: '80%', maxWidth: 300, margin: '0 auto' }"
      label-align="left"
      auto-label-width
      @submit="handleSubmit"
    >
      <a-form-item field="userAccount" label="账号">
        <a-input v-model="form.userAccount" placeholder="请输入账号" />
      </a-form-item>
      <a-form-item field="userPassword" tooltip="密码不小于 8 位" label="密码">
        <a-input-password
          v-model="form.userPassword"
          placeholder="请输入密码"
        />
      </a-form-item>
      <a-form-item
        field="checkPassword"
        tooltip="确认密码不小于 8 位"
        label="确认密码"
      >
        <a-input-password
          v-model="form.checkPassword"
          placeholder="请输入确认密码"
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
            注册
          </button>
          <a-link @click="gologin">已有账号</a-link>
        </div>
      </a-form-item>
    </a-form>
  </div>
</template>

<script setup lang="ts">
import { reactive } from "vue";
// import API from "@/api";
import { UserControllerService } from "../../../generated";
// import { userRegisterUsingPost } from "@/api/userController";
import message from "@arco-design/web-vue/es/message";
import { useRouter } from "vue-router";

const router = useRouter();

const form = reactive({
  userAccount: "",
  userPassword: "",
  checkPassword: "",
} as API.UserRegisterRequest);

/**
 * 提交
 */
const handleSubmit = async () => {
  const res = await UserControllerService.userRegisterUsingPost(form);
  if (res.code == 0) {
    message.success("注册成功");
    router.push({
      path: "/user/login",
      replace: true,
    });
  } else {
    message.error("注册失败，" + res.message);
  }
};

const gologin = () => {
  router.push({
    path: "/user/login"
  });
};
</script>

<style scoped>
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
