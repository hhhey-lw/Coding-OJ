import { RouteRecordRaw } from "vue-router";
import UserLayout from "@/layouts/UserLayout.vue";
import UserLoginView from "@/views/user/UserLoginView.vue";
import UserRegisterView from "@/views/user/UserRegisterView.vue";
import NoAuthView from "@/views/NoAuthView.vue";
import HomeView from "@/views/HomeView.vue";
import ACCESS_ENUM from "@/access/accessEnum";
import AddQuestionView from "@/views/question/AddQuestionView.vue";
import ManageQuestionView from "@/views/question/ManageQuestionView.vue";
import QuestionsView from "@/views/question/ListQuestionsView.vue";
import QuestionSubmitView from "@/views/question/QuestionSubmitView.vue";
import ViewQuestionView from "@/views/question/QuestionDetailView.vue";
import ViewDiscussion from "@/views/discussion/ListDiscussionView.vue";
import DetailDiscussion from "@/views/discussion/DetailDiscussionView.vue";
import UserInfoView from "@/views/user/UserInfoView.vue";
import AddDicussionView from "@/views/discussion/AddDicussionView.vue";
import {IconComputer, IconHome, IconList, IconPalette, IconSettings} from "@arco-design/web-vue/es/icon";
import TagQuestionListView from "@/views/question/TagQuestionListView.vue";
import ManagerCenterView from "@/views/user/ManagerCenterView.vue";

export const routes: Array<RouteRecordRaw> = [
  {
    path: "/user",
    name: "用户",
    component: UserLayout,
    children: [
      {
        path: "login",
        name: "用户登录",
        component: UserLoginView,
      },
      {
        path: "register",
        name: "用户注册",
        component: UserRegisterView,
      },
    ],
    meta: {
      hideInMenu: true,
    },
  },
  {
    path: "/",
    name: "首页",
    component: HomeView,
    meta: {
      icon: IconHome,
    }
  },
  {
    path: "/questions",
    name: "题库",
    component: QuestionsView,
    meta: {
      icon: IconList,
    }
  },
  {
    path: "/question_submit",
    name: "评测",
    component: QuestionSubmitView,
    meta: {
      icon: IconComputer
    }
  },
  {
    path: "/discussion",
    name: "讨论",
    component: ViewDiscussion,
    meta: {
      icon: IconPalette
    }
  },
  {
    path: "/discussion/detail/:id",
    name: "讨论详情",
    props: true,
    component: DetailDiscussion,
    meta: {
      access: ACCESS_ENUM.USER,
      hideInMenu: true,
    },
  },
  {
    path: "/discussion/add",
    name: "发布帖子",
    component: AddDicussionView,
    meta: {
      hideInMenu: true,
      access: ACCESS_ENUM.USER,
    }
  },
  {
    path: "/view/question/:id",
    name: "在线做题",
    component: ViewQuestionView,
    props: true,
    meta: {
      access: ACCESS_ENUM.USER,
      hideInMenu: true,
    },
  },
  {
    path: "/manager/center",
    name: "管理中心",
    component: ManagerCenterView,
    meta: {
      access: ACCESS_ENUM.ADMIN,
      icon: IconSettings
    },
  },
  {
    path: "/update/question",
    name: "更新题目",
    component: AddQuestionView,
    meta: {
      access: ACCESS_ENUM.ADMIN,
      hideInMenu: true,
    },
  },
  {
    path: "/manage/question/",
    name: "管理题目",
    component: ManageQuestionView,
    meta: {
      access: ACCESS_ENUM.ADMIN,
      hideInMenu: true,
    },
  },
  {
    path: "/noAuth",
    name: "无权限",
    component: NoAuthView,
    meta: {
      hideInMenu: true,
    }
  },
  {
    path: "/infoCenter",
    name: "个人中心",
    component: UserInfoView,
    meta: {
      hideInMenu: true,
      access: ACCESS_ENUM.USER,
    }
  },{
    path: "/questions/tag/:tagId",
    name: "标签题集",
    component: TagQuestionListView,
    meta: {
      hideInMenu: true,
      access: ACCESS_ENUM.USER,
    }
}
];
