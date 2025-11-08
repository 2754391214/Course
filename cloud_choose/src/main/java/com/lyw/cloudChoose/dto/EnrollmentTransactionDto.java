package com.lyw.cloudChoose.dto;

import com.lyw.commonUtil.dto.BaseDto;
import io.swagger.annotations.ApiModelProperty;
import java.util.Date;
import org.springframework.format.annotation.DateTimeFormat;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * <p>
 * 选课本地事务表，用于保证分布式环境下选课操作的最终一致性。记录选课、退课等操作的事务状态，支持重试机制、事务追踪和数据一致性检查。
 * </p>
 *
 * @author lyw
 * @since 2025/10/27
 */
@Data
@Accessors(chain = true)
public class EnrollmentTransactionDto extends BaseDto {

    @ApiModelProperty("主键ID，自增，唯一标识每条事务记录")
    private Long id;
    
    @ApiModelProperty("全局事务ID，系统生成的唯一标识，格式：TXN_时间戳_UUID，用于跨系统事务追踪")
    private String transactionId;
    
    @ApiModelProperty("关联的选课记录ID，外键关联enrollments表的id字段，标识此事务对应的选课操作")
    private Long enrollmentId;
    
    @ApiModelProperty("学生ID，关联用户表，标识参与此次选课事务的学生")
    private Long studentId;
    
    @ApiModelProperty("课程ID，关联课程表，标识此次选课事务涉及的课程")
    private Long courseId;
    
    @ApiModelProperty("操作类型：ENROLL-选课, DROP-退课, UPDATE-更新, BATCH_ENROLL-批量选课，用于区分不同的业务操作")
    private String operationType;
    
    @ApiModelProperty("事务状态：INIT-初始, PROCESSING-处理中, SUCCESS-成功, FAILED-失败, DEGRADED-降级, FINAL_FAILURE-最终失败，记录事务的完整生命周期")
    private String transactionStatus;
    
    @ApiModelProperty("重试次数，记录该事务已经尝试重试的次数，用于指数退避算法计算下次重试时间")
    private Integer retryCount;
    
    @ApiModelProperty("最大重试次数，当事务重试达到此值时将标记为最终失败，防止无限重试")
    private Integer maxRetryCount;
    
    @ApiModelProperty("下次重试时间，基于指数退避算法计算得出，NULL表示无需重试或等待首次重试")
    @DateTimeFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss", timezone = "GMT+8")
    private Date nextRetryTime;
    
    @ApiModelProperty("请求数据，JSON格式存储完整的选课请求信息，包括学生信息、课程信息、选课类型、时间戳等，用于重试和问题排查")
    private String requestData;
    
    @ApiModelProperty("响应数据，JSON格式存储远程调用的响应结果，包括成功/失败信息、错误码、处理时间等，用于结果追踪")
    private String responseData;
    
    @ApiModelProperty("错误信息，详细记录事务失败的原因、异常堆栈、错误码等，便于问题定位和修复")
    private String errorMessage;
                    
}
