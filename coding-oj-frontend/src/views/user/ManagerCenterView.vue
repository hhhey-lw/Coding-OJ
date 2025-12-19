<template>
  <div class="user-profile-container">
    <a-tabs default-active-key="manageQuestion" :position="tabsPosition" :lazy-load="true" class="profile-tabs">
      <!-- 基本信息标签页 -->
      <!--   每月签到页面   -->
      <a-tab-pane key="manageQuestion" title="管理题目">
        <ManageQuestionView/>
      </a-tab-pane>

      <!--   用户提交信息页面   -->
      <a-tab-pane key="addQuestion" title="添加题目">
        <AddQuestionView/>
      </a-tab-pane>
    </a-tabs>
  </div>
</template>

<script setup lang="ts">
import {computed, onMounted, ref} from 'vue';
import ManageQuestionView from "@/views/question/ManageQuestionView.vue";
import AddQuestionView from "@/views/question/AddQuestionView.vue";


// 动态调整样式
const isMobile = ref(false);
const tabsPosition = computed(() => isMobile.value ? 'top' : 'left');
const checkScreenSize = () => {
  isMobile.value = window.innerWidth <= 768; // 768px为移动端断点
};

onMounted(() => {
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


.form-item h3 {
  margin-bottom: 12px;
  font-size: 16px;
  color: #1d2129;
}

.security-section h2 {
  margin-bottom: 24px;
  color: #1d2129;
}

:deep(.arco-table-pagination) {
  display: flex;
  justify-content: center;
  margin-top: 16px;
}
</style>