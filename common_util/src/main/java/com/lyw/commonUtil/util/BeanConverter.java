package com.lyw.commonUtil.util;

import org.springframework.cglib.beans.BeanCopier;
import org.springframework.cglib.core.Converter;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 在vo上不要使用@Accessors(chain = true),会报错
 * dto vo 互相转换类
 */
public class BeanConverter {

    /**
     * DTO 转 VO（自动处理嵌套对象和集合）
     *
     * @param dto     源对象（DTO）
     * @param voClass 目标类型（VO.class）
     * @return 转换后的 VO 对象
     */
    public static <D, V> V dtoToVo(D dto, Class<V> voClass) {
        try {
            V vo = voClass.getDeclaredConstructor().newInstance();
            BeanCopier copier = BeanCopier.create(dto.getClass(), voClass, true);
            // 使用自定义转换器处理嵌套类型
            copier.copy(dto, vo, new DeepCopyConverter(true));
            return vo;
        } catch (Exception e) {
            throw new RuntimeException("DTO转VO失败", e);
        }
    }

    /**
     * VO 转 DTO（自动处理嵌套对象和集合）
     *
     * @param vo       源对象（VO）
     * @param dtoClass 目标类型（DTO.class）
     * @return 转换后的 DTO 对象
     */
    public static <V, D> D voToDto(V vo, Class<D> dtoClass) {
        try {
            D dto = dtoClass.getDeclaredConstructor().newInstance();
            BeanCopier copier = BeanCopier.create(vo.getClass(), dtoClass, true);
            // 使用自定义转换器处理嵌套类型
            copier.copy(vo, dto, new DeepCopyConverter(false));
            return dto;
        } catch (Exception e) {
            throw new RuntimeException("VO转DTO失败", e);
        }
    }

    /**
     * 深度拷贝转换器（处理 List 和嵌套对象）
     */
    private static class DeepCopyConverter implements Converter {
        private final boolean isDtoToVo;

        public DeepCopyConverter(boolean isDtoToVo) {
            this.isDtoToVo = isDtoToVo;
        }

        @Override
        public Object convert(Object sourceValue, Class targetType, Object context) {
            if (sourceValue == null) return null;

            // 处理 List 类型
            if (sourceValue instanceof List) {
                return handleListConversion((List<?>) sourceValue);
            }

            // 处理嵌套对象类型
            if (isComplexType(sourceValue)) {
                return handleObjectConversion(sourceValue);
            }

            // 基础类型直接返回
            return sourceValue;
        }

        /**
         * 处理集合类型转换
         */
        private List<?> handleListConversion(List<?> sourceList) {
            return sourceList.stream()
                    .map(item -> {
                        if (item == null) return null;
                        if (item instanceof List) { // 嵌套 List
                            return handleListConversion((List<?>) item);
                        } else if (isComplexType(item)) { // 嵌套对象
                            return handleObjectConversion(item);
                        } else { // 基础类型
                            return item;
                        }
                    })
                    .collect(Collectors.toList());
        }

        /**
         * 处理对象类型转换
         */
        private Object handleObjectConversion(Object sourceObj) {
            try {
                if (sourceObj == null) return null;
                String sourceClassName = sourceObj.getClass().getName();
                String targetClassName = sourceClassName.replace(
                        isDtoToVo ? ".dto." : ".vo.",
                        isDtoToVo ? ".vo." : ".dto."
                );
                targetClassName = targetClassName.replace(
                        isDtoToVo ? "Dto" : "Vo",
                        isDtoToVo ? "Vo" : "Dto"
                );
                Class<?> targetClass = Class.forName(targetClassName);

                if (isDtoToVo) {
                    return dtoToVo(sourceObj, targetClass);
                } else {
                    return voToDto(sourceObj, targetClass);
                }
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("找不到对应转换类型", e);
            }
        }

        /**
         * 判断是否需要特殊处理的复杂类型（非 JDK 内置类型）
         */
        private boolean isComplexType(Object obj) {
            return !obj.getClass().getName().startsWith("java.");
        }
    }
}