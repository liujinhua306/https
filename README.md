# httpclient和tomcat进行https双向认证

根据tomat.cer 生成tomat.truststore文件  

C:\Users\20160712\Desktop>keytool -keystore tomcat.truststore -keypass 123456 -torepass 123456 -alias DemoCA -import -trustcacerts -file tomcat.cer



