package com.thread0.demo.controller;

import com.thread0.demo.annotation.RateLimiter;
import com.thread0.demo.bean.HttpClientUtil;
import com.thread0.demo.bean.HttpClientUtilBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * TODO
 *
 * @author : yzchen
 * since 2022-02-24,v1.0.0
 */
@Controller
@RestController
@Slf4j
public class TestController {

    @Autowired
    private HttpClientUtil httpClientUtil;

    @GetMapping("/test2")
    public Map test2(String url) {
        httpClientUtil.get(url);
        HashMap<String, Long> resMap = new HashMap<>();
        resMap.put("data", System.currentTimeMillis());
        return resMap;
    }


}
