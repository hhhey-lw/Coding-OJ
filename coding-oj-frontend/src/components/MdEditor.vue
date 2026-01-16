<template>
  <Editor
    :value="value"
    :mode="mode"
    :plugins="plugins"
    @change="handleChange"
    @fullscreen-change="handleFullscreenChange"
  />
</template>

<script setup lang="ts">
import 'highlight.js/styles/github.css';
// gfm插件
import gfm from "@bytemd/plugin-gfm";
// 解析前言 格式化
import frontmatter from "@bytemd/plugin-frontmatter";
// 表情
// import gemoji from "@bytemd/plugin-gemoji";
// 突出显示代码块
import highlight from "@bytemd/plugin-highlight";
import "highlight.js/styles/default.css";
// import "katex/dist/katex.css";
// 缩放图片 点击图片放大,再点击缩小
// import mediumZoom from "@bytemd/plugin-medium-zoom";
// 主题
// import theme from "bytemd-plugin-theme";
// 代码高亮
// import highLightPlugin from "bytemd-plugin-highlight";
// 代码复制 - 已移除，使用内置高亮即可
// import copyCode from "bytemd-plugin-copy-code";
// import "bytemd-plugin-copy-code/dist/style/index.css";
// import 'juejin-markdown-themes/dist/juejin.min.css'
import { Editor } from "@bytemd/vue-next";

/**
 * 定义组件属性类型
 */
interface Props {
  value: string;
  mode?: string;
  handleChange: (v: string) => void;
}

function handleFullscreenChange(fullscreen: boolean) {
  console.log("全屏状态变化:", fullscreen);
}

const plugins = [
  // 作用 删除线,任务列表,表格
  gfm(),
  frontmatter(),
  // gemoji(),
  // mediumZoom(),
  // mermaid({
  //   locale: mermaidLocale,
  // }),
  highlight(),
  // theme(),
  // highLightPlugin(),
  // imagePlugin(),
  // copyCode({
  //   copySuccess: (text) => {
  //     console.log("复制成功");
  //   },
  //   copyError: (err) => {},
  //   copyRight: "",
  // }),
];

/**
 * 给组件指定初始值
 */
const props = withDefaults(defineProps<Props>(), {
  value: () => "",
  mode: () => "split",
  handleChange: (v: string) => {
    console.log(v);
  },
});
</script>

<style>
.bytemd-toolbar-icon.bytemd-tippy.bytemd-tippy-right:last-child {
  display: none;
}
</style>
