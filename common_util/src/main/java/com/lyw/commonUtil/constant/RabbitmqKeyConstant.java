package com.lyw.commonUtil.constant;

public class RabbitmqKeyConstant {
    // 选课相关交换器和路由键
    public static final String ENROLLMENT_EXCHANGE = "enrollment.exchange";
    public static final String ENROLLMENT_ROUTING_KEY = "enrollment.routing.key";
    public static final String ENROLLMENT_QUEUE = "enrollment.queue";
    public static final String ENROLLMENT_DLX_EXCHANGE = "enrollment.dlx.exchange";
    public static final String ENROLLMENT_DLX_ROUTING_KEY = "enrollment.dlx.routing.key";
    public static final String ENROLLMENT_DLX_QUEUE = "enrollment.dlx.queue";


    // 用户行为相关交换器和路由键
    public static final String USER_BEHAVIOR_EXCHANGE = "user.behavior.exchange";
    public static final String USER_BEHAVIOR_ROUTING_KEY = "user.behavior.routing.key";
    public static final String USER_BEHAVIOR_QUEUE = "user.behavior.queue";

    // 死信队列配置
    public static final String USER_BEHAVIOR_DLQ_EXCHANGE = "user.behavior.dlq.exchange";
    public static final String USER_BEHAVIOR_DLQ_QUEUE = "user.behavior.dlq.queue";
    public static final String USER_BEHAVIOR_DLQ_ROUTING_KEY = "user.behavior.dlq.routing.key";


    // 排行榜相关交换器和路由键
    public static final String COURSE_HEAT_EXCHANGE = "course.heat.exchange";
    public static final String COURSE_HEAT_ROUTING_KEY_PREFIX = "course.heat.";
    public static final String COURSE_HEAT_ROUTING_KEY = "course.heat.#";
    public static final String COURSE_HEAT_QUEUE = "course.heat.queue";
    public static final String COURSE_HEAT_DLQ = "course.heat.dlq";
}
