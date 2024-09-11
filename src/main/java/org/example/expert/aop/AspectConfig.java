package org.example.expert.aop;


import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

@Slf4j(topic = "AspectConfig")
@Aspect
@RequiredArgsConstructor
public class AspectConfig {

    // PointCut
    @Pointcut("@annotation(org.example.expert.domain.common.annotation.AdminCheckLog)")
    private void adminCheckLogPointcut(){}

    // Advice
    @Before("adminCheckLogPointcut()")
    public void adminCheckLogBefore(){
        Date date = new Date();
        String curDate = new SimpleDateFormat("yyyy년 MM월 dd일 HH시 mm분 ss초").format(date);
        try {
            // Before
            log.info("::: ADMIN 접속 :::");
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            log.info("요청한 사용자의 id : {}",request.getAttribute("userId"));
            log.info("API 요청 시각 : {}",curDate);
            log.info("API 요청 URL : {}",request.getRequestURL());
        } finally {
            // After
        }

    }
}
