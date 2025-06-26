package com.sky.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * @version 1.0.0
 * @program: sky-take-out
 * @className: MyTask
 * @description: TODO
 * @author: Lin
 * @create: 2025/6/23 07:28
 **/
@Component
@Slf4j
public class MyTask {

//    @Scheduled(cron = "0/5 * * * * ?")
    public void executeTask(){
        log.info("定时任务开始执行: {}", new Date());
    }
}