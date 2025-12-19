<template>
  <div class="container">
    <!-- 左侧标题区域 -->
    <div class="left-section">
      <h1 class="main-title">专注于算法竞赛的在线评测系统</h1>

      <p class="description">
        为编程爱好者提供专业的算法训练平台。
      </p>

      <!-- 按钮组 -->
      <div class="button-group">
        <button class="btn btn-primary" @click="toQuestionPage">
          <span class="icon">✨</span>
          开始刷题
        </button>
        <button class="btn btn-secondary" @click="toSubmitPage">
          <span class="icon">⬇️</span>
          查看排名
        </button>
      </div>

      <!-- 特点列表 -->
      <div class="features-list">
        <div class="feature-item">
          <span class="check-icon">✓</span>
          <span class="feature-text">安全可靠</span>
        </div>
        <div class="feature-item">
          <span class="check-icon">✓</span>
          <span class="feature-text">极速响应</span>
        </div>
      </div>
    </div>

    <!-- 右侧展示区域 -->
    <div class="right-section">
      <div class="coloring-pages-container">
        <!-- 标题 -->
        <div class="coloring-title">
          <span class="dear-text">CODING</span>
          <span class="page-text">CODING OJ</span>
        </div>

        <!-- 涂色页面卡片 -->
        <div class="coloring-pages-cards">
          <div class="coloring-page-card" style="z-index: 3;">
            <img :src="rocket" alt="rocket" class="coloring-page-img">
          </div>

          <div class="coloring-page-card" style="z-index: 4;">
            <img :src="monster" alt="monster" class="coloring-page-img">
          </div>
        </div>

        <!-- 底部色条 -->
        <div class="color-indicators">
          <div class="color-bar color-red"></div>
          <div class="color-bar color-orange"></div>
          <div class="color-bar color-yellow"></div>
          <div class="color-bar color-green"></div>
          <div class="color-bar color-blue"></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import rocket from "@/assets/rocket.png";
import monster from "@/assets/monster.png";

// 这里可以添加组件逻辑
import {useRouter} from "vue-router";
const router = useRouter();

const toQuestionPage = () => {
  router.push({ path: 'questions' });
};

const toSubmitPage = () => {
  router.push({ path: 'question_submit' });
};

</script>

<style scoped>
.container {
  width: 80%;
  margin: 0 auto;
  display: flex;
  flex-direction: row;
  justify-content: center;

  min-height: 100vh;
  padding: 20px;
  background-color: #ffffff;
}

@media screen and (max-width: 768px) {
  .container {
    flex-direction: column-reverse;
    align-items: center;
  }

}

/* 左侧区域样式 */
.left-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.main-title {
  font-size: 2.5rem;
  font-weight: bold;
  margin-bottom: 20px;
  color: #333;
}

.description {
  font-size: 1.1rem;
  line-height: 1.6;
  margin-bottom: 30px;
  color: #555;
}

.button-group {
  display: flex;
  gap: 15px;
  margin-bottom: 30px;
}

.btn {
  padding: 12px 24px;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
}

.btn-primary {
  background-color: #f5f3ec;
  color: #333;
  border: none;
}

.btn-secondary {
  background-color: #fff;
  color: #333;
  border: 1px solid #ddd;
}

.icon {
  font-size: 1.2rem;
}

.features-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 1rem;
}

.check-icon {
  color: #4CAF50;
}

/* 右侧区域样式 */
.right-section {
  min-width: 40%;
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.coloring-pages-container {
  width: 70%;
  position: relative;
  padding: 20px;
  background-color: #f5f3ec;
  border-radius: 15px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
}

@media screen and (max-width: 768px) {
  .coloring-pages-container {
    width: 70vw;
  }

  .coloring-pages-cards {
    height: 350px;
  }
}

@media screen and (min-width: 768px) {
  .container {
    margin-top: -100px;
  }
}

.coloring-title {
  position: absolute;
  top: 20px;
  right: 20px;
  text-align: right;
}

.dear-text {
  font-size: 2rem;
  font-weight: bold;
  letter-spacing: 2px;
  display: block;
  color: #e0c0a0;
}

.page-text {
  font-size: 1.5rem;
  display: block;
  color: #555;
}

.coloring-pages-cards {
  position: relative;
  height: 400px;
  margin: 30px 0;
}

.coloring-page-card {
  margin-top: 50px;
  position: absolute;
  width: 80%;
  max-width: 350px;
  height: 80%;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease;
  cursor: pointer;
}

.coloring-page-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 10px;
}

/* 页面层叠顺序和位置 */
.coloring-page-card:nth-child(1) {
  top: -10%;
  left: 10%;
  transform-origin: bottom left;
  z-index: 3;
  transform: rotate(-10deg);
}

.coloring-page-card:nth-child(2) {
  top: 20%;
  right: 5%;
  transform-origin: top right;
  z-index: 2;
  transform: rotate(5deg);
}

.coloring-page-card:hover {
  transform: scale(1.05);
  z-index: 33;
}

/**.coloring-page-card:nth-child(3) {
  bottom: 10%;
  right: 15%;
  transform-origin: bottom right;
  z-index: 1;
} **/

.color-indicators {
  display: flex;
  justify-content: center;
  gap: 10px;
  margin-top: 20px;
}

.color-bar {
  width: 20px;
  height: 20px;
  border-radius: 4px;
}

.color-red    { background-color: #f48c8c; }
.color-orange { background-color: #f4c38c; }
.color-yellow { background-color: #f4e08c; }
.color-green  { background-color: #b4e08c; }
.color-blue   { background-color: #8cd0e0; }
</style>