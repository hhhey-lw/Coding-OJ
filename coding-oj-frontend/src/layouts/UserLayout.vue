<template>
  <div id="userLayout">
    <a-layout style="min-height: 100vh">
      <a-layout-header class="header">
        <a-space>
<!--          <img src="../assets/xue-logo.svg" class="logo" />-->
          <div>𝐂𝐨𝐝𝐢𝐧𝐠 𝐎𝐉</div>
        </a-space>
      </a-layout-header>
      <a-layout-content class="content">
        <router-view />
      </a-layout-content>
<!--      <a-layout-footer class="footer">-->
<!--        <a href="http://www.snowyee.cn" target="_blank">-->
<!--          𝙇𝙊𝙉𝙂 𝙊𝙅-->
<!--        </a>-->
<!--      </a-layout-footer>-->
    </a-layout>
  </div>
</template>

<style scoped>
#userLayout {
  text-align: center;
  background: url("https://gw.alipayobjects.com/zos/rmsportal/FfdJeJRQWjEeGTpqgBKj.png")
    0% 0% / 100% 100%;
}

#userLayout .logo {
  width: 64px;
  height: 64px;
}

#userLayout .header {
  margin-top: 16px;
}

#userLayout .content {
  margin-bottom: 16px;
  padding: 20px;
}

#userLayout .footer {
  padding: 16px;
  position: sticky;
  bottom: 0;
  left: 0;
  right: 0;
  text-align: center;
}
</style>
<script></script>
