package com.lyw.commonUtil.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 判断添加或更新操作时值唯一，数据库字段内容不能重复
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface UniqueFieldValue {
    /**
     *需要查询数据库的字段名
     */
    String value();

    /**
     * 需要提示的哪个字段名已存在
     * ‘名字'已存在
     */
    String Name();

}
