package com.lyw.cloudMember.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lyw.commonUtil.dto.BaseDto;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.util.Date;

/**
 * <p>
 * 学生信息表
 * </p>
 *
 * @author lyw
 * @since 2025/11/06
 */
@Data
public class StudentProfileDto extends BaseDto {

    private Long id;
    
    @ApiModelProperty("用户ID")
    private Long userId;

    @ApiModelProperty("姓名")
    private String name;

    @ApiModelProperty("相片")
    private String avatar;

    @ApiModelProperty("学号")
    private String studentId;
    
    @ApiModelProperty("年级")
    private String grade;
    
    @ApiModelProperty("专业")
    private String major;
    
    @ApiModelProperty("学院")
    private String faculty;
    
    @ApiModelProperty("入学年份")
    @DateTimeFormat(pattern = "dd-MM-yyyy")
    @JsonFormat(pattern = "dd/MM/yyyy", timezone = "GMT+8")
    private Date enrollmentYear;
    
    @ApiModelProperty("预计毕业年份")
    @DateTimeFormat(pattern = "dd-MM-yyyy")
    @JsonFormat(pattern = "dd/MM/yyyy", timezone = "GMT+8")
    private Date expectedGraduation;
    
    @ApiModelProperty("绩点")
    private BigDecimal gpa;
    
    @ApiModelProperty("已获学分")
    private Integer creditsEarned;
    
    @ApiModelProperty("学业状态:1-在读 2-休学 3-毕业")
    private Integer academicStatus;
                    
}
