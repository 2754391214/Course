package com.lyw.commonUtil.util;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.config.rules.DateType;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.lyw.commonUtil.controller.BaseController;
import com.lyw.commonUtil.service.BaseBo;
import com.lyw.commonUtil.service.BaseImpl;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.baomidou.mybatisplus.generator.config.rules.NamingStrategy.underlineToCamel;

/**
 * <p>
 * 代码生成：快速生成
 * </p>
 *
 */
public class AutoGenerate {

    public static void main(String[] args) {
        //参数说明：第一个参数为表名，第二模块名(Controller和Bo对应模块),第三为是否只创建Vo,Dto和Dao,第四为是否只创建的模块的路径名
        ArrayList<String> tableArr = new ArrayList<>();
        tableArr.add("local_message");
        tableArr.forEach(it->{
            generate(it, "cloudChoose",true, "cloud_choose");
        });
    }
    /**
     * 数据源信息
     */
    public static String URL = "jdbc:mysql://110.42.233.169:3306/cloud_choose?useUnicode=true&characterEncoding=utf-8&serverTimezone=GMT%2B8";
    public static String USERNAME = "lyw";
    public static String PASSWORD = "li15907354492@";
    /**
     * 包设置
     */
    public static String PARENT_PACKAGE = "com.lyw";

    /**
     * 自定义模板路径
     */
    public static String TEMPLATE_CONTROLLER_PATH = "templates/javaVm/controller.java.vm";
    public static String TEMPLATE_SERVICE_PATH = "templates/javaVm/service.java.vm";
    public static String TEMPLATE_SERVICEIMPL_PATH = "templates/javaVm/serviceImpl.java.vm";
    public static String TEMPLATE_MAPPER_PATH = "templates/javaVm/mapper.java.vm";
    public static String TEMPLATE_MAPPER_XML_PATH = "templates/javaVm/mapper.xml.vm";
    public static String TEMPLATE_DTO_PATH = "templates/javaVm/dto.java.vm";
    public static String TEMPLATE_VO_PATH = "templates/javaVm/vo.java.vm";


    /**
     * 生成文件
     * @param tableName 表名
     * @param moduleName 模块名，Controller和Bo对应的模块
     * @param isOnlyVoAndDao 是否只创建Vo,Dto和Dao,XMl
     */
    public static void generate(String tableName, String moduleName, Boolean isOnlyVoAndDao, String outputDir){
        FastAutoGenerator fastAutoGenerator = generateConfig(tableName,moduleName,outputDir);
        //当只创建Vo,Dto和Dao
        if(!isOnlyVoAndDao){
            generateNew(fastAutoGenerator);
        }
        fastAutoGenerator.templateEngine(new TimerVelocityTemplateEngine()).execute();
    }
    /**
     * 代码生成(公共配置，生成Vo,Dao)
     */
    public static FastAutoGenerator generateConfig(String tableName, String moduleName, String outputDir) {
        if(StringUtils.isNotBlank(moduleName)){
            moduleName = moduleName + ".";
        }
        FastAutoGenerator fastAutoGenerator = FastAutoGenerator.create(
                URL,
                USERNAME,PASSWORD);
        //全局配置
        fastAutoGenerator.globalConfig(builder -> {builder
                .enableSwagger() // 是否启用swagger注解
                .author("lyw") // 作者名称
                .dateType(DateType.ONLY_DATE) // 时间策略
                .commentDate("yyyy/MM/dd") // 注释日期
                .outputDir(System.getProperty("user.dir")+"\\"+outputDir+"\\src\\main\\java\\") // 输出目录
                .fileOverride(); // 覆盖已生成文件
        });
        // 包配置
        String finalModuleName = moduleName;
        fastAutoGenerator.packageConfig(builder -> {builder
                .parent(PARENT_PACKAGE) // 父包名
                .moduleName("") // 模块包名
                .entity(finalModuleName +"vo") // 实体类包名
                .service(finalModuleName +"service") // service包名
                .serviceImpl(finalModuleName +"service.impl") // serviceImpl包名
                .mapper(finalModuleName +"mapper") // mapper包名
                .controller(finalModuleName +"controller") // controller包名
                .other(finalModuleName +"dto") // 自定义包名
                .pathInfo(Collections.singletonMap(OutputFile.mapperXml, System.getProperty("user.dir") + "\\"+outputDir+"\\src\\main\\resources\\mapper"));
        });
        // 策略配置
        fastAutoGenerator.strategyConfig(builder -> {builder
                .addInclude(tableName) // 表匹配
                // Entity 策略配置
                .entityBuilder()
                .enableLombok() // 开启lombok
                .enableChainModel() // 链式
                .enableRemoveIsPrefix() // 开启boolean类型字段移除is前缀
                .enableTableFieldAnnotation() //开启生成实体时生成的字段注解
                .versionColumnName("version") // 乐观锁数据库字段
                .versionPropertyName("version") // 乐观锁实体类名称
                .logicDeleteColumnName("is_deleted") // 逻辑删除数据库中字段名
                .logicDeletePropertyName("deleted") // 逻辑删除实体类中的字段名
                .naming(NamingStrategy.underline_to_camel) // 表名 下划线 -》 驼峰命名
                .columnNaming(NamingStrategy.underline_to_camel) // 字段名 下划线 -》 驼峰命名
                .idType(IdType.AUTO) // 主键生成策略 自增
                .formatFileName("%sVo") // Entity 文件名称
                .enableColumnConstant()
                .enableActiveRecord()
                .disableSerialVersionUID()
                // Mapper 策略配置
                .mapperBuilder()
                .enableMapperAnnotation() // 开启@Mapper
                .enableBaseColumnList() // 启用 columnList (通用查询结果列)
                .enableBaseResultMap() // 启动resultMap
                .formatMapperFileName("%sDao") // Mapper 文件名称
                .formatXmlFileName("%sDao");
        });
        fastAutoGenerator.templateConfig(builder -> {builder
                .entity(TEMPLATE_VO_PATH )
                .mapper(TEMPLATE_MAPPER_PATH)
                .mapperXml(TEMPLATE_MAPPER_XML_PATH);
        });
        fastAutoGenerator.injectionConfig(builder -> {
            // 自定义 DTO 生成
            Map<String, String> customFile = new HashMap<>();
            // 自定义参数，用于模板中获取dtoClassName
            Map<String, Object> customMap = new HashMap<>();
            String code = StringUtils.capitalize(underlineToCamel(tableName));
            customFile.put(code+"Dto.java", TEMPLATE_DTO_PATH);
            builder.customFile(customFile);

            customMap.put("className", code);
            // 使用表名生成Dto类名，这里表名是tableName，我们使用StringUtils将下划线表名转为驼峰，并首字母大写，然后加上Dto
            String dtoName = code + "Dto";
            customMap.put("dtoClassName", dtoName);
            String voName = code + "Vo";
            customMap.put("voClassName", voName);
            builder.customMap(customMap);

        });
        return fastAutoGenerator;
    }

    /**
     * 代码生成(新增文件)
     */
    public static void generateNew(FastAutoGenerator fastAutoGenerator) {
        fastAutoGenerator.strategyConfig(builder -> {builder
            // Controller 策略配置
            .controllerBuilder()
            .enableRestStyle() // 开启@Controller
            .formatFileName("%sController") // Controller 文件名称
            .superClass(BaseController.class)
            // Service 策略配置
            .serviceBuilder()
            .formatServiceFileName("%sBo") // Service 文件名称
            .superServiceClass(BaseBo.class)
            .formatServiceImplFileName("%sImpl") // ServiceImpl 文件名称
            .superServiceImplClass(BaseImpl.class);
        }).templateConfig(builder -> {builder
            .controller(TEMPLATE_CONTROLLER_PATH)
            .service(TEMPLATE_SERVICE_PATH)
            .serviceImpl(TEMPLATE_SERVICEIMPL_PATH);
        }).templateEngine(new TimerVelocityTemplateEngine()).execute();
    }
}
