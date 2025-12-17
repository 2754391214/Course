package com.lyw.cloudCourse.dto;

import com.lyw.cloudCourse.vo.CoursesVo;
import lombok.Data;

import java.util.List;

/**
 * 批量更新单个结果类
 */
@Data
public class BatchUpdateResponseDto {
    private Integer totalCount;
    private Integer successCount;
    private Integer failureCount;
    private List<CoursesVo> results;
}
