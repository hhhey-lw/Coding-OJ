<template>
  <div id="basicLayout">
    <a-layout style="min-height: 100vh">
      <a-layout-header class="header">
        <GlobalHeader />
      </a-layout-header>
      <a-layout-content class="content">
        <router-view />
      </a-layout-content>
<!--      <a-layout-footer class="footer">-->
<!--        <a href="http://www.snowyee.cn" target="_blank">-->
<!--          𝑳𝑶𝑵𝑮 𝑶𝑱-->
<!--        </a>-->
<!--      </a-layout-footer>-->
    </a-layout>
  </div>
</template>


<script>
import GlobalHeader from "@/components/GlobalHeader";

export default {
  components: { GlobalHeader },
};
</script>

<style scoped>
#basicLayout {
}

#basicLayout .header {
  position: fixed;
  top: 0;
  width: 100vw;
  background-color: #fff;
  display: flex; justify-content: center; align-content: center;
  margin-bottom: 16px;
  box-shadow: #eee 1px 1px 5px;
  z-index: 999;
  height: 5REM;
}

#basicLayout .content {
  max-width: 100vw;
  overflow-x: hidden;
  position: relative;
  background: linear-gradient(to right, #fefefe, #fff);
  margin-top: 0.5REM;
  padding-top: 5REM;
  padding-bottom: 20px;
  padding-left: 10px;
  padding-right: 10px;
  height: 100vh;
}

#basicLayout .footer {
  background: #efefef;
  padding: 16px;
  position: sticky;
  bottom: 0;
  left: 0;
  right: 0;
  text-align: center;
}
</style>
