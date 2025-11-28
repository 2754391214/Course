package com.lyw.commonUtil.util;

import cn.hutool.core.util.ObjectUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;

import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Feign响应处理工具类
 */
public class FeignResponseHelper {

    private static final ObjectMapper objectMapper = createObjectMapper();

    /**
     * 转换 CourseResponseWrapper 到指定类型
     * @param responseWrapper 响应包装器
     * @param clazz 目标类型Class
     * @param <T> 目标类型
     * @return 转换后的对象
     * @throws RuntimeException 当响应失败时抛出异常
     */
    public static <T> T convert(CourseResponseWrapper<?> responseWrapper, Class<T> clazz) {
        validateResponse(responseWrapper);

        Object data = responseWrapper.getData();
        if (ObjectUtil.isEmpty(data)) {
            return null;
        }

        // 如果data已经是目标类型，直接转换
        if (clazz.isInstance(data)) {
            return clazz.cast(data);
        }

        // 如果data是Map类型，使用ObjectMapper转换
        if (data instanceof Map) {
            try {
                return objectMapper.convertValue(data, clazz);
            } catch (Exception e) {
                throw new RuntimeException("Map转换失败: " + e.getMessage(), e);
            }
        }

        throw new RuntimeException("数据类型不匹配，期望: " + clazz.getName() + "，实际: " + data.getClass().getName());
    }

    /**
     * 转换 CourseResponseWrapper 到集合类型
     * @param responseWrapper 响应包装器
     * @param collectionClass 集合类型Class
     * @param elementClass 元素类型Class
     * @param <T> 集合类型
     * @param <E> 元素类型
     * @return 转换后的集合
     */
    public static <T extends Collection<E>, E> T convert(CourseResponseWrapper<?> responseWrapper,
                                                         Class<T> collectionClass,
                                                         Class<E> elementClass) {
        validateResponse(responseWrapper);

        Object data = responseWrapper.getData();
        if (ObjectUtil.isEmpty(data)) {
            try {
                return collectionClass.newInstance();
            } catch (Exception e) {
                throw new RuntimeException("创建集合实例失败", e);
            }
        }

        // 如果data已经是目标集合类型，检查元素类型
        if (collectionClass.isInstance(data)) {
            Collection<?> collection = (Collection<?>) data;
            if (!collection.isEmpty()) {
                Object firstElement = collection.iterator().next();
                if (!elementClass.isInstance(firstElement)) {
                    // 需要转换元素类型
                    return convertCollectionElements(collection, collectionClass, elementClass);
                }
            }
            return collectionClass.cast(data);
        }

        // 使用ObjectMapper进行类型安全的转换
        try {
            JavaType javaType = objectMapper.getTypeFactory()
                    .constructCollectionType(collectionClass, elementClass);
            return objectMapper.convertValue(data, javaType);
        } catch (Exception e) {
            throw new RuntimeException("集合转换失败: " + e.getMessage(), e);
        }
    }

    /**
     * 专门用于转换List的方法
     */
    public static <E> List<E> convertToList(CourseResponseWrapper<?> responseWrapper, Class<E> elementClass) {
        return convert(responseWrapper, ArrayList.class, elementClass);
    }

    /**
     * 转换集合中的元素类型
     */
    private static <T extends Collection<E>, E> T convertCollectionElements(Collection<?> source,
                                                                            Class<T> collectionClass,
                                                                            Class<E> elementClass) {
        try {
            T result = collectionClass.newInstance();
            for (Object item : source) {
                if (elementClass.isInstance(item)) {
                    result.add(elementClass.cast(item));
                } else {
                    E converted = objectMapper.convertValue(item, elementClass);
                    result.add(converted);
                }
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException("集合元素转换失败", e);
        }
    }

    private static void validateResponse(CourseResponseWrapper<?> responseWrapper) {
        if (ObjectUtil.isEmpty(responseWrapper)) {
            throw new RuntimeException("响应包装器不能为空");
        }

        if (!responseWrapper.isSuccess()) {
            throw new RuntimeException(responseWrapper.getErrorMessage() != null ?
                    responseWrapper.getErrorMessage() : "请求失败");
        }
    }

    /**
     * 创建配置好的ObjectMapper实例
     */
    private static ObjectMapper createObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();

        // 注册JavaTimeModule以支持Java 8日期时间类型
        objectMapper.registerModule(new JavaTimeModule());

        // 配置日期时间格式
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

        // 配置LocalTime的序列化格式
        objectMapper.configOverride(LocalTime.class)
                .setFormat(JsonFormat.Value.forPattern("HH:mm:ss"));

        // 忽略未知属性
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // 禁用将日期写为时间戳
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        return objectMapper;
    }

    // 保留原有的安全转换方法，增加集合版本
    public static <T> T convertSafe(CourseResponseWrapper<?> responseWrapper, Class<T> clazz) {
        try {
            return convert(responseWrapper, clazz);
        } catch (Exception e) {
            return null;
        }
    }

    public static <E> List<E> convertToListSafe(CourseResponseWrapper<?> responseWrapper, Class<E> elementClass) {
        try {
            return convertToList(responseWrapper, elementClass);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public static <T extends Collection<E>, E> T convertSafe(CourseResponseWrapper<?> responseWrapper,
                                                             Class<T> collectionClass,
                                                             Class<E> elementClass) {
        try {
            return convert(responseWrapper, collectionClass, elementClass);
        } catch (Exception e) {
            try {
                return collectionClass.newInstance();
            } catch (Exception ex) {
                throw new RuntimeException("创建默认集合失败", ex);
            }
        }
    }
}
