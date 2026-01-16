<template>
  <div id="viewQuestionView">
    <a-row :gutter="[24, 24]">
      <a-col class="quesBox" :md="12" :xs="24">
        <a-tabs default-active-key="question" style="overflow-y: scroll">
          <a-tab-pane key="question" title="题目" justify>
            <a-card v-if="question" :title="question.title">
              <a-descriptions
                title="判题条件"
                :column="{ xs: 1, md: 2, lg: 3 }"
              >
                <a-descriptions-item label="时间限制">
                  {{ question.judgeConfig?.timeLimit ?? 0 }}ms
                </a-descriptions-item>
                <a-descriptions-item label="内存限制">
                  {{ question.judgeConfig?.memoryLimit ?? 0 }}MB
                </a-descriptions-item>
<!--                <a-descriptions-item label="堆栈限制">-->
<!--                  {{ question.judgeConfig.stackLimit ?? 0 }}-->
<!--                </a-descriptions-item>-->
              </a-descriptions>
              <MdViewer :value="question.content || ''" />
              <template #extra>
                <a-space wrap>
                  <a-tag
                    v-for="(tag, index) of question.tags"
                    :key="index"
                    color="green"
                    >{{ tag }}
                  </a-tag>
                </a-space>
              </template>
            </a-card>
          </a-tab-pane >
          <a-tab-pane key="comment" title="评论" disabled justify> 评论区</a-tab-pane>
          <a-tab-pane key="answer" title="答案" justify> 暂时无法查看答案</a-tab-pane>
        </a-tabs>
      </a-col>
      <a-col class="quesBox editor-container" :md="12" :xs="24">
        <a-form :model="form" layout="inline" style="display: flex; width: 100%; justify-content: space-between; margin-bottom: 10px;">
          <a-form-item
              field="language"
              label="编程语言"
          >
            <a-select
                v-model="form.language"
                :style="{ width: '200px' }"
                placeholder="选择编程语言"
            >
              <a-option value="java">Java</a-option>
<!--              <a-option value="cpp">C++</a-option>-->
              <!--<a-option>cpp</a-option>-->
              <!--<a-option>go</a-option>-->
              <!--<a-option>html</a-option>-->
            </a-select>
          </a-form-item>

          <a-form-item>
            <button @click="doSubmit" class="btn-class">
              提交代码
            </button>
          </a-form-item>
        </a-form>
        <!-- 为编辑器添加 flex-grow -->
        <div class="editor-wrapper">
          <CodeEditor
              :value="form.code as string"
              :language="form.language"
              :handle-change="changeCode"
          />
        </div>
<!--        <a-divider size="0" />-->
<!--        <a-button type="primary" style="width: 100%" @click="doSubmit">-->
<!--          提交代码-->
<!--        </a-button>-->
      </a-col>
    </a-row>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, watchEffect } from "vue";
import { useRouter } from "vue-router";
import message from "@arco-design/web-vue/es/message";
import CodeEditor from "@/components/CodeEditor.vue";
import MdViewer from "@/components/MdViewer.vue";
import {
  getQuestionVOById,
  doQuestionSubmit,
  QuestionSubmitAddRequest,
  QuestionVO,
} from "@/api/question";

interface Props {
  id: string;
}

const props = withDefaults(defineProps<Props>(), {
  id: () => "",
});

const question = ref<QuestionVO>();
const router = useRouter();

const loadData = async () => {
  const res:any = await getQuestionVOById(
    props.id as any
  );
  if (res) {
    console.log('res.data',res);
    
    question.value = res;
  } else {
    message.error("加载失败");
  }
};

const form = ref<QuestionSubmitAddRequest>({
  language: "java",
  code: "",
});

/**
 * 提交代码
 */
const doSubmit = async () => {
  if (!question.value?.id) {
    return;
  }

  const res:any = await doQuestionSubmit({
    ...form.value,
    questionId: question.value.id,
  });
  if (res) {
    message.success("提交成功");
    router.push("/question_submit");
  } else {
    message.error("提交失败");
  }
};

/**
 * 页面加载时，请求数据
 */
onMounted(() => {
  loadData();
});

/**
 * 等question.value?.sourceCode有值之后再赋值
 */
watchEffect(() => {
  if (question.value?.sourceCode) {
    form.value.code = question.value.sourceCode;
  }
});

const changeCode = (value: string) => {
  form.value.code = value;
};
</script>

<style>
#viewQuestionView {
  max-width: 1400px;
  margin: 0 auto;
  height: calc(100vh - 90px); /* 假设顶部导航栏高度为64px */
  padding-bottom: -20px;
  overflow: hidden;
}

#viewQuestionView .arco-space-horizontal .arco-space-item {
  margin-bottom: 0 !important;
}

.quesBox {
  height: 90vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  padding-bottom: 1REM;
}

.editor-container {
  display: flex;
  flex-direction: column;
  overflow: hidden !important;
}

.editor-wrapper {
  flex: 1;
  min-height: 400px;
  position: relative;
}

.btn-class {
  background-color: #8ebc8e;
  border: 2px solid #2d8a55;
  border-radius: 5%;
  height: 36px;
  color: white;
  width: 6REM;
  font-size: 16px;
}

@media (max-width: 768px) {
  #viewQuestionView {
    overflow: scroll;
  }
  .quesBox {
    height: 100%;
  }
}

</style>
