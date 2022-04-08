package com.thread0.demo.bean;

import lombok.Data;

/**
 * http 请求配置
 *
 * @author linzp
 * @since 2022-01-28, v1.0.0
 */
@Data
public class HttpConfig {
    /**
     * 线程池最大连接数
     */
    private int poolMaxTotal = 20;

    /**
     * 每个请求路由最大连接数
     */
    private int poolMaxPerRouter = 5;

    /**
     * 失败重试次数, 默认1不重试
     */
    private int retryTime = 1;
}
