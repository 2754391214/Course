package com.lyw.commonUtil.util;

import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.config.po.TableInfo;
import com.baomidou.mybatisplus.generator.engine.VelocityTemplateEngine;

import java.io.File;
import java.util.Map;

/**
 * 配合代码生成器使用，主要用于自定义模块时输出的路径，如果不使用该类，则会多一层 entity 目录
 * 自定义文件的输出的路径、文件名
 * 重写AbstractTemplateEngine的outputCustomFile方法
 */
public class TimerVelocityTemplateEngine extends VelocityTemplateEngine {

    /**
     * 重写自定义文件输入路径
     *
     * @param customFile --> 自定义 vm
     * @param tableInfo  --> 数据库表信息
     * @param objectMap  --> 自定义 变量
     */
    @Override
    protected void outputCustomFile(Map<String, String> customFile, TableInfo tableInfo, Map<String, Object> objectMap) {
        //获取实体类名称
        String entityName = tableInfo.getEntityName();
        //获取配置的other的文件路径
        String otherPath = this.getPathInfo(OutputFile.other);
        //截取新的文件路径用于拼接
        String path = otherPath.substring(0, otherPath.lastIndexOf(File.separator));
        //遍历自定义的vm
        customFile.forEach((key, value) -> {
            //定义Dto路径
            String fileName = String.format(path + File.separator + "dto" + File.separator + "%s", key);
            //调用outputFile方法  文件对象(路径)、自定义的变量、自定义的vm模板
            this.outputFile(new File(fileName), objectMap, value);
        });
    }
}
