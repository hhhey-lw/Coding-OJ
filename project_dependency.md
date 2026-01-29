### Rabbit MQ Installation
```yaml
docker run -d --name coding-oj-mq \
  -p 25672:5672 \
  -p 35672:15672 \
  -v /your/local/data:/var/lib/rabbitmq \
  -e RABBITMQ_DEFAULT_USER=coding-oj \
  -e RABBITMQ_DEFAULT_PASS=coding-oj \
  rabbitmq:3.13.6-management-alpine
```


### Docker Cli 
> 运行以下脚本生成 TLS 证书，用于 Docker 客户端和服务器之间的安全通信。
> 将(tls-client-certs-docker下的)ca-docker, cert-docker,
> key-docker三个文件放到Boot项目的resource/tls-client-certs-docker目录下即可，注意重命名，去掉每个文件的 '-docker' 。
```shell
#!/bin/bash
#
# -------------------------------------------------------------
# 自动创建 Docker TLS 证书
# -------------------------------------------------------------
# 以下是配置信息
# --[BEGIN]------------------------------
CODE="docker"

# 本机IP
IP="121.40.252.207"

PASSWORD="wl070919"
COUNTRY="CN"
STATE="ZHEJIANG"
CITY="HANGZHOU"
ORGANIZATION="WeiLong"
ORGANIZATIONAL_UNIT="Dev"
COMMON_NAME="$IP"
EMAIL="1410124534@qq.com"
# --[END]--
# Generate CA key
openssl genrsa -aes256 -passout "pass:$PASSWORD" -out "ca-key-$CODE.pem" 4096
# Generate CA
openssl req -new -x509 -key "ca-key-$CODE.pem" -sha256 -out "ca-$CODE.pem" -passin "pass:$PASSWORD" -subj "/C=$COUNTRY/ST=$STATE/L=$CITY/O=$ORGANIZATION/OU=$ORGANIZATIONAL_UNIT/CN=$COMMON_NAME/emailAddress=$EMAIL"
# Generate Server key
openssl genrsa -out "server-key-$CODE.pem" 4096
# Generate Server Certs.
openssl req -subj "/CN=$COMMON_NAME" -sha256 -new -key "server-key-$CODE.pem" -out server.csr
echo "subjectAltName = IP:$IP,IP:127.0.0.1" >> extfile.cnf
echo "extendedKeyUsage = serverAuth" >> extfile.cnf
openssl x509 -req -sha256 -in server.csr -passin "pass:$PASSWORD" -CA "ca-$CODE.pem" -CAkey "ca-key-$CODE.pem" -CAcreateserial -out "server-cert-$CODE.pem" -extfile extfile.cnf
# Generate Client Certs.
rm -f extfile.cnf
openssl genrsa -out "key-$CODE.pem" 4096
openssl req -subj '/CN=client' -new -key "key-$CODE.pem" -out client.csr
echo extendedKeyUsage = clientAuth >> extfile.cnf
openssl x509 -req -sha256 -in client.csr -passin "pass:$PASSWORD" -CA "ca-$CODE.pem" -CAkey "ca-key-$CODE.pem" -CAcreateserial -out "cert-$CODE.pem" -extfile extfile.cnf
rm -vf client.csr server.csr
chmod -v 0400 "ca-key-$CODE.pem" "key-$CODE.pem" "server-key-$CODE.pem"
chmod -v 0444 "ca-$CODE.pem" "server-cert-$CODE.pem" "cert-$CODE.pem"
# 打包客户端证书
mkdir -p "tls-client-certs-$CODE"
cp -f "ca-$CODE.pem" "cert-$CODE.pem" "key-$CODE.pem" "tls-client-certs-$CODE/"
```

```bash
vim /lib/systemd/system/docker.service

ExecStart=/usr/bin/dockerd --containerd=/run/containerd/containerd.sock -H unix:///var/run/docker.sock -H tcp://0.0.0.0:2376 --tlsverify --tlscacert=/root/app/docker_connect_certs/ca-docker.pem --tlscert=/root/app/docker_connect_certs/server-cert-docker.pem --tlskey=/root/app/docker_connect_certs/server-key-docker.pem
```

记得重启docker
sudo systemctl restart docker