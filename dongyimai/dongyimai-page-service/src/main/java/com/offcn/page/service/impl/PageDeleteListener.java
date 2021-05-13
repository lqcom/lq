package com.offcn.page.service.impl;

import com.offcn.page.setvice.ItemPageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

/**
 * Created by travelround on 2020/10/26.
 */
@Component
public class PageDeleteListener implements MessageListener {

    @Autowired
    private ItemPageService itemPageService;

    @Override
    public void onMessage(Message message) {
        ObjectMessage objectMessage = (ObjectMessage) message;
        try {
            Long[] goodsIds = (Long[])objectMessage.getObject();
            System.out.println("删除静态页接收到消息");

            itemPageService.deleteItemHtml(goodsIds);
            System.out.println("shanchuchenggong");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }












}
