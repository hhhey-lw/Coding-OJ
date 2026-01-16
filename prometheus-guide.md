# Prometheus 可观测性服务部署指南

## 前置条件

- Docker 和 Docker Compose 已安装
- Spring Boot 应用已集成 Actuator 和 Micrometer Prometheus

## 目录结构

```
prometheus/
├── docker-compose.yml
└── prometheus.yml
```

## 1. 创建配置文件

### prometheus.yml

```yaml
# 全局配置
global:
  scrape_interval: 15s      # 每隔 15 秒从目标抓取一次指标数据
  evaluation_interval: 15s  # 每隔 15 秒评估一次告警规则

# 抓取配置：定义 Prometheus 从哪些目标获取指标
scrape_configs:
  # 抓取 Prometheus 自身的指标（用于监控 Prometheus 本身）
  - job_name: 'prometheus'
    static_configs:
      - targets: ['localhost:9090']  # Prometheus 自己的地址

  # 抓取 Coding-OJ 应用的指标
  - job_name: 'coding-oj'
    metrics_path: '/api/actuator/prometheus'  # Spring Boot Actuator 暴露的 Prometheus 端点路径
    static_configs:
      # host.docker.internal 是 Docker 提供的特殊 DNS，用于从容器内访问宿主机
      # 端口需要改成你的 Spring Boot 应用端口，参考 application.yml 中的 server.port 配置
      - targets: ['host.docker.internal:8101']
```

### docker-compose.yml

```yaml
version: '3.8'  # Docker Compose 文件格式版本

services:
  # Prometheus 服务：时序数据库，负责抓取和存储指标数据
  prometheus:
    image: prom/prometheus:latest  # 使用官方最新镜像
    container_name: prometheus     # 容器名称，方便管理
    ports:
      - "9090:9090"  # 映射端口：宿主机9090 -> 容器9090，用于访问 Prometheus Web UI
    volumes:
      # 挂载配置文件：将本地 prometheus.yml 映射到容器内的配置路径
      - ./prometheus.yml:/etc/prometheus/prometheus.yml
      # 数据持久化：将指标数据存储到 Docker 卷，容器重启后数据不丢失
      - prometheus_data:/prometheus
    command:
      - '--config.file=/etc/prometheus/prometheus.yml'  # 指定配置文件路径
      - '--storage.tsdb.path=/prometheus'               # 指定数据存储路径
      - '--web.enable-lifecycle'                        # 启用热重载 API，支持通过 HTTP 请求重载配置
    restart: unless-stopped  # 除非手动停止，否则容器异常退出后自动重启

  # Grafana 服务：可视化面板，用于展示 Prometheus 中的指标数据
  grafana:
    image: grafana/grafana:latest  # 使用官方最新镜像
    container_name: grafana
    ports:
      - "3000:3000"  # Grafana Web UI 端口
    environment:
      # 设置 Grafana 管理员账号密码（生产环境请修改！）
      - GF_SECURITY_ADMIN_USER=admin
      - GF_SECURITY_ADMIN_PASSWORD=admin123
    volumes:
      # 数据持久化：保存 Dashboard、数据源配置等
      - grafana_data:/var/lib/grafana
    depends_on:
      - prometheus  # 依赖 Prometheus，确保 Prometheus 先启动
    restart: unless-stopped

# 定义 Docker 卷，用于数据持久化存储
volumes:
  prometheus_data:  # Prometheus 时序数据
  grafana_data:     # Grafana 配置和面板数据
```

## 2. 启动服务

```bash
# 创建目录
mkdir -p prometheus && cd prometheus

# 创建配置文件后启动
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f
```

## 3. 访问地址

| 服务 | 地址 | 说明 |
|------|------|------|
| Prometheus | http://localhost:9090 | 指标查询和管理 |
| Grafana | http://localhost:3000 | 可视化面板 (admin/admin123) |
| 应用指标 | http://localhost:8101/api/actuator/prometheus | Spring Boot 指标端点 |

## 4. Grafana 配置

1. 登录 Grafana (admin/admin123)
2. 添加数据源: Configuration → Data Sources → Add data source → Prometheus
3. URL 填写: `http://prometheus:9090`
4. 点击 Save & Test
5. 导入 Dashboard: Dashboards → Import → 输入 ID `4701` (JVM Micrometer Dashboard)

## 5. 常用命令

```bash
# 启动服务
docker-compose up -d

# 停止服务
docker-compose down

# 重启服务
docker-compose restart

# 查看日志
docker-compose logs -f prometheus
docker-compose logs -f grafana

# 重新加载 Prometheus 配置 (无需重启)
curl -X POST http://localhost:9090/-/reload
```

## 6. 常用 PromQL 查询

```promql
# JVM 内存使用
jvm_memory_used_bytes{application="Coding-OJ"}

# HTTP 请求总数
http_server_requests_seconds_count{application="Coding-OJ"}

# HTTP 请求平均响应时间
rate(http_server_requests_seconds_sum[5m]) / rate(http_server_requests_seconds_count[5m])

# CPU 使用率
system_cpu_usage{application="Coding-OJ"}
```

## 7. 注意事项

- macOS/Windows 中使用 `host.docker.internal` 访问宿主机
- Linux 中需要使用 `--network=host` 或宿主机 IP
- 生产环境请修改 Grafana 默认密码
