package com.example.web.scheduletask;

import com.example.dao.StatisticMapper;
import com.example.utils.MailUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduleTask {
    //需要注入刚才写好得到MailUtils
    //还需要注入 文章统计表
    @Autowired
    private StatisticMapper statisticMapper;
    @Autowired
    private MailUtils mailUtils;

    @Value("${spring.mail.username}")
    private String mailto;

    //定时任务
    @Scheduled(cron = "0 15 10 ? * *")
    public void sendEmail(){
        //定制邮件内容
        long totalVisit = statisticMapper.getTotalVisit();
        long totalComment = statisticMapper.getTotalComment();
        //创建邮件主题
        StringBuffer content = new StringBuffer();

        content.append("博客系统总访问量为："+totalVisit+"人次").append("\n");
        content.append("博客系统总评论量为："+totalComment+"人次").append("\n");

        try {
            mailUtils.sendEmail(mailto,"个人博客系统流量统计情况",content.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
