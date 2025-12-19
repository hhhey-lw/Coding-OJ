<template>
  <a-space id="addDiscussionView" direction="vertical" class="box-class">
    <h2 class="header-box">发布帖子</h2>
    <a-form :model="form" label-align="right" class="form-box">
      <a-form-item field="title" label="标题">
        <a-input v-model="form.title" placeholder="请输入标题" />
      </a-form-item>
      <a-form-item field="tags" label="标签">
        <a-input-tag v-model="form.tags" placeholder="请选择标签" allow-clear />
      </a-form-item>
      <a-form-item field="content" label="内容" style="z-index: 999;">
        <MdEditor :value="form.content" :handle-change="onContentChange" class="editor-box"/>
      </a-form-item>
      <div style="margin-top: 16px" />
      <a-form-item>
        <button class="button-class" @click="doSubmit"
        >发布
        </button>
      </a-form-item>
    </a-form>
  </a-space>
</template>

<script setup lang="ts">
import { ref } from "vue";
import MdEditor from "@/components/MdEditor.vue";
import {PostAddRequest, PostControllerService} from "../../../generated";
import { Modal } from '@arco-design/web-vue';
import message from "@arco-design/web-vue/es/message";

let form = ref<PostAddRequest>({
  title: "",
  tags: [],
  content: ""
});

const doSubmit = async () => {
  Modal.confirm({
    title: '确认发布吗？',
    content: `确定要发布编写的帖子吗？`,
    onOk: async () => {
      const res = await PostControllerService.addPostUsingPost(
          form.value
      );
      if (res.code === 0) {
        message.success("创建成功");
        alert("发布成功！");
        form.value.content = "";
        form.value.tags = "";
        form.value.title = "";
      } else {
        message.error("创建失败，" + res.message);
      }
    }
  })
};

const onContentChange = (value: string) => {
  form.value.content = value;
};

</script>

<style scoped>
.box-class {
  width: 90%;
  margin: 0 auto;
}

.header-box {
  text-align: center;
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 1REM;
}

.form-box {
  width: 100%;
  margin: 0 auto;
  padding: 20px;
  border-radius: 8px;
  margin-left: -2REM;
}

.editor-box {
  width: 100%;
}

.button-class {
  white-space: nowrap;
  background-color: #8ebc8e;
  border: 2px solid #2d8a55;
  border-radius: 5%;
  height: 36px;
  color: white;
  width: 12REM;
  font-size: 16px;
}

.button-class:hover {
  background-color: #2d8a55;
  border-color: #1c5d39;
  cursor: pointer;
}

</style>
