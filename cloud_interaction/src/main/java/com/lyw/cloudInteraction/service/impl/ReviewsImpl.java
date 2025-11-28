package com.lyw.cloudInteraction.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyw.cloudInteraction.dto.ReviewsDto;
import com.lyw.cloudInteraction.mapper.ReviewRepliesDao;
import com.lyw.cloudInteraction.mapper.ReviewsDao;
import com.lyw.cloudInteraction.service.HeatEventPublisher;
import com.lyw.cloudInteraction.service.ReviewsBo;
import com.lyw.cloudInteraction.vo.ReviewRepliesVo;
import com.lyw.cloudInteraction.vo.ReviewsVo;
import com.lyw.commonUtil.constant.CommonKeyConstant;
import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;
import com.lyw.commonUtil.util.BeanConverter;
import com.lyw.commonUtil.util.CurUserUtil;
import com.lyw.commonUtil.util.DateTimeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 评价表 服务类
 * </p>
 *
 * @author lyw
 * @since 2025/10/29
 */
@Slf4j
@Service
public class ReviewsImpl extends ServiceImpl<ReviewsDao, ReviewsVo> implements ReviewsBo {
    @Resource
    private ReviewRepliesDao reviewRepliesDao;
    @Resource
    private HeatEventPublisher heatEventPublisher;
    @Override
    public CourseResponseWrapper getReviewsByCourseId(ReviewsDto dto) {
        Long courseId = dto.getCourseId();
        try {
            Page<ReviewsVo> pageParam = new Page<>(dto.getPageNo(), dto.getPageSize());

            LambdaQueryWrapper<ReviewsVo> queryWrapper = new LambdaQueryWrapper<ReviewsVo>()
                    .eq(ReviewsVo::getCourseId, courseId)
                    .eq(ReviewsVo::getStatus, "approved") // 只查询已审核的评价
                    .orderByDesc(ReviewsVo::getCrd);

            Page<ReviewsVo> result = baseMapper.selectPage(pageParam, queryWrapper);

            // 处理匿名评价
            List<ReviewsVo> processedReviews = result.getRecords().stream()
                    .map(this::processAnonymousReview)
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("list", processedReviews);
            response.put("total", result.getTotal());
            response.put("page", result.getCurrent());
            response.put("size", result.getSize());

            return CourseResponseWrapper.getSuccess(response);
        } catch (Exception e) {
            log.error("根据课程ID获取评价列表失败: courseId={}", courseId, e);
            return CourseResponseWrapper.getFailed("获取评价列表失败");
        }
    }

    @Override
    public CourseResponseWrapper getReviewsByStudentId(ReviewsDto dto) {
        Long studentId = dto.getStudentId();
        try {
            Page<ReviewsVo> pageParam = new Page<>(dto.getPageNo(), dto.getPageSize());
            LambdaQueryWrapper<ReviewsVo> queryWrapper = new LambdaQueryWrapper<ReviewsVo>()
                    .eq(ReviewsVo::getStudentId, studentId)
                    .orderByDesc(ReviewsVo::getCrd);

            Page<ReviewsVo> result = baseMapper.selectPage(pageParam, queryWrapper);

            Map<String, Object> response = new HashMap<>();
            response.put("list", result.getRecords());
            response.put("total", result.getTotal());
            response.put("page", result.getCurrent());
            response.put("size", result.getSize());

            return CourseResponseWrapper.getSuccess(response);
        } catch (Exception e) {
            log.error("根据学生ID获取评价列表失败: studentId={}", studentId, e);
            return CourseResponseWrapper.getFailed("获取评价列表失败");
        }
    }

    @Override
    public CourseResponseWrapper getCourseReviewStatistics(Long courseId) {
        try {
            // 获取评价统计
            ReviewsVo statistics = baseMapper.selectReviewStatistics(courseId);
            if (ObjectUtil.isEmpty(statistics)) {
                statistics.setTotalReviews(0);
                statistics.setAverageRating(BigDecimal.valueOf(0));
                statistics.setRecommendRate(BigDecimal.valueOf(0));
            }

            // 获取评分分布
            List<ReviewsVo> ratingDistribution = baseMapper.selectRatingDistribution(courseId);
            statistics.setRatingDistribution(ratingDistribution);

//            // 获取标签统计
//            List<ReviewsVo> tagStatistics = baseMapper.selectTagStatistics(courseId);
//            statistics.setTagStatistics(tagStatistics);

            return CourseResponseWrapper.getSuccess(statistics);
        } catch (Exception e) {
            log.error("获取课程评价统计失败: courseId={}", courseId, e);
            return CourseResponseWrapper.getFailed("获取评价统计失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CourseResponseWrapper submitReview(ReviewsDto dto) {
        try {
            // 检查学生是否已经评价过该课程
            LambdaQueryWrapper<ReviewsVo> checkWrapper = new LambdaQueryWrapper<ReviewsVo>()
                    .eq(ReviewsVo::getCourseId, dto.getCourseId())
                    .eq(ReviewsVo::getStudentId, dto.getStudentId());

            long existingCount = baseMapper.selectCount(checkWrapper);
            if (existingCount > 0) {
                return CourseResponseWrapper.getFailed("您已经评价过该课程");
            }

            // 验证评分范围
            if (dto.getOverallRating().compareTo(BigDecimal.ONE) < 0 ||
                    dto.getOverallRating().compareTo(new BigDecimal("5")) > 0) {
                return CourseResponseWrapper.getFailed("评分必须在1-5分之间");
            }

            // 设置默认状态
            if (dto.getStatus() == null) {
                dto.setStatus("pending");
            }

            if (dto.getAnonymous() == null) dto.setAnonymous(false);

            // 保存评价
            ReviewsVo reviewsVo = BeanConverter.dtoToVo(dto,ReviewsVo.class);
            reviewsVo.setCrdAndLud(DateTimeUtils.getCurrentDateTime());
            reviewsVo.setCruAndLuu(CurUserUtil.getUserId());

            int result = baseMapper.insert(reviewsVo);
            if (result > 0) {

                heatEventPublisher.publishEvent(CommonKeyConstant.COMMENT,dto.getCourseId(),CommonKeyConstant.COMMENT,reviewsVo.getRating());

                return CourseResponseWrapper.getSuccess("评价提交成功，等待审核");
            } else {
                return CourseResponseWrapper.getFailed("评价提交失败");
            }
        } catch (Exception e) {
            log.error("提交评价失败: {}", dto, e);
            return CourseResponseWrapper.getFailed("评价提交失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CourseResponseWrapper updateReview(ReviewsDto dto) {
        try {
            // 验证评价是否存在且属于该学生
            ReviewsVo existingReview = baseMapper.selectById(dto.getId());
            if (ObjectUtil.isEmpty(existingReview)) {
                return CourseResponseWrapper.getFailed("评价不存在");
            }

            if (!existingReview.getStudentId().equals(dto.getStudentId())) {
                return CourseResponseWrapper.getFailed("无权修改此评价");
            }

            // 只能修改待审核或已审核的评价
            if ("rejected".equals(existingReview.getStatus()) || "hidden".equals(existingReview.getStatus())) {
                return CourseResponseWrapper.getFailed("该评价无法修改");
            }

            // 更新后状态重置为待审核
            dto.setStatus("pending");

            ReviewsVo updateVo = BeanConverter.dtoToVo(dto,ReviewsVo.class);
            updateVo.setLuu(CurUserUtil.getUserId())
                    .setLud(DateTimeUtils.getCurrentDateTime());
            int result = baseMapper.updateById(updateVo);
            if (result > 0) {
                return CourseResponseWrapper.getSuccess("评价更新成功，等待重新审核");
            } else {
                return CourseResponseWrapper.getFailed("评价更新失败");
            }
        } catch (Exception e) {
            log.error("更新评价失败: {}", dto, e);
            return CourseResponseWrapper.getFailed("评价更新失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CourseResponseWrapper deleteReview(Long reviewId, Long studentId) {
        try {
            // 验证评价是否存在且属于该学生
            ReviewsVo existingReview = baseMapper.selectById(reviewId);
            if (ObjectUtil.isEmpty(existingReview)) {
                return CourseResponseWrapper.getFailed("评价不存在");
            }

            if (!existingReview.getStudentId().equals(studentId)) {
                return CourseResponseWrapper.getFailed("无权删除此评价");
            }

            // 逻辑删除评价
            LambdaUpdateWrapper<ReviewsVo> updateWrapper = new LambdaUpdateWrapper<ReviewsVo>()
                    .eq(ReviewsVo::getId, reviewId)
                    .set(ReviewsVo::getStatus, "hidden")
                    .set(ReviewsVo::getLuu, CurUserUtil.getUserId())
                    .set(ReviewsVo::getLud, DateTimeUtils.getCurrentDateTime());

            int result = baseMapper.update(null, updateWrapper);
            if (result > 0) {
                return CourseResponseWrapper.getSuccess("评价删除成功");
            } else {
                return CourseResponseWrapper.getFailed("评价删除失败");
            }
        } catch (Exception e) {
            log.error("删除评价失败: reviewId={}, studentId={}", reviewId, studentId, e);
            return CourseResponseWrapper.getFailed("评价删除失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CourseResponseWrapper auditReview(Long reviewId, String status, String auditRemark) {
        try {
            ReviewsVo existingReview = baseMapper.selectById(reviewId);
            if (ObjectUtil.isEmpty(existingReview)) {
                return CourseResponseWrapper.getFailed("评价不存在");
            }

            // 验证状态值
            if (!Arrays.asList("approved", "rejected", "hidden").contains(status)) {
                return CourseResponseWrapper.getFailed("无效的状态值");
            }
            String currentDateTime = DateTimeUtils.getCurrentDateTime();
            LambdaUpdateWrapper<ReviewsVo> updateWrapper = new LambdaUpdateWrapper<ReviewsVo>()
                    .eq(ReviewsVo::getId, reviewId)
                    .set(ReviewsVo::getStatus, status)
                    .set(ReviewsVo::getLuu, CurUserUtil.getUserId())
                    .set(ReviewsVo::getLud, currentDateTime);

            // 如果有审核备注，可以存储在metadata字段中
            if (StrUtil.isNotEmpty(auditRemark)) {
                Object metadata = JSON.parse(existingReview.getMetadata());
                Map<String, Object> metadataMap = new HashMap<>();
                if (ObjectUtil.isNotEmpty(metadata)) {
                    metadataMap = (Map<String, Object>) metadata;
                }
                metadataMap.put("auditRemark", auditRemark);
                metadataMap.put("auditTime", currentDateTime);
                // 将metadataMap转换为JSON字符串
                 updateWrapper.set(ReviewsVo::getMetadata, JSON.toJSONString(metadataMap));
            }

            int result = baseMapper.update(null, updateWrapper);
            if (result > 0) {
                return CourseResponseWrapper.getSuccess("审核操作成功");
            } else {
                return CourseResponseWrapper.getFailed("审核操作失败");
            }
        } catch (Exception e) {
            log.error("审核评价失败: reviewId={}, status={}", reviewId, status, e);
            return CourseResponseWrapper.getFailed("审核操作失败");
        }
    }

    @Override
    public CourseResponseWrapper checkReviewExists(Long courseId, Long studentId) {
        try {
            LambdaQueryWrapper<ReviewsVo> queryWrapper = new LambdaQueryWrapper<ReviewsVo>()
                    .eq(ReviewsVo::getCourseId, courseId)
                    .eq(ReviewsVo::getStudentId, studentId)
                    .ne(ReviewsVo::getStatus, "hidden"); // 排除已隐藏的评价

            long count = baseMapper.selectCount(queryWrapper);

            Map<String, Object> result = new HashMap<>();
            result.put("exists", count > 0);
            if (count > 0) {
                ReviewsVo review = baseMapper.selectOne(queryWrapper);
                result.put("reviewId", review.getId());
                result.put("status", review.getStatus());
            }

            return CourseResponseWrapper.getSuccess(result);
        } catch (Exception e) {
            log.error("检查评价是否存在失败: courseId={}, studentId={}", courseId, studentId, e);
            return CourseResponseWrapper.getFailed("检查失败");
        }
    }

    @Override
    public CourseResponseWrapper getReviewDetail(Long reviewId) {
        try {
            ReviewsVo review = getBaseMapper().selectById(reviewId);
            if (review == null) {
                return CourseResponseWrapper.getFailed("评价不存在");
            }

            // 处理匿名评价
            review = processAnonymousReview(review);

            // 获取回复数量
            LambdaQueryWrapper<ReviewRepliesVo> replyWrapper = new LambdaQueryWrapper<>();
            replyWrapper.eq(ReviewRepliesVo::getReviewId, reviewId)
                    .eq(ReviewRepliesVo::getStatus, "approved");
            long replyCount = reviewRepliesDao.selectCount(replyWrapper);

            // 构建详情信息
            Map<String, Object> detail = new HashMap<>();
            detail.put("review", review);
            detail.put("replyCount", replyCount);

            return CourseResponseWrapper.getSuccess(detail);
        } catch (Exception e) {
            log.error("获取评价详情失败: reviewId={}", reviewId, e);
            return CourseResponseWrapper.getFailed("获取评价详情失败");
        }
    }

    /**
     * 处理匿名评价
     */
    private ReviewsVo processAnonymousReview(ReviewsVo review) {
        if (review.getAnonymous() != null && review.getAnonymous()) {
            // 匿名处理：隐藏学生信息
            review.setStudentId(null);
            // 可以根据需要隐藏其他敏感信息
        }
        return review;
    }
}