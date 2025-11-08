package com.lyw.cloudThirdPart.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.utils.BinaryUtil;
import com.aliyun.oss.model.MatchMode;
import com.aliyun.oss.model.PolicyConditions;
import com.lyw.cloudThirdPart.service.OssService;
import com.lyw.cloudThirdPart.utils.AliOSSUtils;
import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Service
public class OssServiceImpl implements OssService {
    @Resource
    private AliOSSUtils aliOSSUtils;

    @Override
    public CourseResponseWrapper policy() {
        //https://gulimall-clouds.oss-cn-beijing.aliyuncs.com/iqiyi.png

        // Endpoint以杭州为例，其它Region请按实际情况填写。
        String endpoint = aliOSSUtils.getEndpoints();
        // 云账号AccessKey有所有API访问权限，建议遵循阿里云安全最佳实践，创建并使用RAM子账号进行API访问或日常运维，请登录 https://ram.console.aliyun.com 创建。
        String accessKeyId = aliOSSUtils.getAccessKeyId();
        String accessKeySecret = aliOSSUtils.getAccessKeySecret();

        String bucket = aliOSSUtils.getBucketName(); // 请填写您的 bucketname 。
        String host = "https://" + bucket + "." + endpoint; // host的格式为 bucketname.endpoint
        // callbackUrl为 上传回调服务器的URL，请将下面的IP和Port配置为您自己的真实信息。
        //String callbackUrl = "http://88.88.88.88:8888";

        String format = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String dir = format + "/"; // 用户上传文件时指定的前缀。

        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        Map<String, String> respMap = null;
        try {
            long expireTime = 30;
            long expireEndTime = System.currentTimeMillis() + expireTime * 1000;
            Date expiration = new Date(expireEndTime);
            PolicyConditions policyConds = new PolicyConditions();
            policyConds.addConditionItem(PolicyConditions.COND_CONTENT_LENGTH_RANGE, 0, 1048576000);
            policyConds.addConditionItem(MatchMode.StartWith, PolicyConditions.COND_KEY, dir);

            String postPolicy = ossClient.generatePostPolicy(expiration, policyConds);
            byte[] binaryData = postPolicy.getBytes("utf-8");
            String encodedPolicy = BinaryUtil.toBase64String(binaryData);
            String postSignature = ossClient.calculatePostSignature(postPolicy);

            respMap = new LinkedHashMap<String, String>();
            respMap.put("accessid", accessKeyId);
            respMap.put("policy", encodedPolicy);
            respMap.put("signature", postSignature);
            respMap.put("dir", dir);
            respMap.put("host", host);
            respMap.put("expire", String.valueOf(expireEndTime / 1000));

        } catch (Exception e) {
            // Assert.fail(e.getMessage());
            System.out.println(e.getMessage());
        } finally {
            ossClient.shutdown();
        }
        return CourseResponseWrapper.getSuccess(respMap);
    }

    @Override
    public CourseResponseWrapper upload(MultipartFile file) {
        log.info("文件上传，文件：{}", file.getOriginalFilename());
        //调用阿里云OSS工具类进行文件上传
        String url = null;
        try {
            url = aliOSSUtils.upload(file);
            log.info("文件上传完成，文件访问的url：{}", url);
            return CourseResponseWrapper.getSuccess(url);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
