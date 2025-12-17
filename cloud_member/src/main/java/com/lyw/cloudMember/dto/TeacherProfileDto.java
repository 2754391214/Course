package com.lyw.cloudMember.dto;

import com.lyw.commonUtil.dto.BaseDto;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * 教师信息表
 * </p>
 *
 * @author lyw
 * @since 2025/11/06
 */
@Data
public class TeacherProfileDto extends BaseDto {

    private Long id;
    
    @ApiModelProperty("用户ID")
    private Long userId;
    
    @ApiModelProperty("工号")
    private String teacherId;

    @ApiModelProperty("姓名")
    private String name;

    @ApiModelProperty("相片")
    private String avatar;

    @ApiModelProperty("职称")
    private String title;
    
    @ApiModelProperty("部门")
    private String department;
    
    @ApiModelProperty("办公室")
    private String office;
    
    @ApiModelProperty("研究方向")
    private String researchField;
    
    @ApiModelProperty("教龄")
    private Integer teachingYears;
    
    @ApiModelProperty("是否是导师")
    private Integer isTutor;
    
    @ApiModelProperty("任职状态:1-在职 2-离职 3-退休")
    private Integer employmentStatus;
                    
}
