package com.lyw.commonUtil.util;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.BeanUtils;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;

import java.text.SimpleDateFormat;
import java.util.Map;

/**
 * Feign响应处理工具类
 */
public class FeignResponseHelper {

    /**
     * 转换 CourseResponseWrapper 到指定类型
     * @param responseWrapper 响应包装器
     * @param clazz 目标类型Class
     * @param <T> 目标类型
     * @return 转换后的对象
     * @throws RuntimeException 当响应失败时抛出异常
     */
    public static <T> T convert(CourseResponseWrapper<?> responseWrapper, Class<T> clazz) {
        if (ObjectUtil.isEmpty(responseWrapper)) {
            throw new RuntimeException("响应包装器不能为空");
        }

        if (!responseWrapper.isSuccess()) {
            throw new RuntimeException(responseWrapper.getErrorMessage() != null ?
                    responseWrapper.getErrorMessage() : "请求失败");
        }

        Object data = responseWrapper.getData();
        if (ObjectUtil.isEmpty(data)) {
            return null;
        }

        // 如果data已经是目标类型，直接转换
        if (clazz.isInstance(data)) {
            return clazz.cast(data);
        }
        // 新增：如果data是Map类型，使用ObjectMapper转换
        if (data instanceof Map) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                // 配置ObjectMapper，忽略未知属性
                objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                // 处理日期格式
                objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

                return objectMapper.convertValue(data, clazz);
            } catch (Exception e) {
                throw new RuntimeException("Map转换失败: " + e.getMessage(), e);
            }
        }
        throw new RuntimeException("数据类型不匹配，期望: " + clazz.getName() + "，实际: " + data.getClass().getName());
    }

    /**
     * 安全转换，失败时返回null
     * @param responseWrapper 响应包装器
     * @param clazz 目标类型Class
     * @param <T> 目标类型
     * @return 转换后的对象，失败时返回null
     */
    public static <T> T convertSafe(CourseResponseWrapper<?> responseWrapper, Class<T> clazz) {
        try {
            return convert(responseWrapper, clazz);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 转换并提供默认值
     * @param responseWrapper 响应包装器
     * @param clazz 目标类型Class
     * @param defaultValue 默认值
     * @param <T> 目标类型
     * @return 转换后的对象，失败时返回默认值
     */
    public static <T> T convertWithDefault(CourseResponseWrapper<?> responseWrapper, Class<T> clazz, T defaultValue) {
        try {
            T result = convert(responseWrapper, clazz);
            return result != null ? result : defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }
}
