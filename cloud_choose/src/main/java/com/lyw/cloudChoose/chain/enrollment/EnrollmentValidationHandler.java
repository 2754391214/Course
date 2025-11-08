package com.lyw.cloudChoose.chain.enrollment;

import com.lyw.cloudChoose.chain.ValidationContext;
import com.lyw.cloudChoose.chain.ValidationResult;
import com.lyw.cloudChoose.dto.EnrollmentsDto;
import com.lyw.cloudChoose.dto.CoursesDto;
import com.lyw.cloudChoose.vo.EnrollmentStrategiesVo;

/**
 * 选课验证处理器接口
 */
public interface EnrollmentValidationHandler {

    /**
     * 处理验证
     * @param request 选课请求
     * @param course 课程信息
     * @param strategy 选课策略
     * @param context 验证上下文
     * @return 验证结果
     */
    ValidationResult handle(EnrollmentsDto request, CoursesDto course,
                            EnrollmentStrategiesVo strategy, ValidationContext context);

    /**
     * 设置下一个处理器
     */
    void setNextHandler(EnrollmentValidationHandler nextHandler);
}
