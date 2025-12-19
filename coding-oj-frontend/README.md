## 根据后台生成代码

```shell
openapi --input http://localhost:8101/api/v2/api-docs --output ./generated --client axios
```

OpenAPI.ts 设置这个
BASE: 'http://localhost:8101',
VERSION: '1.0',
WITH_CREDENTIALS: true,

## Project setup

```
yarn install
```

### Compiles and hot-reloads for development

```
yarn serve
```

### Compiles and minifies for production

```
yarn build
```

### Lints and fixes files

```
yarn lint
```

### Customize configuration

See [Configuration Reference](https://cli.vuejs.org/config/).
