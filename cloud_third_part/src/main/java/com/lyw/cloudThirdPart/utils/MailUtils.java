package com.lyw.cloudThirdPart.utils;

import cn.hutool.core.collection.CollectionUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.internet.MimeMessage;
import java.io.*;
import java.text.MessageFormat;
import java.util.List;

@Component
@Slf4j
@ConditionalOnClass(value = JavaMailSender.class)
public class MailUtils {
    @Value(value = "${spring.mail.from}")
    private String from;


    @javax.annotation.Resource
    private JavaMailSender mailSender;


    public boolean sendSampleMail(String to, String subject, String context) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(context);
            mailSender.send(message);
        } catch (Exception e) {
            log.error("邮件发送日志记录失败,原因 {}",e.getMessage());
            return false;
        }
        return true;
    }

    public boolean sendHtmlMailAndAttach(String to, String subject, String htmlContent, List<File> attachmentList) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true);
            messageHelper.setFrom(from);
            messageHelper.setTo(to);
            messageHelper.setSubject(subject);
            messageHelper.setText(htmlContent, true);  // 设置第二个参数为 true 表示邮件内容为 HTML
            if (CollectionUtil.isNotEmpty(attachmentList)) {
                for (File attachment:attachmentList) {
                    // 添加附件（如果附件存在）
                    if (attachment != null && attachment.exists() && attachment.isFile()) {
                        messageHelper.addAttachment(
                                attachment.getName(),  // 附件显示名称
                                new FileSystemResource(attachment)  // 附件资源
                        );
                    }
                }
            }
            mailSender.send(mimeMessage);
        } catch (Exception e) {
            log.error("HTML邮件发送失败, 原因 {}", e.getMessage());
            return false;
        }
        return true;
    }

    public boolean sendHtmlMail(String to, String subject, String htmlContent) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true);
            messageHelper.setFrom(from);
            messageHelper.setTo(to);
            messageHelper.setSubject(subject);
            messageHelper.setText(htmlContent, true);  // 设置第二个参数为 true 表示邮件内容为 HTML
            mailSender.send(mimeMessage);
        } catch (Exception e) {
            log.error("HTML邮件发送失败, 原因 {}", e.getMessage());
            return false;
        }
        return true;
    }


    public void sendAttachmentMail(String to, String subject, String context, String attachmentName, String filePath) throws Exception {
        //创建一个复杂的消息邮件
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
        helper.setFrom(from);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(context);

        //上传文件
        helper.addAttachment(attachmentName, new File(filePath));
        mailSender.send(mimeMessage);
    }

    public void sendTemplateMail(String to, String subject, String templatePath, String... arguments) throws Exception {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
        helper.setFrom(from);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(this.buildTemplateContext(templatePath, arguments), true);
        mailSender.send(mimeMessage);
    }

    private String buildTemplateContext(String templatePath, String... arguments) {
        //加载邮件html模板
        Resource resource = new ClassPathResource(templatePath);
        InputStream inputStream = null;
        BufferedReader fileReader = null;
        StringBuffer buffer = new StringBuffer();
        String line = "";
        try {
            inputStream = resource.getInputStream();
            fileReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            while ((line = fileReader.readLine()) != null) {
                buffer.append(line);
            }
        } catch (Exception e) {
            log.info("读取模板失败:", e);
        } finally {
            if (fileReader != null) {
                try {
                    fileReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        //替换html模板中的参数
        return MessageFormat.format(buffer.toString(), arguments);
    }
}