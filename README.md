# httpclient和tomcat进行https双向认证


使用java httpclientAPI进行https访问需要生成p12和truststore结尾的两个文件

首先生成p12文件（首先获取到clinet.crt和client.key两个文件）

openssl pkcs12 -export -clcerts -in ./users/client.crt -inkey ./users/client.key -out ./users/client.p12 

再生成truststore结尾文件
keytool -keystore tomcat.truststore -keypass 123456 -storepass 123456 -alias CA -import -trustcacerts -file client.crt


ps:java调用ssl请求必须将证书转换成jks格式或者keystore格式，受信任的证书为truststore文件。

