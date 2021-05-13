package com.offcn.pay.service;

import java.util.Map;

public interface AliPayService {

    Map createNative(String out_trade_no, String total_fee);

    Map<String, String> queryPayStatus(String out_trade_no);
}
