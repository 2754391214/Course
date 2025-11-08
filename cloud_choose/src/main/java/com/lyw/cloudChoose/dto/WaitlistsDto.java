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
 * 选课等待列表
 * </p>
 *
 * @author lyw
 * @since 2025/10/22
 */
@Data
@Accessors(chain = true)
public class WaitlistsDto extends BaseDto {

@ApiModelProperty("主键ID")
private Long id;
    
@ApiModelProperty("学生ID")
private Long studentId;
    
@ApiModelProperty("课程ID")
private Long courseId;
    
@ApiModelProperty("在等待列表中的位置")
private Integer position;
    
@ApiModelProperty("加入等待列表时间")
@DateTimeFormat(pattern = "dd-MM-yyyy HH:mm:ss")
@JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss", timezone = "GMT+8")
private Date joinedAt;
    
@ApiModelProperty("状态：WAITING-等待中, OFFERED-已提供席位, EXPIRED-已过期, CANCELLED-已取消")
private String status;
    
@ApiModelProperty("席位过期时间（当状态为OFFERED时）")
@DateTimeFormat(pattern = "dd-MM-yyyy HH:mm:ss")
@JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss", timezone = "GMT+8")
private Date expiresAt;
    
@ApiModelProperty("优先级，用于抽签规则")
private Integer priority;
    
@ApiModelProperty("抽签结果：WIN-中签, LOSE-未中签")
private String lotteryResult;
                    
}
