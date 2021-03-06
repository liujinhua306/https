package com.bluedon.soc.dao.siem.http;


import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import java.io.*;
import java.security.KeyStore;

/**
 * 使用 httpclient4.5 进行 https 通讯，
 * 采用双向认证， 连接池管理connection
 *
 * @author liujh
 *
 */
public class HttpsKeyStoreDemo {

    public static HttpClientConnectionManager CONNECTION_MANAGER = null;

    /**
     * 初始化 connection manager.
     * @param keyStoreFile
     * @param keyStorePass
     * @param trustStoreFile
     * @param trustStorePass
     * @throws Exception
     */
    public void init(String keyStoreFile, String keyStorePass,
                     String trustStoreFile, String trustStorePass) throws Exception {
        System.out.println("init conection pool...");

        InputStream ksis = new FileInputStream(new File(keyStoreFile));
        InputStream tsis = new FileInputStream(new File(trustStoreFile));

        KeyStore ks = KeyStore.getInstance("PKCS12");
        ks.load(ksis, keyStorePass.toCharArray());

        KeyStore ts = KeyStore.getInstance("JKS");
        ts.load(tsis, trustStorePass.toCharArray());

        SSLContext sslContext = new SSLContextBuilder().loadKeyMaterial(ks, keyStorePass.toCharArray()).loadTrustMaterial(ts, new TrustSelfSignedStrategy()).build();

        HostnameVerifier hostnameVerifier = NoopHostnameVerifier.INSTANCE;
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext, hostnameVerifier);

        /*SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                sslContext, new String[] { "TLSv1" }, null,
                SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);*/

        Registry<ConnectionSocketFactory> registry = RegistryBuilder
                .<ConnectionSocketFactory> create()
                .register("http", PlainConnectionSocketFactory.INSTANCE)
                .register("https", sslsf).build();
        ksis.close();
        tsis.close();
        CONNECTION_MANAGER = new PoolingHttpClientConnectionManager(registry);

    }

    /**
     * do post
     * @param url
     * @param params
     * @throws Exception
     */
    public void post(String url, String params) throws Exception {
        if (CONNECTION_MANAGER == null) {
            return;
        }
        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(CONNECTION_MANAGER).build();
        HttpPost httpPost = new HttpPost(url);

        httpPost.setEntity(new StringEntity(params,
                ContentType.APPLICATION_JSON));

        CloseableHttpResponse resp = httpClient.execute(httpPost);
        System.out.println(resp.getStatusLine());
        InputStream respIs = resp.getEntity().getContent();
        String content = convertStreamToString(respIs);
        System.out.println(content);
        EntityUtils.consume(resp.getEntity());
    }


    public static String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        // 服务地址
        String url = "https://localhost:8443/SOCWeb/alarm/closeAlarmInterface.action";
        // 服务参数，这里接口的参数采用 json 格式传递
        String params = "{\"merchantCode\": \"www.demo.com\","
                + "\"sessionId\": \"10000011\"," + "\"userName\": \"jack\","
                + "\"idNumber\": \"432652515\"," + "\"cardNo\": \"561231321\","
                + "\"phoneNo\": \"\"}";
        // 私钥证书
        String keyStoreFile = "C:\\Users\\20160712\\Desktop\\clientkey.p12";
        String keyStorePass = "123456";

        // 配置信任证书库及密码
        String trustStoreFile = "C:\\Users\\20160712\\Desktop\\tomcat.truststore";
        String trustStorePass = "123456";

        HttpsKeyStoreDemo obj = new HttpsKeyStoreDemo();
        try {
            obj.init(keyStoreFile, keyStorePass, trustStoreFile, trustStorePass);
                obj.post(url, params);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}