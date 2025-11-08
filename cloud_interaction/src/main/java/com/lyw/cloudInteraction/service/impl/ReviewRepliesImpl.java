package com.lyw.cloudInteraction.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lyw.cloudInteraction.mapper.InteractionsDao;
import com.lyw.cloudInteraction.mapper.ReviewsDao;
import com.lyw.cloudInteraction.vo.InteractionsVo;
import com.lyw.cloudInteraction.vo.ReviewRepliesVo;
import com.lyw.cloudInteraction.dto.ReviewRepliesDto;
import com.lyw.cloudInteraction.mapper.ReviewRepliesDao;
import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;
import com.lyw.commonUtil.service.BaseImpl;
import com.lyw.commonUtil.util.DateTimeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.lyw.cloudInteraction.service.ReviewRepliesBo;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 评价回复表 服务类
 * </p>
 *
 * @author lyw
 * @since 2025/10/29
 */
@Slf4j
@Service
public class ReviewRepliesImpl extends BaseImpl<ReviewRepliesDao, ReviewRepliesVo, ReviewRepliesDto> implements ReviewRepliesBo {
    @Resource
    private ReviewsDao reviewsDao;
    @Resource
    private InteractionsDao interactionsDao;

    @Override
    public CourseResponseWrapper getRepliesByReviewId(Long reviewId, Integer page, Integer size, String sort) {
        try {
            Page<ReviewRepliesVo> pageParam = new Page<>(page == null ? 1 : page, size == null ? 10 : size);

            LambdaQueryWrapper<ReviewRepliesVo> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ReviewRepliesVo::getReviewId, reviewId)
                    .eq(ReviewRepliesVo::getStatus, "approved") // 只查询已审核的回复
                    .isNull(ReviewRepliesVo::getParentId); // 只查询顶级回复

            // 排序处理
            if ("latest".equals(sort)) {
                queryWrapper.orderByDesc(ReviewRepliesVo::getCrd);
            } else if ("like".equals(sort)) {
                queryWrapper.orderByDesc(ReviewRepliesVo::getLikeCount);
            } else {
                queryWrapper.orderByDesc(ReviewRepliesVo::getCrd); // 默认按时间倒序
            }

            Page<ReviewRepliesVo> result = getBaseMapper().selectPage(pageParam, queryWrapper);

            // 处理匿名回复
            List<ReviewRepliesVo> processedReplies = result.getRecords().stream()
                    .map(this::processAnonymousReply)
                    .collect(Collectors.toList());

            // 获取每个顶级回复的子回复数量
            Map<Long, Long> childReplyCounts = getChildReplyCounts(
                    processedReplies.stream().map(ReviewRepliesVo::getId).collect(Collectors.toList())
            );

            // 构建响应数据
            List<Map<String, Object>> replyData = processedReplies.stream().map(reply -> {
                Map<String, Object> data = new HashMap<>();
                data.put("reply", reply);
                data.put("childReplyCount", childReplyCounts.getOrDefault(reply.getId(), 0L));
                return data;
            }).collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("list", replyData);
            response.put("total", result.getTotal());
            response.put("page", result.getCurrent());
            response.put("size", result.getSize());

            return CourseResponseWrapper.getSuccess(response);
        } catch (Exception e) {
            log.error("根据评价ID获取回复列表失败: reviewId={}", reviewId, e);
            return CourseResponseWrapper.getFailed("获取回复列表失败");
        }
    }

    @Override
    public CourseResponseWrapper getReplyTreeByReviewId(Long reviewId) {
        try {
            // 获取该评价下的所有回复
            LambdaQueryWrapper<ReviewRepliesVo> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ReviewRepliesVo::getReviewId, reviewId)
                    .eq(ReviewRepliesVo::getStatus, "approved")
                    .orderByAsc(ReviewRepliesVo::getCrd);

            List<ReviewRepliesVo> allReplies = getBaseMapper().selectList(queryWrapper);

            // 处理匿名回复
            allReplies = allReplies.stream()
                    .map(this::processAnonymousReply)
                    .collect(Collectors.toList());

            // 构建树形结构
            List<Map<String, Object>> treeData = buildReplyTree(allReplies);

            return CourseResponseWrapper.getSuccess(treeData);
        } catch (Exception e) {
            log.error("获取回复树形结构失败: reviewId={}", reviewId, e);
            return CourseResponseWrapper.getFailed("获取回复树形结构失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CourseResponseWrapper submitReply(ReviewRepliesDto dto) {
        try {
            // 验证评价是否存在
            if (reviewsDao.selectById(dto.getReviewId()) == null) {
                return CourseResponseWrapper.getFailed("评价不存在");
            }

            // 如果是回复回复，验证父回复是否存在
            if (dto.getParentId() != null) {
                ReviewRepliesVo parentReply = getBaseMapper().selectById(dto.getParentId());
                if (parentReply == null || !parentReply.getReviewId().equals(dto.getReviewId())) {
                    return CourseResponseWrapper.getFailed("父回复不存在或不属于该评价");
                }
            }

            // 设置默认状态
            if (dto.getStatus() == null) {
                dto.setStatus("pending");
            }

            // 初始化计数
            if (dto.getLikeCount() == null) dto.setLikeCount(0);
            if (dto.getAnonymous() == null) dto.setAnonymous(false);

            // 保存回复
            ReviewRepliesVo replyVo = convertToVo(dto);
            replyVo.setCrd(DateTimeUtils.getCurrentDateTime());
            replyVo.setLud(DateTimeUtils.getCurrentDateTime());

            int result = getBaseMapper().insert(replyVo);
            if (result > 0) {
                // 如果是顶级回复，更新评价表的回复数
                if (dto.getParentId() == null) {
                    reviewsDao.incrementReplyCount(dto.getReviewId());
                }

                return CourseResponseWrapper.getSuccess("回复提交成功，等待审核");
            } else {
                return CourseResponseWrapper.getFailed("回复提交失败");
            }
        } catch (Exception e) {
            log.error("提交回复失败: {}", dto, e);
            return CourseResponseWrapper.getFailed("回复提交失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CourseResponseWrapper updateReply(ReviewRepliesDto dto) {
        try {
            // 验证回复是否存在且属于该用户
            ReviewRepliesVo existingReply = getBaseMapper().selectById(dto.getId());
            if (existingReply == null) {
                return CourseResponseWrapper.getFailed("回复不存在");
            }

            if (!existingReply.getUserId().equals(dto.getUserId())) {
                return CourseResponseWrapper.getFailed("无权修改此回复");
            }

            // 只能修改待审核或已审核的回复
            if ("rejected".equals(existingReply.getStatus()) || "hidden".equals(existingReply.getStatus())) {
                return CourseResponseWrapper.getFailed("该回复无法修改");
            }

            // 更新后状态重置为待审核
            dto.setStatus("pending");

            ReviewRepliesVo updateVo = convertToVo(dto);
            updateVo.setLud(DateTimeUtils.getCurrentDateTime());

            int result = getBaseMapper().updateById(updateVo);
            if (result > 0) {
                return CourseResponseWrapper.getSuccess("回复更新成功，等待重新审核");
            } else {
                return CourseResponseWrapper.getFailed("回复更新失败");
            }
        } catch (Exception e) {
            log.error("更新回复失败: {}", dto, e);
            return CourseResponseWrapper.getFailed("回复更新失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CourseResponseWrapper deleteReply(Long replyId, Long userId) {
        try {
            // 验证回复是否存在且属于该用户
            ReviewRepliesVo existingReply = getBaseMapper().selectById(replyId);
            if (existingReply == null) {
                return CourseResponseWrapper.getFailed("回复不存在");
            }

            if (!existingReply.getUserId().equals(userId)) {
                return CourseResponseWrapper.getFailed("无权删除此回复");
            }

            // 逻辑删除回复
            LambdaUpdateWrapper<ReviewRepliesVo> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(ReviewRepliesVo::getId, replyId)
                    .set(ReviewRepliesVo::getStatus, "hidden")
                    .set(ReviewRepliesVo::getLud, DateTimeUtils.getCurrentDateTime());

            int result = getBaseMapper().update(null, updateWrapper);
            if (result > 0) {
                // 如果是顶级回复，更新评价表的回复数
                if (existingReply.getParentId() == null) {
                    reviewsDao.decrementReplyCount(existingReply.getReviewId());
                }

                return CourseResponseWrapper.getSuccess("回复删除成功");
            } else {
                return CourseResponseWrapper.getFailed("回复删除失败");
            }
        } catch (Exception e) {
            log.error("删除回复失败: replyId={}, userId={}", replyId, userId, e);
            return CourseResponseWrapper.getFailed("回复删除失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CourseResponseWrapper auditReply(Long replyId, String status, String auditRemark) {
        try {
            ReviewRepliesVo existingReply = getBaseMapper().selectById(replyId);
            if (existingReply == null) {
                return CourseResponseWrapper.getFailed("回复不存在");
            }

            // 验证状态值
            if (!Arrays.asList("approved", "rejected", "hidden").contains(status)) {
                return CourseResponseWrapper.getFailed("无效的状态值");
            }

            LambdaUpdateWrapper<ReviewRepliesVo> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(ReviewRepliesVo::getId, replyId)
                    .set(ReviewRepliesVo::getStatus, status)
                    .set(ReviewRepliesVo::getLud, DateTimeUtils.getCurrentDateTime());

            // 如果有审核备注，可以存储在metadata字段中
            if (auditRemark != null && !auditRemark.trim().isEmpty()) {
                // 这里可以添加metadata处理逻辑
            }

            int result = getBaseMapper().update(null, updateWrapper);
            if (result > 0) {
                // 如果审核通过且是顶级回复，更新评价表的回复数
                if ("approved".equals(status) && existingReply.getParentId() == null) {
                    reviewsDao.incrementReplyCount(existingReply.getReviewId());
                }

                return CourseResponseWrapper.getSuccess("审核操作成功");
            } else {
                return CourseResponseWrapper.getFailed("审核操作失败");
            }
        } catch (Exception e) {
            log.error("审核回复失败: replyId={}, status={}", replyId, status, e);
            return CourseResponseWrapper.getFailed("审核操作失败");
        }
    }

    @Override
    public CourseResponseWrapper getRepliesByUserId(Long userId, Integer page, Integer size) {
        try {
            Page<ReviewRepliesVo> pageParam = new Page<>(page == null ? 1 : page, size == null ? 10 : size);

            LambdaQueryWrapper<ReviewRepliesVo> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ReviewRepliesVo::getUserId, userId)
                    .orderByDesc(ReviewRepliesVo::getCrd);

            Page<ReviewRepliesVo> result = getBaseMapper().selectPage(pageParam, queryWrapper);

            // 处理匿名回复
            List<ReviewRepliesVo> processedReplies = result.getRecords().stream()
                    .map(this::processAnonymousReply)
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("list", processedReplies);
            response.put("total", result.getTotal());
            response.put("page", result.getCurrent());
            response.put("size", result.getSize());

            return CourseResponseWrapper.getSuccess(response);
        } catch (Exception e) {
            log.error("根据用户ID获取回复列表失败: userId={}", userId, e);
            return CourseResponseWrapper.getFailed("获取回复列表失败");
        }
    }

    @Override
    public CourseResponseWrapper getReplyDetail(Long replyId) {
        try {
            ReviewRepliesVo reply = getBaseMapper().selectById(replyId);
            if (reply == null) {
                return CourseResponseWrapper.getFailed("回复不存在");
            }

            // 处理匿名回复
            reply = processAnonymousReply(reply);

            // 获取子回复数量
            LambdaQueryWrapper<ReviewRepliesVo> childWrapper = new LambdaQueryWrapper<>();
            childWrapper.eq(ReviewRepliesVo::getParentId, replyId)
                    .eq(ReviewRepliesVo::getStatus, "approved");
            long childCount = getBaseMapper().selectCount(childWrapper);

            // 构建详情信息
            Map<String, Object> detail = new HashMap<>();
            detail.put("reply", reply);
            detail.put("childCount", childCount);
            detail.put("interactionStats", getInteractionStats(replyId));

            // 如果是子回复，获取父回复信息
            if (reply.getParentId() != null) {
                ReviewRepliesVo parentReply = getBaseMapper().selectById(reply.getParentId());
                if (parentReply != null) {
                    detail.put("parentReply", processAnonymousReply(parentReply));
                }
            }

            return CourseResponseWrapper.getSuccess(detail);
        } catch (Exception e) {
            log.error("获取回复详情失败: replyId={}", replyId, e);
            return CourseResponseWrapper.getFailed("获取回复详情失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CourseResponseWrapper incrementLikeCount(Long replyId) {
        try {
            LambdaUpdateWrapper<ReviewRepliesVo> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(ReviewRepliesVo::getId, replyId)
                    .setSql("like_count = like_count + 1")
                    .set(ReviewRepliesVo::getLud, DateTimeUtils.getCurrentDateTime());

            int result = getBaseMapper().update(null, updateWrapper);
            if (result > 0) {
                return CourseResponseWrapper.getSuccess("点赞数增加成功");
            } else {
                return CourseResponseWrapper.getFailed("回复不存在");
            }
        } catch (Exception e) {
            log.error("增加回复点赞数失败: replyId={}", replyId, e);
            return CourseResponseWrapper.getFailed("操作失败");
        }
    }

    @Override
    public CourseResponseWrapper getChildReplies(Long parentId, Integer page, Integer size) {
        try {
            Page<ReviewRepliesVo> pageParam = new Page<>(page == null ? 1 : page, size == null ? 10 : size);

            LambdaQueryWrapper<ReviewRepliesVo> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ReviewRepliesVo::getParentId, parentId)
                    .eq(ReviewRepliesVo::getStatus, "approved")
                    .orderByAsc(ReviewRepliesVo::getCrd);

            Page<ReviewRepliesVo> result = getBaseMapper().selectPage(pageParam, queryWrapper);

            // 处理匿名回复
            List<ReviewRepliesVo> processedReplies = result.getRecords().stream()
                    .map(this::processAnonymousReply)
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("list", processedReplies);
            response.put("total", result.getTotal());
            response.put("page", result.getCurrent());
            response.put("size", result.getSize());

            return CourseResponseWrapper.getSuccess(response);
        } catch (Exception e) {
            log.error("获取子回复列表失败: parentId={}", parentId, e);
            return CourseResponseWrapper.getFailed("获取子回复列表失败");
        }
    }

    /**
     * 处理匿名回复
     */
    private ReviewRepliesVo processAnonymousReply(ReviewRepliesVo reply) {
        if (reply.getAnonymous() != null && reply.getAnonymous()) {
            // 匿名处理：隐藏用户信息
            reply.setUserId(null);
            // 可以根据需要隐藏其他敏感信息
        }
        return reply;
    }

    /**
     * 获取子回复数量
     */
    private Map<Long, Long> getChildReplyCounts(List<Long> parentIds) {
        if (parentIds == null || parentIds.isEmpty()) {
            return new HashMap<>();
        }

        LambdaQueryWrapper<ReviewRepliesVo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(ReviewRepliesVo::getParentId, parentIds)
                .eq(ReviewRepliesVo::getStatus, "approved");

        List<ReviewRepliesVo> childReplies = getBaseMapper().selectList(queryWrapper);

        return childReplies.stream()
                .collect(Collectors.groupingBy(ReviewRepliesVo::getParentId, Collectors.counting()));
    }

    /**
     * 构建回复树形结构
     */
    private List<Map<String, Object>> buildReplyTree(List<ReviewRepliesVo> allReplies) {
        // 按父ID分组
        Map<Long, List<ReviewRepliesVo>> repliesByParentId = allReplies.stream()
                .filter(reply -> reply.getParentId() != null)
                .collect(Collectors.groupingBy(ReviewRepliesVo::getParentId));

        // 构建树形结构
        return allReplies.stream()
                .filter(reply -> reply.getParentId() == null) // 顶级回复
                .map(topReply -> buildReplyTreeNode(topReply, repliesByParentId))
                .collect(Collectors.toList());
    }

    /**
     * 构建回复树节点
     */
    private Map<String, Object> buildReplyTreeNode(ReviewRepliesVo reply, Map<Long, List<ReviewRepliesVo>> repliesByParentId) {
        Map<String, Object> node = new HashMap<>();
        node.put("reply", reply);

        List<ReviewRepliesVo> children = repliesByParentId.get(reply.getId());
        if (children != null && !children.isEmpty()) {
            List<Map<String, Object>> childNodes = children.stream()
                    .map(childReply -> buildReplyTreeNode(childReply, repliesByParentId))
                    .collect(Collectors.toList());
            node.put("children", childNodes);
        } else {
            node.put("children", Collections.emptyList());
        }

        return node;
    }

    /**
     * 获取互动统计
     */
    private Map<String, Object> getInteractionStats(Long replyId) {
        Map<String, Object> stats = new HashMap<>();

        try {
            // 获取点赞相关的互动统计
            LambdaQueryWrapper<InteractionsVo> likeWrapper = new LambdaQueryWrapper<>();
            likeWrapper.eq(InteractionsVo::getTargetType, "reply")
                    .eq(InteractionsVo::getTargetId, replyId)
                    .eq(InteractionsVo::getInteractionType, "like");
            long likeCount = interactionsDao.selectCount(likeWrapper);
            stats.put("likeCount", likeCount);

        } catch (Exception e) {
            log.error("获取互动统计失败: replyId={}", replyId, e);
        }

        return stats;
    }

    /**
     * DTO转VO
     */
    private ReviewRepliesVo convertToVo(ReviewRepliesDto dto) {
        if (dto == null) {
            return null;
        }

        ReviewRepliesVo vo = new ReviewRepliesVo();
        vo.setId(dto.getId());
        vo.setReviewId(dto.getReviewId());
        vo.setParentId(dto.getParentId());
        vo.setUserId(dto.getUserId());
        vo.setContent(dto.getContent());
        vo.setLikeCount(dto.getLikeCount());
        vo.setStatus(dto.getStatus());
        vo.setAnonymous(dto.getAnonymous());
        vo.setCru(dto.getCru());
        vo.setLuu(dto.getLuu());

        return vo;
    }
}