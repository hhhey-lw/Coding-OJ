<template>
  <div
    id="code-editor"
    ref="codeEditorRef"
    style="height: 100%; min-height: 400px;"
  />
</template>

<script setup lang="ts">
import * as monaco from "monaco-editor";
import { onMounted, ref, toRaw, watch } from "vue";

/**
 * 定义组件属性类型
 */
interface Props {
  value: string;
  language?: string;
  handleChange: (v: string) => void;
}

/**
 * 给组件指定初始值
 */
const props = withDefaults(defineProps<Props>(), {
  value: () => "",
  language: () => "java",
  handleChange: (v: string) => {
    console.log(v);
  },
});

const codeEditorRef = ref();
const codeEditor = ref();

// 更新内容时避免闪烁
const updateEditorContent = (newValue: string) => {
  if (codeEditor.value) {
    const editor = toRaw(codeEditor.value);
    const model = editor.getModel();
    if (model?.getValue() !== newValue) {
      // 只在内容实际变化时更新
      const position = editor.getPosition(); // 获取光标位置

      // 使用 applyEdits 而不是 pushEditOperations 或 setValue
      const range = model.getFullModelRange();
      editor.executeEdits('updateContent', [
        {
          range,
          text: newValue,
          forceMoveMarkers: true
        }
      ]);

      // 恢复光标位置
      if (position) {
        editor.setPosition(position);
      }
    }
  }
};

// 监听 props.value 的变化
watch(
  () => props.value,
  (newValue) => {
    if (codeEditor.value) {
      updateEditorContent(newValue);
    }
  }
);

// 监听 props.language 的变化
watch(
  () => props.language,
  () => {
    if (codeEditor.value) {
      monaco.editor.setModelLanguage(
        toRaw(codeEditor.value).getModel(),
        props.language
      );
    }
  }
);

onMounted(() => {
  if (!codeEditorRef.value) {
    return;
  }
  // 初始化 Monaco Editor
  codeEditor.value = monaco.editor.create(codeEditorRef.value, {
    value: props.value,
    language: props.language,
    automaticLayout: true,
    colorDecorators: true,
    minimap: {
      enabled: false,
    },
    readOnly: false,
    theme: "vs",
  });

  // 编辑时监听内容变化
  codeEditor.value.onDidChangeModelContent(() => {
    props.handleChange(toRaw(codeEditor.value).getValue());
  });
});
</script>

<style scoped></style>
