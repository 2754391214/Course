package com.lyw.commonUtil.service;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.ClassUtils;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ReflectionKit;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyw.commonUtil.util.BeanConverter;
import com.lyw.commonUtil.util.DateTimeUtils;
import com.lyw.commonUtil.annotation.SelectOneOfField;
import com.lyw.commonUtil.annotation.UniqueFieldValue;
import com.lyw.commonUtil.exception.UserPerceivableSpecificException;
import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public class BaseImpl<M extends BaseMapper<T>, T, D> extends ServiceImpl<M,T> implements BaseBo<T,D> {
    protected Log log = LogFactory.getLog(getClass());
    /**
     * UNIQUE_MAP为对应的表的唯一索引，用于公共保存方法中，有唯一索引异常时，提示对应信息.
     * UNIQUE_MAP 的key为表名，value为那些唯一索引的字段属性名和对应属性提示信息
     */

    public static final Map<String,Map<String,String>> UNIQUE_MAP = new HashMap<>(){{
        //tableName，columnName不能重复
        /*put("tableName",new HashMap<>(){{
            put("columnName","lable name");
        }});*/
    }};
    @Override
    public CourseResponseWrapper searchList(D dto) throws NoSuchFieldException, IllegalAccessException {
        if (ObjectUtil.isNotNull(dto)) {
            Class<?> superclass = dto.getClass().getSuperclass();
            Field pageNoField = superclass.getDeclaredField("pageNo");
            pageNoField.setAccessible(true);
            Integer pageNo = (Integer) pageNoField.get(dto);
            pageNo = ObjectUtil.isNotEmpty(pageNo)?pageNo:1;
            Field pageSizeField = superclass.getDeclaredField("pageSize");
            pageSizeField.setAccessible(true);
            Integer pageSize =(Integer) pageSizeField.get(dto);
            pageSize = ObjectUtil.isNotEmpty(pageSize)?pageSize:1;
            QueryWrapper<T> queryChainWrapper = new QueryWrapper<>();
            IPage<T> res = baseMapper.selectPage(new Page<>(pageNo, pageSize), queryChainWrapper);
            return CourseResponseWrapper.getSuccess(res);

        }else{
            return CourseResponseWrapper.getFailed("参数不能为空");
        }
    }

    @Override
    public CourseResponseWrapper findById(Long id) {
        try {
            T entity = baseMapper.selectById(id);
            if (ObjectUtil.isEmpty(entity)) {
                return CourseResponseWrapper.getFailed("数据不存在");
            }
            return CourseResponseWrapper.getSuccess(entity);
        } catch (Exception e) {
            log.error("Find by id error", e);
            throw new UserPerceivableSpecificException("查询失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CourseResponseWrapper addDetail(D dto) throws NoSuchFieldException, IllegalAccessException {
        if (ObjectUtil.isNotEmpty(dto)) {
            T entity = (T) BeanConverter.dtoToVo(dto,this.entityClass);
            //获取实体类信息
            TableInfo tableInfo = TableInfoHelper.getTableInfo(this.entityClass);
            //获取表名
            String tableName = tableInfo.getTableName();
            try{
                //获取主键属性名
                String keyProperty = tableInfo.getKeyProperty();
                //获取主键属性值
                Object idVal = ReflectionKit.getFieldValue(entity, keyProperty);
                Object lastUpdate = ReflectionKit.getFieldValue(entity,"lud");

                String now = DateTimeUtils.parseLocalDateTime(LocalDateTime.now(), "yyyy-MM-dd HH:mm:ss.SSS");
                //TODO:用户id应该存在分布式session中
                String userCode = "admin";
                List<Field> fieldList = ReflectionKit.getFieldList(ClassUtils.getUserClass(entity));
                QueryWrapper<T> uniqueQueryWrapper = new QueryWrapper<T>();
                List<String> exitName = new ArrayList<>();
                List<String> errValue = new ArrayList<>();
                Boolean isTableId = false;
                for (Field field : fieldList) {
                    field.setAccessible(true);
                    if (field.isAnnotationPresent(UniqueFieldValue.class)) {
                        //获取标识的数据库字段
                        String column = field.getAnnotation(UniqueFieldValue.class).value();
                        //获取标识的数据库字段值
                        Object value = field.get(entity);
                        //获取字段的名称
                        if (ObjectUtil.isNotEmpty(value)) {
                            if(field.isAnnotationPresent(TableId.class)){
                                isTableId = true;
                            }
                            uniqueQueryWrapper.eq(column, value);
                            exitName.add(field.getAnnotation(UniqueFieldValue.class).Name());
                            errValue.add(String.valueOf(value));
                        }
                    }
                }
                if(CollectionUtils.isNotEmpty(exitName)){
                    if(!isTableId || StringUtils.isNotBlank(String.valueOf(lastUpdate))){ // 当前唯一字段不是主键，或者唯一字段是主键且当前是修改才可以加 ne 条件
                        uniqueQueryWrapper.ne(!StringUtils.checkValNull(idVal) ,tableInfo.getKeyColumn(),idVal);
                    }
                    if (baseMapper.selectCount(uniqueQueryWrapper) > 0) {
                        //数据库对应字段已存在值,提示已存在
                        throw new UserPerceivableSpecificException(exitName.stream().collect(Collectors.joining(","))+" "+errValue.stream().collect(Collectors.joining(","))+" 已存在");
                    }
                }

                //从页面回传过来的最后修改时间,需要作为where条件使用
                if (StringUtils.checkValNull(idVal) || StringUtils.checkValNull(lastUpdate)){
                    //获取实体类的属性
                    List<TableFieldInfo> fields = tableInfo.getFieldList();
                    //对luu,lud，cru,cud进行赋值
                    for (TableFieldInfo fieldInfo : fields){
                        Field field = fieldInfo.getField();
                        field.setAccessible(true);
                        //根据自定义字段唯一查询的注解，根据改字段检查数据库是否有改据，如果有提示信息已被修改
                        if(field.isAnnotationPresent(SelectOneOfField.class)){
                            //获取标识的数据库字段
                            String column = field.getAnnotation(SelectOneOfField.class).value();
                            //获取标识的数据库字段值
                            Object value = field.get(entity);
                            if(baseMapper.selectCount(new QueryWrapper<T>().eq(column,value)) > 0){
                                throw new UserPerceivableSpecificException("此页面上的信息已被其他人修改，请刷新页面！");
                            }
                        }
                        if("luu".equals(fieldInfo.getProperty()) || "cru".equals(fieldInfo.getProperty())){
                            field.set(entity,userCode);
                        }
                        if("lud".equals(fieldInfo.getProperty()) || "crd".equals(fieldInfo.getProperty())){
                            field.set(entity,now);
                        }
                    }
                    //插入数据表
                    if(baseMapper.insert(entity)!=1){
                        throw new UserPerceivableSpecificException("操作失败");
                    }
                }else {
                    throw new UserPerceivableSpecificException("操作失败");
                }
                return CourseResponseWrapper.getSuccess(entity);
            } catch (DuplicateKeyException e){
                // 查找匹配的字段
                TableFieldInfo fieldInfo = tableInfo.getFieldList().stream()
                        .filter(it -> e.getMessage().contains(tableName + "." + it.getProperty()))
                        .findFirst().orElse(null);
                if (fieldInfo == null) {
                    e.printStackTrace();
                    throw new UserPerceivableSpecificException("数据已存在");
                }
                // 构建错误信息
                Object propertyValue = ReflectionKit.getFieldValue(entity, fieldInfo.getProperty());
                String fieldName = UNIQUE_MAP.getOrDefault(tableName, Collections.emptyMap())
                        .getOrDefault(fieldInfo.getProperty(), "");
                String fieldValue = ObjectUtil.isNotNull(propertyValue) ? propertyValue.toString() : "";

                throw new UserPerceivableSpecificException(String.format("%s %s 已存在", fieldName, fieldValue));
            }
        }else{
            throw new UserPerceivableSpecificException("操作失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CourseResponseWrapper updateDetail(Long id, D dto) throws NoSuchFieldException, IllegalAccessException {
        if (ObjectUtil.isNotEmpty(dto)) {
            T entity = (T) BeanConverter.dtoToVo(dto,this.entityClass);
            //获取实体类信息
            TableInfo tableInfo = TableInfoHelper.getTableInfo(this.entityClass);
            //获取表名
            String tableName = tableInfo.getTableName();
            try{
                Object lastUpdate = ReflectionKit.getFieldValue(entity,"lud");
                String now = DateTimeUtils.parseLocalDateTime(LocalDateTime.now(), "yyyy-MM-dd HH:mm:ss.SSS");
                //TODO:用户id应该存在分布式session中
                String userCode = "admin";
                List<Field> fieldList = ReflectionKit.getFieldList(ClassUtils.getUserClass(entity));
                QueryWrapper<T> uniqueQueryWrapper = new QueryWrapper<T>();
                List<String> exitName = new ArrayList<>();
                List<String> errValue = new ArrayList<>();
                Boolean isTableId = false;
                for (Field field : fieldList) {
                    field.setAccessible(true);
                    if (field.isAnnotationPresent(UniqueFieldValue.class)) {
                        //获取标识的数据库字段
                        String column = field.getAnnotation(UniqueFieldValue.class).value();
                        //获取标识的数据库字段值
                        Object value = field.get(entity);
                        //获取字段的名称
                        if (ObjectUtil.isNotEmpty(value)) {
                            if(field.isAnnotationPresent(TableId.class)){
                                isTableId = true;
                            }
                            uniqueQueryWrapper.eq(column, value);
                            exitName.add(field.getAnnotation(UniqueFieldValue.class).Name());
                            errValue.add(String.valueOf(value));
                        }
                    }
                }
                if(CollectionUtils.isNotEmpty(exitName)){
                    if(!isTableId || StringUtils.isNotBlank(String.valueOf(lastUpdate))){ // 当前唯一字段不是主键，或者唯一字段是主键且当前是修改才可以加 ne 条件
                        uniqueQueryWrapper.ne(ObjectUtil.isNotEmpty(id),tableInfo.getKeyColumn(),id);
                    }
                    if (baseMapper.selectCount(uniqueQueryWrapper) > 0) {
                        //数据库对应字段已存在值,提示已存在
                        throw new UserPerceivableSpecificException(exitName.stream().collect(Collectors.joining(","))+" "+errValue.stream().collect(Collectors.joining(","))+" 已存在");
                    }
                }

                //乐观锁，判断编辑期间是否已经被其他人更新，有则丢弃
                QueryWrapper queryWrapper = new QueryWrapper();
                queryWrapper.eq(tableInfo.getKeyColumn(),id);
                queryWrapper.eq("lud",lastUpdate);

                //根据主键和lud查询记录，判断是否存在记录
                Integer count = baseMapper.selectCount(queryWrapper);
                if(count < 1){//记录不存在时，提示记录信息已被修改
                    throw new UserPerceivableSpecificException("此页面上的信息已被其他人修改，请刷新页面！");
                }

                setFieldValue(entity,"lud",now);
                setFieldValue(entity,"luu",userCode);
                //根据主键和lud更新数据表
                if(baseMapper.update(entity, queryWrapper)!=1){
                    /*更新失败,事物回滚*/
                    throw new UserPerceivableSpecificException("此页面上的信息已被其他人修改，请刷新页面！");
                }
                return CourseResponseWrapper.getSuccess(entity);
            } catch (DuplicateKeyException e){
                // 查找匹配的字段
                TableFieldInfo fieldInfo = tableInfo.getFieldList().stream()
                        .filter(it -> e.getMessage().contains(tableName + "." + it.getProperty()))
                        .findFirst().orElse(null);
                if (fieldInfo == null) {
                    e.printStackTrace();
                    throw new UserPerceivableSpecificException("数据已存在");
                }
                // 构建错误信息
                Object propertyValue = ReflectionKit.getFieldValue(entity, fieldInfo.getProperty());
                String fieldName = UNIQUE_MAP.getOrDefault(tableName, Collections.emptyMap())
                        .getOrDefault(fieldInfo.getProperty(), "");
                String fieldValue = ObjectUtil.isNotNull(propertyValue) ? propertyValue.toString() : "";

                throw new UserPerceivableSpecificException(String.format("%s %s 已存在", fieldName, fieldValue));
            }
        }else{
            throw new UserPerceivableSpecificException("操作失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CourseResponseWrapper deleteDetail(Long id) {
        int count = baseMapper.deleteById(id);
        if (count==0) {
            return CourseResponseWrapper.getFailed("数据不存在");
        }
        return CourseResponseWrapper.getSuccess();
    }

    //设置属性值
    private void setFieldValue(T entity, String fieldName, Object val) throws NoSuchFieldException, IllegalAccessException {
        Field field = this.entityClass.getSuperclass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(entity,val);
    }
}
