package com.thread0.demo.bean;

import com.thread0.demo.annotation.RateLimiter;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import java.io.InterruptedIOException;
import java.net.UnknownHostException;

public class HttpClientUtilBuilder {

    private HttpConfig httpConfig = new HttpConfig();
    private RequestConfig.Builder requestConfigBuilder = RequestConfig.custom();

    private HttpClientUtilBuilder() {

    }

    public static HttpClientUtilBuilder builder() {
        return new HttpClientUtilBuilder();
    }

    /**
     * 建立连接超时时间
     *
     * @param connectTimeout
     * @return
     */
    public HttpClientUtilBuilder connectTimeout(int connectTimeout) {
        requestConfigBuilder.setConnectTimeout(connectTimeout);
        return this;
    }

    /**
     * 从连接池获取到连接的超时时间
     *
     * @param connectRequestTimeout
     * @return
     */
    public HttpClientUtilBuilder connectionRequestTimeout(int connectRequestTimeout) {
        requestConfigBuilder.setConnectionRequestTimeout(connectRequestTimeout);
        return this;
    }

    /**
     * 客户端服务端数据交互时间, 两个数据包之间的时间大于该时间认为超时
     *
     * @param socketTimeout
     * @return
     */
    public HttpClientUtilBuilder socketTimeout(int socketTimeout) {
        requestConfigBuilder.setSocketTimeout(socketTimeout);
        return this;
    }

    /**
     * 设置连接池最大连接数
     *
     * @param poolMaxTotal
     * @return
     */
    public HttpClientUtilBuilder poolMaxTotal(int poolMaxTotal) {
        httpConfig.setPoolMaxTotal(poolMaxTotal);
        return this;
    }

    /**
     * 设置每个路由最大连接数
     *
     * @param poolMaxPerRouter
     * @return
     */
    public HttpClientUtilBuilder poolMaxPerRouter(int poolMaxPerRouter) {
        httpConfig.setPoolMaxPerRouter(poolMaxPerRouter);
        return this;
    }

    /**
     * 设置最大失败重试次数
     *
     * @param retryTime
     * @return
     */
    public HttpClientUtilBuilder retryTime(int retryTime) {
        httpConfig.setRetryTime(retryTime);
        return this;
    }

    public HttpClientUtil build() {
        ConnectionSocketFactory plainsf = PlainConnectionSocketFactory.getSocketFactory();
        LayeredConnectionSocketFactory sslsf = SSLConnectionSocketFactory.getSocketFactory();
        Registry<ConnectionSocketFactory> registry = RegistryBuilder
                .<ConnectionSocketFactory>create().register("http", plainsf).register("https", sslsf).build();
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(registry);
        // 将最大连接数增加
        cm.setMaxTotal(httpConfig.getPoolMaxTotal());
        // 将每个路由基础的连接增加
        cm.setDefaultMaxPerRoute(httpConfig.getPoolMaxPerRouter());
        CloseableHttpClient closeableHttpClient = HttpClients.custom()
                .setConnectionManager(cm)
                .setRetryHandler(getRetryHandler()).build();

        HttpClientUtil httpClientUtil = new HttpClientUtil(closeableHttpClient);
        httpClientUtil.setRequestConfig(requestConfigBuilder.build());

        return httpClientUtil;
    }

    public HttpRequestRetryHandler getRetryHandler() {
        return (exception, executionCount, context) -> {
            // 如果已经重试了3次，就放弃
            if (executionCount >= httpConfig.getRetryTime()) {
                return false;
            }
            if (exception instanceof NoHttpResponseException) {// 如果服务器丢掉了连接，那么就重试
                return true;
            }
            if (exception instanceof SSLHandshakeException) {// 不要重试SSL握手异常
                return false;
            }
            if (exception instanceof ConnectTimeoutException) {// 连接被拒绝
                return false;
            }
            if (exception instanceof InterruptedIOException) {// 超时
                return false;
            }
            if (exception instanceof UnknownHostException) {// 目标服务器不可达
                return false;
            }
            if (exception instanceof SSLException) {// SSL握手异常
                return false;
            }

            HttpClientContext clientContext = HttpClientContext.adapt(context);
            HttpRequest request = clientContext.getRequest();

            // 如果请求是幂等的，就再次尝试
            return !(request instanceof HttpEntityEnclosingRequest);
        };
    }

}
