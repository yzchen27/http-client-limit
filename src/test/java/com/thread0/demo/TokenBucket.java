package com.thread0.demo;

import com.google.common.util.concurrent.RateLimiter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

/**
 * 令牌桶算法的限流器
 *
 * @author : yzchen
 * since 2022-04-06,v1.0.0
 */
@Slf4j
public class TokenBucket {

    public static void main(String[] args) {
//        new SmoothRater().start();
        new PreHotRater().start();
    }
}

@Slf4j
class SmoothRater {

    @SneakyThrows
    public void start() {
        /**
         *  创建令牌数 每秒4个
         *  平滑突发限流：一次性(1s)往桶里放n个,消费完以后每 1/令牌数的速率进桶 0.25s一个令牌
         */
        RateLimiter rateLimiter = RateLimiter.create(4);
        log.info("令牌桶准备中.....");
        Thread.sleep(2000L);
        while (true) {
            String time = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME);
            log.info("加令牌的速度1:{}s,time={}", rateLimiter.acquire(), time);
            log.info("加令牌的速度2:{}s,time={}", rateLimiter.acquire(), time);
            log.info("加令牌的速度3:{}s,time={}", rateLimiter.acquire(), time);
            log.info("加令牌的速度4:{}s,time={}", rateLimiter.acquire(), time);

        }
    }
}

@Slf4j
class PreHotRater {


    @SneakyThrows
    public void start() {
        /**
         *  创建令牌数 每秒4个
         *  平滑预热限流
         *  0-2s内放置 3个令牌，后续每个令牌的速率 1/令牌数一个
         *
         */
        RateLimiter rateLimiter = RateLimiter.create(4, 2 , TimeUnit.SECONDS);
        log.info("令牌桶准备中.....");
        Thread.sleep(5000L);
        while (true) {
            String time = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME);
            log.info("加令牌的速度-1:{}s,time={}", rateLimiter.acquire(), time);
            log.info("加令牌的速度-2:{}s,time={}", rateLimiter.acquire(), time);
            log.info("加令牌的速度-3:{}s,time={}", rateLimiter.acquire(), time);
            log.info("加令牌的速度-4:{}s,time={}", rateLimiter.acquire(), time);
        }

    }
}
