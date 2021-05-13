package com.offcn.pay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.offcn.pay.service.AliPayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class AliPayServiceImpl implements AliPayService {

    @Autowired
    private AlipayClient alipayClient;

    @Override
    public Map createNative(String out_trade_no, String total_fee) {

        Map<String,String> map = new HashMap();
        Long longfen = Long.parseLong(total_fee);
        BigDecimal fen = new BigDecimal(longfen);
        BigDecimal cs = new BigDecimal(100);
        BigDecimal yuan = fen.divide(cs);

        AlipayTradePrecreateRequest request = new AlipayTradePrecreateRequest();
        request.setBizContent("{" +
                "    \"out_trade_no\":\"" + out_trade_no + "\"," +
                "    \"total_amount\":\"" + yuan.doubleValue() + "\"," +
                "    \"subject\":\"测试购买商品001\"," +
                "    \"store_id\":\"xa_001\"," +
                "    \"timeout_express\":\"90m\"}");//设置业务参数
        try {
            AlipayTradePrecreateResponse response = alipayClient.execute(request);

            //获取响应信息
            String code = response.getCode();//状态码
            System.out.println("状态码"+code);
            String body = response.getBody();//响应全部数据
            System.out.println("全部数据"+body);

            if (code.equals("10000")){
                map.put("qrcode", response.getQrCode());
                map.put("out_trade_no", response.getOutTradeNo());
                map.put("total_fee", yuan.doubleValue()+"");
                System.out.println("qrcode"+response.getQrCode());
                System.out.println("out_trade_no"+response.getOutTradeNo());
                System.out.println("total_fee"+total_fee);
            }else {
                System.out.println("预下单接口调用失败"+body);
            }

        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        return map;
    }

    @Override
    public Map<String, String> queryPayStatus(String out_trade_no) {
        Map<String, String> map = new HashMap<String, String>();
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        request.setBizContent("{" +
                "    \"out_trade_no\":\"" + out_trade_no + "\"," +
                "    \"trade_no\":\"\"}"); //设置业务参数

        //发出请求
        try {
            AlipayTradeQueryResponse response = alipayClient.execute(request);
            String code = response.getCode();
            System.out.println("返回值1:" + response.getBody());
            if (code.equals("10000")) {
                //System.out.println("返回值2:"+response.getBody());
                map.put("out_trade_no", out_trade_no);
                map.put("tradestatus", response.getTradeStatus());
                map.put("transaction_id", response.getTradeNo());
            }
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        return map;
    }

}
