package com.thread0.demo.bean;

import com.thread0.demo.annotation.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.Map;

/**
 * http 工具类
 *
 * 限流和限速使用规则：
 *
 *
 * @author linzp
 * @since 2022-01-28, v1.0.0
 */
@Slf4j
public class HttpClientUtil {
    static final int timeOut = 10 * 1000;
    /**
     * 失败尝试次数,3次
     */
    private final static int MAX_RETRY = 3;

    private final static int POOL_DEFAULT_MAX_TOTAL = 10;
    private final static int POOL_DEFAULT_MAX_PER_ROUTER = 10;

    private CloseableHttpClient httpClient;
    private HttpConfig config = new HttpConfig();
    private RequestConfig requestConfig;


    private final static Object syncLock = new Object();

    public HttpClientUtil(CloseableHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public void setRequestConfig(RequestConfig requestConfig) {
        this.requestConfig = requestConfig;
    }




    /**
     * post 请求
     *
     * @param url 请求url
     * @param data 提交body数据
     * @param headerMap 头参数
     * @return
     * @throws IOException
     */
    @RateLimiter
    public String post(String url, String data, Map<String, String> headerMap) throws IOException {
        HttpPost httpPost = new HttpPost(url);
        httpPost.setConfig(requestConfig);

        if (headerMap != null) {
            for(Map.Entry<String, String> entry : headerMap.entrySet()){
                httpPost.setHeader(entry.getKey(), entry.getValue());
            }
        }
        if (null != data && !"".equals(data)) {
            httpPost.setEntity(new ByteArrayEntity(data.getBytes("utf-8")));
        }
        CloseableHttpResponse response = null;
        try {
            response = httpClient.execute(httpPost, HttpClientContext.create());
            int statusCode = response.getStatusLine().getStatusCode();
            if(statusCode != HttpStatus.SC_OK) {
                log.warn("status warning: {}, {}", statusCode, url);
            }
            HttpEntity entity = response.getEntity();
            return EntityUtils.toString(entity, "utf-8");
        } catch (Exception e) {
            log.error("httpclient post failed, ioException: {}", e.getMessage());
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                log.error("httpclient post method close failed, ioException: {}", e.getMessage());
            }
        }
        return null;
    }
    @RateLimiter
    public String get(String url) {
        HttpGet httpGet = new HttpGet(url);
        httpGet.setConfig(requestConfig);
        CloseableHttpResponse response = null;
        try {
            response = httpClient.execute(httpGet, HttpClientContext.create());
            HttpEntity entity = response.getEntity();
            int statusCode = response.getStatusLine().getStatusCode();
            if(statusCode != HttpStatus.SC_OK) {
                log.warn("status warning: {}, {}", statusCode, url);
            }
            String result = EntityUtils.toString(entity, "utf-8");
            EntityUtils.consume(entity);
            return result;
        } catch (IOException e) {
            log.error("httpclient get failed, ioException: {}", e.getMessage());
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                log.error("httpclient get method close failed, ioException: {}", e.getMessage());
            }
        }
        return null;
    }

}
