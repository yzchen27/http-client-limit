package com.thread0.demo;

import lombok.SneakyThrows;

import java.util.Date;

/**
 * 固定窗口限流
 *
 * @author : yzchen
 * since 2022-04-06,v1.0.0
 */
public class TimeWindow {

    public static final Integer DURATION = 1 * 3010; // 时间窗口大小，单位毫秒

    public static final Integer MAX_COUNT = 10; // 允许的最大请求次数

    public static Integer curCount = 0; // 当前请求次数

    public static Long endTime = new Date().getTime(); // 当前时间窗口的开始时间

    public boolean limit() {
        long currentTime = new Date().getTime();

        // 是否已经不在当前时间窗口范围内了
        if (currentTime > endTime) {
            endTime = currentTime + DURATION;
            curCount = 1;
            return true;
        } else {
            // 处于当前时间窗口
            curCount = curCount + 1;
            return curCount < MAX_COUNT;
        }
    }

    @SneakyThrows
    public static void main(String[] args) {
        TimeWindow timeWindow = new TimeWindow();
        Thread.sleep(1000L);
        for (int i = 1; i < 10; i++) {
            System.out.println(timeWindow.limit() + "-" + i);
        }
        Thread.sleep(1000L);
        for (int i = 1; i < 10; i++) {
            System.out.println(timeWindow.limit() + "-" + i);
        }
    }
}

