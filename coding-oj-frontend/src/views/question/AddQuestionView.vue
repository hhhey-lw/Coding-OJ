<template>
  <div id="addQuestionView"style="width: 90vw;">
    <!-- 顶部导航栏 -->
    <header class="header" v-if="isUpdatePage">
      <a-button type="text" class="back-btn" @click="handleBack">
        <template #icon><icon-left /></template>
        返回
      </a-button>
      <div class="header-right">
        <a-button type="text" class="share-btn" @click="() => Message.info('分享功能尚未实现')">
          <template #icon><icon-share-alt /></template>
        </a-button>
      </div>
    </header>
    <h2 style="text-align: center">创建题目</h2>
    <a-form :model="form" label-align="right">
      <a-form-item field="title" label="标题">
        <a-input v-model="form.title" placeholder="请输入标题" />
      </a-form-item>
      <a-form-item field="tags" label="标签">
        <a-input-tag v-model="form.tags" placeholder="请选择标签" allow-clear />
      </a-form-item>
      <a-form-item field="difficulty" label="难度">
        <a-rate v-model="form.difficulty"/>
<!--        <a-input v-model="form.difficulty" placeholder="请输出题目难度"/>-->
      </a-form-item>
      <a-form-item field="content" label="题目内容">
        <div class="editor-container">
          <MdEditor :value="form.content" :handle-change="onContentChange" class="fullscreen-editor"/>
        </div>
      </a-form-item>
      <a-form-item field="answer" label="答案">
        <div class="editor-container">
        <MdEditor :value="form.answer" :handle-change="onAnswerChange" class="fullscreen-editor" />
        </div>
      </a-form-item>
      <a-form-item field="sourceCode" label="初始代码">
        <div class="editor-container">
        <MdEditor :value="form.sourceCode" :handle-change="onSourceCodeChange" class="fullscreen-editor"/>
        </div>
      </a-form-item>
      <a-form-item label="判题配置" :content-flex="true" :merge-props="false">
        <a-space direction="vertical">
          <a-form-item field="judgeConfig.timeLimit" label="时间限制"
                       :label-col-props="{ span: 8, style: { whiteSpace: 'nowrap' } }">
            <a-input-number
              v-model="form.judgeConfig.timeLimit"
              placeholder="时间限制"
              mode="button"
              min="0"
              size="large"
            />
          </a-form-item>
          <a-form-item field="judgeConfig.memoryLimit" label="内存限制"
                       :label-col-props="{ span: 8, style: { whiteSpace: 'nowrap' } }">
            <a-input-number
              v-model="form.judgeConfig.memoryLimit"
              placeholder="内存限制"
              mode="button"
              min="0"
              size="large"
            />
          </a-form-item>
          <a-form-item field="judgeConfig.stackLimit" label="堆栈限制"
                       :label-col-props="{ span: 8, style: { whiteSpace: 'nowrap' } }">
            <a-input-number
              v-model="form.judgeConfig.stackLimit"
              placeholder="堆栈限制"
              mode="button"
              min="0"
              size="large"
            />
          </a-form-item>
        </a-space>
      </a-form-item>
      <a-form-item
        label="测试用例配置"
        :content-flex="false"
        :merge-props="false"
      >
        <a-form-item
          v-for="(judgeCaseItem, index) of form.judgeCase"
          :key="index"
          no-style
        >
          <a-space direction="vertical" class="usecase-class mobile-usecase-class">
            <a-form-item
              :label-col-props="{ span: 8, style: { whiteSpace: 'nowrap', fontSize: `12px`} }"
              :field="`form.judgeCase[${index}].input`"
              :key="index"
              :label="`输入用例-${index}`"
            >
              <a-textarea
                :auto-size="false"
                v-model="judgeCaseItem.input"
                placeholder="请输入测试输入用例"
                style="height: 120px;"
              />
            </a-form-item>
            <a-form-item
              :label-col-props="{ span: 8, style: { whiteSpace: 'nowrap' } }"
              :field="`form.judgeCase[${index}].output`"
              :label="`输出用例-${index}`"
              :key="index"
            >
              <a-textarea
                :auto-size="false"
                v-model="judgeCaseItem.output"
                placeholder="请输入测试输出用例"
                style="height: 120px;"
              />
            </a-form-item>
            <a-button status="danger" @click="handleDelete(index)">
              删除用例-{{index}}
            </a-button>
          </a-space>
        </a-form-item>
        <div style="margin-top: 32px">
          <a-button @click="handleAdd" type="outline" status="success"
            >新增测试用例
          </a-button>
        </div>
      </a-form-item>
      <div style="margin-top: 16px" />
      <a-form-item>
        <button class="button-class" @click="doSubmit"
          >提交
        </button>
      </a-form-item>
    </a-form>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from "vue";
import MdEditor from "@/components/MdEditor.vue";
import { getQuestionById, updateQuestion, addQuestion } from "@/api/question";
import message from "@arco-design/web-vue/es/message";
import { useRouter, useRoute } from "vue-router";
import {Message} from "@arco-design/web-vue";
import {IconLeft, IconShareAlt} from "@arco-design/web-vue/es/icon";

const router = useRouter();
const route = useRoute();
// 如果页面地址包含 update，视为更新页面
const updatePage = route.path.includes("update");

const isUpdatePage = ref(updatePage);

let form = ref({
  title: "",
  tags: [],
  difficulty: "",
  answer: "",
  sourceCode: "",
  content: "",
  judgeConfig: {
    memoryLimit: 1000,
    stackLimit: 1000,
    timeLimit: 1000,
  },
  judgeCase: [
    {
      input: "",
      output: "",
    },
  ],
});

/**
 * 根据题目 id 获取老的数据
 */
const loadData = async () => {
  const id = route.query.id;
  if (!id) {
    return;
  }
  const res:any = await getQuestionById(
    id as any
  );
  if (res) {
    form.value = res as any;
    // json 转 js 对象
    if (!form.value.judgeCase) {
      form.value.judgeCase = [
        {
          input: "",
          output: "",
        },
      ];
    } else {
      form.value.judgeCase = JSON.parse(form.value.judgeCase as any);
    }
    if (!form.value.judgeConfig) {
      form.value.judgeConfig = {
        memoryLimit: 1000,
        stackLimit: 1000,
        timeLimit: 1000,
      };
    } else {
      form.value.judgeConfig = JSON.parse(form.value.judgeConfig as any);
    }
    if (!form.value.tags) {
      form.value.tags = [];
    } else {
      form.value.tags = JSON.parse(form.value.tags as any);
    }
  } else {
    message.error("加载失败");
  }
};

onMounted(() => {
  loadData();
});

const handleBack = () => {
  router.back();
};

const doSubmit = async () => {
  console.log(form.value);
  // 区分更新还是创建
  if (updatePage) {
    const res:any = await updateQuestion(
      form.value
    );
    if (res) {
      message.success("更新成功");
    } else {
      message.error("更新失败");
    }
  } else {
    const res:any = await addQuestion(
      form.value
    );
    if (res) {
      message.success("创建成功");
    } else {
      message.error("创建失败");
    }
  }
};

/**
 * 新增判题用例
 */
const handleAdd = () => {
  form.value.judgeCase.push({
    input: "",
    output: "",
  });
};

/**
 * 删除判题用例
 */
const handleDelete = (index: number) => {
  form.value.judgeCase.splice(index, 1);
};

const onContentChange = (value: string) => {
  form.value.content = value;
};

const onAnswerChange = (value: string) => {
  form.value.answer = value;
};

const onSourceCodeChange = (value: string) => {
  form.value.sourceCode = value;
};
</script>

<style scoped>
#addQuestionView {
  max-width: 1280px;
  margin: 0 auto;
}

.editor-container {
  width: 100%;
  max-width: 1000px;
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

.usecase-class {
  display: flex;
  flex-direction: row;
  align-items: center;
}

@media (max-width: 768px) {
  .mobile-usecase-class {
    margin-bottom: 3REM;
    flex-direction: column;
  }
}

:deep(.bytemd-fullscreen) {
  z-index: 999 !important;
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 25px;
  margin-top: 20px;
  margin-bottom: 20px;
  background-color: #fff;
  border-bottom: 1px solid #e8e8e8;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  border-radius: 8px;
  flex-shrink: 0; /* Prevent header from shrinking */
}
.back-btn, .share-btn {
  color: #555;
  font-size: 16px;
}
.back-btn:hover, .share-btn:hover {
  background-color: #f0f2f5 !important;
  color: #165dff !important;
}
</style>
