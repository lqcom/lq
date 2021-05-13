package com.offcn.sellergoods.controller;
import java.util.Arrays;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.offcn.group.Goods;
//import com.offcn.page.setvice.ItemPageService;
import com.offcn.pojo.TbItem;
//import com.offcn.search.service.ItemSearchService;
import com.offcn.sellergoods.service.GoodsDescService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.dubbo.config.annotation.Reference;
import com.offcn.pojo.TbGoods;
import com.offcn.sellergoods.service.GoodsService;

import com.offcn.entity.PageResult;
import com.offcn.entity.Result;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

/**
 * controller
 * @author Administrator
 *
 */
@RestController
@RequestMapping("/goods")
public class GoodsController {

	@Reference
	private GoodsService goodsService;

//	@Reference
//	private ItemSearchService itemSearchService;
//	@Reference(timeout = 40000)
//	private ItemPageService itemPageService;

	@Autowired
	private Destination queueSolrDestination;//用于发送solr导入的消息
	@Autowired
	private Destination topicPageDestination;//生成静态页面发布订阅模型
	@Autowired
	private JmsTemplate jmsTemplate;
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findAll")
	public List<TbGoods> findAll(){			
		return goodsService.findAll();
	}
	
	
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findPage")
	public PageResult  findPage(int page,int rows){			
		return goodsService.findPage(page, rows);
	}
	
	/**
	 * 增加
	 * @param goods
	 * @return
	 */
	@RequestMapping("/add")
	public Result add(@RequestBody TbGoods goods){
		try {
			goodsService.add(goods);
			return new Result(true, "增加成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "增加失败");
		}
	}
	
	/**
	 * 修改
	 * @param goods
	 * @return
	 */
	@RequestMapping("/update")
	public Result update(@RequestBody TbGoods goods){
		try {
			goodsService.update(goods);
			return new Result(true, "修改成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "修改失败");
		}
	}	
	
	/**
	 * 获取实体
	 * @param id
	 * @return
	 */
	@RequestMapping("/findOne")
	public Goods findOne(Long id){
		return goodsService.findOne(id);		
	}
	
	/**
	 * 批量删除
	 * @param ids
	 * @return
	 */
	@RequestMapping("/delete")
	public Result delete(Long [] ids){
		try {
			goodsService.delete(ids);
			return new Result(true, "删除成功"); 
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "删除失败");
		}
	}
	
		/**
	 * 查询+分页
	 * @param
	 * @param page
	 * @param rows
	 * @return
	 */
	@RequestMapping("/search")
	public PageResult search(@RequestBody TbGoods goods, int page, int rows  ){
		return goodsService.findPage(goods, page, rows);		
	}


	//审核
	@RequestMapping("/updateStatus")
	public Result updateStatus(Long[] ids,String status ){

		try {
			goodsService.updateStatus(ids,status);
			if ("1".equals(status)){
				//在数据中查找满足条件的商品
				List<TbItem> itemListByGoodsIdandStatus = goodsService.findItemListByGoodsIdandStatus(ids, status);
				if (itemListByGoodsIdandStatus.size()>0){
					//将查出来的数据存入solr
//					itemSearchService.importList(itemListByGoodsIdandStatus);

					final String jsonString = JSON.toJSONString(itemListByGoodsIdandStatus);
					jmsTemplate.send(queueSolrDestination, new MessageCreator() {
						@Override
						public Message createMessage(Session session) throws JMSException {
							return session.createTextMessage(jsonString);
						}
					});
				}
				//审核通过后生成静态页面
//				for (Long id : ids) {
////					itemPageService.genItemHtml(id);
//
//
//				}
				for (final Long id : ids) {
					jmsTemplate.send(topicPageDestination, new MessageCreator() {
						@Override
						public Message createMessage(Session session) throws JMSException {
							return session.createTextMessage(id+"");
						}
					});
				}

			}


			return new Result(true,"操作成功");
		} catch (Exception e) {
			e.printStackTrace();
			return  new Result(false,"操作失败");
		}

	}

//	//生成静态页
//	@RequestMapping("/genHtml")
//	public void genHtml(Long goodsId){
//		itemPageService.genItemHtml(goodsId);
//	}
}
