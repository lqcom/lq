package com.offcn.order.service.impl;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.offcn.mapper.TbOrderItemMapper;
import com.offcn.mapper.TbPayLogMapper;
import com.offcn.pojo.TbOrderItem;
import com.offcn.pojo.TbPayLog;
import com.offcn.util.IdWorker;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.offcn.mapper.TbOrderMapper;
import com.offcn.pojo.TbOrder;
import com.offcn.pojo.TbOrderExample;
import com.offcn.pojo.TbOrderExample.Criteria;
import com.offcn.order.service.OrderService;

import com.offcn.entity.PageResult;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import com.offcn.group.Cart;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
@Transactional
public class OrderServiceImpl implements OrderService {

	@Autowired
	private TbOrderMapper orderMapper;
	@Autowired
	private TbOrderItemMapper orderItemMapper;
	@Autowired
	private RedisTemplate redisTemplate;
	@Autowired
	private IdWorker idWorker;
	@Autowired
	private TbPayLogMapper payLogMapper;
	/**
	 * 查询全部
	 */
	@Override
	public List<TbOrder> findAll() {
		return orderMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbOrder> page=   (Page<TbOrder>) orderMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbOrder order) {

		//订单编号列表集合
		List orderList = new ArrayList();

		//订单的总金额
		double total_money = 0.0;

		//获取当前登录账号的购物车数据
		List<Cart> cartList =(List<Cart>) redisTemplate.boundHashOps("cartList").get(order.getUserId());
		if (cartList==null){
			throw new RuntimeException("你的购物车无数据");
		}
		for (Cart cart : cartList) {
			//新建订单
			TbOrder tbOrder = new TbOrder();
			//将order中的数据复制到tbOrder中
			BeanUtils.copyProperties(order,tbOrder);
			long orderId = idWorker.nextId();
			tbOrder.setOrderId(orderId);//设置id
			tbOrder.setStatus("1");//设置状态为未支付
			tbOrder.setCreateTime(new Date());//设置创建订单时间
			tbOrder.setUpdateTime(new Date());//设置更新订单时间
			tbOrder.setSellerId(cart.getSellerId());//设置商家id
			//定义总金钱
			double money = 0.00;
			//循环商品明细

			for (TbOrderItem tbOrderItem : cart.getOrderItemList()) {
				tbOrderItem.setId(idWorker.nextId());//设置订单明细id
				tbOrderItem.setOrderId(orderId);//设置订单id

				orderList.add(orderId+"");

				tbOrderItem.setSellerId(cart.getSellerId());

				money+=tbOrderItem.getTotalFee().doubleValue();


				//保存订单明细
				orderItemMapper.insert(tbOrderItem);

			}
			//总金额
			total_money += money;

			tbOrder.setPayment(new BigDecimal(money));
			//保存订单
			orderMapper.insert(tbOrder);
		}

		if ("1".equals(order.getPaymentType())){
			TbPayLog tbPayLog = new TbPayLog();
			long outTradeNo = idWorker.nextId();//支付日志编号
			tbPayLog.setOutTradeNo(outTradeNo+"");
			tbPayLog.setCreateTime(new Date());
			String replace = orderList.toString().replace("[", "").replace("]", "").replace(" ", "");
			tbPayLog.setOrderList(replace);//订单编号列表
			tbPayLog.setPayType("1");//支付类型

			BigDecimal total_money_yuan = new BigDecimal(total_money);
			BigDecimal cs = new BigDecimal(100);
			BigDecimal total_money_fen = total_money_yuan.multiply(cs);

			//设置当前和用户要支付的总金额
			tbPayLog.setTotalFee(total_money_fen.toBigInteger().longValue());
			tbPayLog.setTradeState("0");//支付状态
			tbPayLog.setUserId(order.getUserId());//用户ID
			//保存到数据库
			payLogMapper.insert(tbPayLog);
			//保存到redis
			redisTemplate.boundHashOps("payLog").put(order.getUserId(),tbPayLog);

		}

		//下单成功后清空购物车
		redisTemplate.boundHashOps("cartList").delete(order.getUserId());

	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbOrder order){
		orderMapper.updateByPrimaryKey(order);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param
	 * @return
	 */
	@Override
	public TbOrder findOne(Long orderId){
		return orderMapper.selectByPrimaryKey(orderId);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] orderIds) {
		for(Long orderId:orderIds){
			orderMapper.deleteByPrimaryKey(orderId);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbOrder order, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbOrderExample example=new TbOrderExample();
		Criteria criteria = example.createCriteria();
		
		if(order!=null){			
						if(order.getPaymentType()!=null && order.getPaymentType().length()>0){
				criteria.andPaymentTypeLike("%"+order.getPaymentType()+"%");
			}			if(order.getPostFee()!=null && order.getPostFee().length()>0){
				criteria.andPostFeeLike("%"+order.getPostFee()+"%");
			}			if(order.getStatus()!=null && order.getStatus().length()>0){
				criteria.andStatusLike("%"+order.getStatus()+"%");
			}			if(order.getShippingName()!=null && order.getShippingName().length()>0){
				criteria.andShippingNameLike("%"+order.getShippingName()+"%");
			}			if(order.getShippingCode()!=null && order.getShippingCode().length()>0){
				criteria.andShippingCodeLike("%"+order.getShippingCode()+"%");
			}			if(order.getUserId()!=null && order.getUserId().length()>0){
				criteria.andUserIdLike("%"+order.getUserId()+"%");
			}			if(order.getBuyerMessage()!=null && order.getBuyerMessage().length()>0){
				criteria.andBuyerMessageLike("%"+order.getBuyerMessage()+"%");
			}			if(order.getBuyerNick()!=null && order.getBuyerNick().length()>0){
				criteria.andBuyerNickLike("%"+order.getBuyerNick()+"%");
			}			if(order.getBuyerRate()!=null && order.getBuyerRate().length()>0){
				criteria.andBuyerRateLike("%"+order.getBuyerRate()+"%");
			}			if(order.getReceiverAreaName()!=null && order.getReceiverAreaName().length()>0){
				criteria.andReceiverAreaNameLike("%"+order.getReceiverAreaName()+"%");
			}			if(order.getReceiverMobile()!=null && order.getReceiverMobile().length()>0){
				criteria.andReceiverMobileLike("%"+order.getReceiverMobile()+"%");
			}			if(order.getReceiverZipCode()!=null && order.getReceiverZipCode().length()>0){
				criteria.andReceiverZipCodeLike("%"+order.getReceiverZipCode()+"%");
			}			if(order.getReceiver()!=null && order.getReceiver().length()>0){
				criteria.andReceiverLike("%"+order.getReceiver()+"%");
			}			if(order.getInvoiceType()!=null && order.getInvoiceType().length()>0){
				criteria.andInvoiceTypeLike("%"+order.getInvoiceType()+"%");
			}			if(order.getSourceType()!=null && order.getSourceType().length()>0){
				criteria.andSourceTypeLike("%"+order.getSourceType()+"%");
			}			if(order.getSellerId()!=null && order.getSellerId().length()>0){
				criteria.andSellerIdLike("%"+order.getSellerId()+"%");
			}	
		}
		
		Page<TbOrder> page= (Page<TbOrder>)orderMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

	//根据userId在redis中读取数据
	@Override
	public TbPayLog searchPayLogFromRedis(String userId){
		return (TbPayLog) redisTemplate.boundHashOps("payLog").get(userId);

	}

	@Override
	public void updateOrderStatus(String out_trade_no, String transaction_id) {
		//1.修改支付日志状态
		TbPayLog payLog = payLogMapper.selectByPrimaryKey(out_trade_no);
		payLog.setPayTime(new Date());
		payLog.setTradeState("1");//已支付
		payLog.setTransactionId(transaction_id);//交易号
		payLogMapper.updateByPrimaryKey(payLog);
		//2.修改订单状态
		String orderList = payLog.getOrderList();//获取订单号列表
		String[] orderIds = orderList.split(",");//获取订单号数组

		for (String orderId : orderIds) {
			TbOrder order = orderMapper.selectByPrimaryKey(Long.parseLong(orderId));
			if (order != null) {
				order.setStatus("2");//已付款
				orderMapper.updateByPrimaryKey(order);
			}
		}
		//清除redis缓存数据
		redisTemplate.boundHashOps("payLog").delete(payLog.getUserId());
	}

}
