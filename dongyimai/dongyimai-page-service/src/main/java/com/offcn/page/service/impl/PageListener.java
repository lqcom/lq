package com.offcn.page.service.impl;

import com.offcn.page.setvice.ItemPageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

/**
 * Created by travelround on 2020/10/26.
 */
@Component
public class PageListener implements MessageListener {

    @Autowired
    private ItemPageService itemPageService;

    @Override
    public void onMessage(Message message) {

        TextMessage textMessage = (TextMessage) message;

        try {

            String text = textMessage.getText();
            System.out.println("生成静态化页面模块接收到消息:"+text);

            itemPageService.genItemHtml(Long.parseLong(text));
            System.out.println("tianjiajingtaiyemianchenggong");
        } catch (Exception e) {

        }
    }
}
