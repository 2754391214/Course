package com.lyw.cloudThirdPart.aspect;

import com.lyw.cloudThirdPart.service.ThirdPartyProtectManager;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Aspect
@Component
public class ThirdPartyProtectAspect {

    @Autowired
    private ThirdPartyProtectManager protectManager;

    @Pointcut("@annotation(com.lyw.cloudThirdPart.aspect.ThirdPartyProtect)")
    public void pointcut() {
    }

    @Before("pointcut() && @annotation(protect)")
    public void doBefore(JoinPoint joinPoint, ThirdPartyProtect protect) {
        String callerId = getCallerId();
        Map<String, Object> context = extractCallContext(joinPoint);

        boolean allow = protectManager.allow(protect, callerId, context);
        if (!allow) {
            throw new RuntimeException("服务调用频率超限，请稍后重试");
        }
    }

    @AfterReturning("pointcut() && @annotation(protect)")
    public void doAfterReturning(JoinPoint joinPoint, ThirdPartyProtect protect) {
        // 调用成功后记录成本
        String callerId = getCallerId();
        String serviceType = protect.service().name();
        double cost = estimateCallCost(joinPoint, protect);

        protectManager.recordCost(serviceType, callerId, cost);
    }

    private String getCallerId() {
        ServletRequestAttributes requestAttributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes != null) {
            HttpServletRequest request = requestAttributes.getRequest();
            // 优先使用用户ID，其次是IP
            String userId = request.getHeader("X-User-ID");
            if (userId != null) return "user:" + userId;

            return "ip:" + getClientIp(request);
        }
        return "system";
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    private Map<String, Object> extractCallContext(JoinPoint joinPoint) {
        Map<String, Object> context = new HashMap<>();

        // 提取方法参数作为业务上下文
        Object[] args = joinPoint.getArgs();
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof String) {
                context.put("arg" + i, args[i]);
            }
        }

        // 添加时间上下文
        context.put("callTime", System.currentTimeMillis());

        return context;
    }

    private double estimateCallCost(JoinPoint joinPoint, ThirdPartyProtect protect) {
        // 根据服务类型和调用参数估算成本
        switch (protect.service()) {
            case SMS:
                return estimateSmsCost(joinPoint);
            case EMAIL:
                return estimateEmailCost(joinPoint);
            case OSS:
                return estimateOssCost(joinPoint);
            case AI_SERVICE:
                return estimateAiCost(joinPoint);
            default:
                return 0.1; // 默认成本
        }
    }

    private double estimateSmsCost(JoinPoint joinPoint) {
        // 根据短信条数、国际短信等估算成本
        return 0.05; // 默认每条0.05元
    }

    private double estimateEmailCost(JoinPoint joinPoint) {
        // 根据邮件大小、附件等估算成本
        return 0.01; // 默认每封0.01元
    }

    private double estimateOssCost(JoinPoint joinPoint) {
        // 根据文件大小、操作类型估算成本
        return 0.001; // 默认每次操作0.001元
    }

    private double estimateAiCost(JoinPoint joinPoint) {
        // 根据AI服务类型、调用量估算成本
        return 0.1; // 默认每次0.1元
    }
}