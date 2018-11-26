# httpclient和tomcat进行https双向认证


使用java httpclientAPI进行https访问需要生成p12和truststore结尾的两个文件

首先生成p12文件（首先获取到clinet.crt和client.key两个文件）

openssl pkcs12 -export -clcerts -in ./users/client.crt -inkey ./users/client.key -out ./users/client.p12 

生成客户端信任证书库(由服务端证书生成的证书库)
keytool -import -v -alias server -file server.crt -keystore ../users/client.truststore -storepass 123456

