package com.example.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

@Component
public class MailUtils {

    //如果要使用邮箱 那么需要调用 java提供的邮箱配置类
    @Autowired
    private JavaMailSenderImpl mailSender;

    //发送邮件 需要 发送人，接收人，邮件标题，邮件主题信息
    @Value("${spring.mail.username}")
    private String mailfrom;

    //邮件发送方法
    public void sendEmail(String mailto,String title,String content){
        //定制邮件发送内容
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailfrom);
        message.setTo(mailto);
        message.setSubject(title);
        message.setText(content);
        mailSender.send(message);
    }
}
