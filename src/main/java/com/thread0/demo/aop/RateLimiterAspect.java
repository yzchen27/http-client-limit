package com.thread0.demo.aop;

import cn.hutool.core.date.LocalDateTimeUtil;
import com.thread0.demo.annotation.RateLimiter;
import com.thread0.demo.enums.LimitType;
import com.thread0.demo.util.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 限流处理
 *
 * @author yzChen
 */
@Slf4j
@Aspect
@Component
@AutoConfigureAfter(RedissonClient.class)
public class RateLimiterAspect {

    /**
     * 开始时间
     */
    private long startTime;

    /**
     * 请求url
     */
    private String url;

    /**
     * 统计成功次数
     */
    private Map<String, AtomicInteger> successCountMap = new HashMap<>();
    /**
     * 统计失败次数
     */
    private Map<String, AtomicInteger> failCountMap = new HashMap<>();

    /**
     *  是否初始化map
     */
    private Boolean initMapFlag = true;

    /**
     * 限制列表
     */
    @Nullable
    @Value("#{${third.limiterMap}}")
    private Map<String, String> limiterMap = new HashMap<>();


    public void initMap() {
        if (!CollectionUtils.isEmpty(limiterMap)) {
            limiterMap.keySet().forEach(url -> {
                successCountMap.put(url, new AtomicInteger(0));
                failCountMap.put(url, new AtomicInteger(0));
            });
            this.initMapFlag = false;
            log.info("--------------统计map初始化完成------------------");
        }
    }

    @Before("@annotation(rateLimiter)")
    public void doBefore(JoinPoint point, RateLimiter rateLimiter) {
        startTime = System.currentTimeMillis() / 100;
        // 间隔速率
        int count = rateLimiter.count();
        int time = rateLimiter.time();

        Object[] args = point.getArgs();
        url = args[0].toString();

        String combineKey = getCombineKey(rateLimiter, point);
        // 限流列表
        if (!CollectionUtils.isEmpty(limiterMap) && StringUtils.hasText(url)) {
            if (initMapFlag){
                initMap();
            }
            String value = limiterMap.get(url);
            if (StringUtils.hasText(value)) {
                int todayCount = Integer.parseInt(value);
                try {
                    check(rateLimiter, combineKey + "-today", todayCount, 60);
                } catch (Exception e) {
                    throw new RuntimeException("请求过于频繁！！");
                }

            }

        }
    }

    @AfterReturning(pointcut = "@annotation(rateLimiter)", returning = "result")
    public void doAfterReturn(RateLimiter rateLimiter, Object result) throws Throwable {
        if (Objects.isNull(successCountMap.get(url))){
            return;
        }
        log.info("{url:'{}',responseTime:{},successCount:{},failCount:{},result:'success',time:'{}'}",url,(System.currentTimeMillis() / 100 - startTime), successCountMap.get(url).incrementAndGet(), failCountMap.get(url),LocalDateTimeUtil.formatNormal(LocalDateTimeUtil.now()));
    }

    @AfterThrowing(throwing = "ex", pointcut = "@annotation(rateLimiter)")
    public void doThrowing(Throwable ex, RateLimiter rateLimiter){
        if (Objects.isNull(successCountMap.get(url))){
            return;
        }
        log.info("{url:'{}',responseTime:{},successCount:{},failCount:{},result:'fail',time:'{}'}",url,(System.currentTimeMillis() / 100 - startTime), successCountMap.get(url).get(), failCountMap.get(url).incrementAndGet(),LocalDateTimeUtil.formatNormal(LocalDateTimeUtil.now()));
    }

    public String getCombineKey(RateLimiter rateLimiter, JoinPoint point) {
        StringBuilder stringBuffer = new StringBuilder(rateLimiter.key());
        if (rateLimiter.limitType() == LimitType.CLUSTER) {
            // 获取客户端实例id
            stringBuffer.append(RedisUtils.getClient().getId()).append("-");
        }
        Object[] args = point.getArgs();
        if (args.length > 0) {
            String url = args[0].toString();
            stringBuffer.append(url);
        }

        return stringBuffer.toString();
    }

    private void check(RateLimiter rateLimiter, String combineKey, int count, int time) throws Exception {
        RateType rateType = RateType.OVERALL;
        if (rateLimiter.limitType() == LimitType.CLUSTER) {
            rateType = RateType.PER_CLIENT;
        }
        long number = RedisUtils.rateLimiter(combineKey, rateType, count, time);
        if (number == -1) {
            throw new Exception("访问过于频繁，请稍候再试");
        }
        log.info("限制令牌 => {}, 剩余令牌 => {}, 缓存key => '{}', 调用时间time => {}", count, number, combineKey, LocalDateTimeUtil.formatNormal(LocalDateTimeUtil.now()));
    }
}
