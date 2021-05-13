package com.offcn.sellergoods.service.impl;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.offcn.group.Goods;
import com.offcn.mapper.*;
import com.offcn.pojo.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.offcn.pojo.TbGoodsExample.Criteria;
import com.offcn.sellergoods.service.GoodsService;

import com.offcn.entity.PageResult;
import org.springframework.transaction.annotation.Transactional;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
@Transactional
public class GoodsServiceImpl implements GoodsService {

	@Autowired
	private TbGoodsMapper goodsMapper;
	@Autowired
	private TbGoodsDescMapper goodsDescMapper;
	@Autowired
	private TbBrandMapper brandMapper;
	@Autowired
	private TbItemCatMapper itemCatMapper;
	@Autowired
	private TbSellerMapper sellerMapper;
	@Autowired
	private TbItemMapper itemMapper;
	/**
	 * 查询全部
	 */
	@Override
	public List<TbGoods> findAll() {
		return goodsMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbGoods> page=   (Page<TbGoods>) goodsMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbGoods goods) {
		goodsMapper.insert(goods);
	}

	@Override
	public void add(Goods goods) {
		goods.getGoods().setAuditStatus("0");//设置未申请状态
		goodsMapper.insert(goods.getGoods());
		goods.getGoodsDesc().setGoodsId(goods.getGoods().getId());//设置ID
		goodsDescMapper.insert(goods.getGoodsDesc());//插入商品扩展数据

		saveItemList(goods);
//		//判断是否启动规格选项
//		if ("1".equals(goods.getGoods().getIsEnableSpec())){
//			System.out.println("laile");
//			for (TbItem tbItem : goods.getItemList()) {
//
//				//设置标题
//				String goodsName = goods.getGoods().getGoodsName();//拿到主标题
//				Map<String,Object> specMap = JSON.parseObject(tbItem.getSpec());//拿到规格选项
//				for (String key : specMap.keySet()) {//循环遍历specMap用来拼接主标题
//					goodsName += " " + specMap.get(key);
//				}
//				tbItem.setTitle(goodsName);
//
//				setItemValus(goods,tbItem);
//				//持久化数据库
//				itemMapper.insert(tbItem);
//
//			}
//		}else {
//			TbItem item = new TbItem();
//			item.setTitle(goods.getGoods().getGoodsName());//商品KPU+规格描述串作为SKU名称
//			item.setPrice(goods.getGoods().getPrice());//价格
//			item.setStatus("1");//状态
//			item.setIsDefault("1");//是否默认
//			item.setNum(99999);//库存数量
//			item.setSpec("{}");
//			setItemValus(goods, item);
//			itemMapper.insert(item);
//		}


	}

	private void saveItemList(Goods goods){
		//判断是否启动规格选项
		if ("1".equals(goods.getGoods().getIsEnableSpec())){
			System.out.println("laile");
			for (TbItem tbItem : goods.getItemList()) {

				//设置标题
				String goodsName = goods.getGoods().getGoodsName();//拿到主标题
				Map<String,Object> specMap = JSON.parseObject(tbItem.getSpec());//拿到规格选项
				for (String key : specMap.keySet()) {//循环遍历specMap用来拼接主标题
					goodsName += " " + specMap.get(key);
				}
				tbItem.setTitle(goodsName);

				setItemValus(goods,tbItem);
				//持久化数据库
				itemMapper.insert(tbItem);

			}
		}else {
			TbItem item = new TbItem();
			item.setTitle(goods.getGoods().getGoodsName());//商品KPU+规格描述串作为SKU名称
			item.setPrice(goods.getGoods().getPrice());//价格
			item.setStatus("1");//状态
			item.setIsDefault("1");//是否默认
			item.setNum(99999);//库存数量
			item.setSpec("{}");
			setItemValus(goods, item);
			itemMapper.insert(item);
		}
	}


	private void setItemValus(Goods goods, TbItem tbItem) {
		//商品图片，只添加第一张
		List<Map> list = JSON.parseArray(goods.getGoodsDesc().getItemImages(), Map.class);
		if (list.size()>0){
			tbItem.setImage((String)list.get(0).get("url"));
		}

		//商品三级分类编号
		tbItem.setCategoryid(goods.getGoods().getCategory3Id());

		//创建时间
		tbItem.setCreateTime(new Date());

		//修改时间
		tbItem.setUpdateTime(new Date());

		//商品SPU的id
		tbItem.setGoodsId(goods.getGoods().getId());

		//商家id
		tbItem.setSellerId(goods.getGoods().getSellerId());

		//品牌名称
		TbBrand tbBrand = brandMapper.selectByPrimaryKey(goods.getGoods().getBrandId());
		tbItem.setBrand(tbBrand.getName());

		//分类名称
		TbItemCat tbItemCat = itemCatMapper.selectByPrimaryKey(goods.getGoods().getCategory3Id());
		tbItem.setCategory(tbItemCat.getName());

		//商家名称
		TbSeller tbSeller = sellerMapper.selectByPrimaryKey(goods.getGoods().getSellerId());
		tbItem.setSeller(tbSeller.getName());

		//商家编号
		tbItem.setSellerId(goods.getGoods().getSellerId());

	}
	/**
	 * 修改
	 */
	@Override
	public void update(Goods goods){
		//goodsMapper.updateByPrimaryKey(goods);


		//设置未申请状态
		goods.getGoods().setAuditStatus("0");
		//保存商品表
		goodsMapper.updateByPrimaryKey(goods.getGoods());
		//保存商品扩展表
		goodsDescMapper.updateByPrimaryKey(goods.getGoodsDesc());

		//删除原有的sku列数据
		TbItemExample tbItemExample = new TbItemExample();
		TbItemExample.Criteria criteria = tbItemExample.createCriteria();
		criteria.andGoodsIdEqualTo(goods.getGoods().getId());
		itemMapper.deleteByExample(tbItemExample);
		//添加新数据
		saveItemList(goods);
	}

	@Override
	public void update(TbGoods goods){
		goodsMapper.updateByPrimaryKey(goods);
	}
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public Goods findOne(Long id){
		//return goodsMapper.selectByPrimaryKey(id);
		//查询商品SPU信息
		TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
		TbGoodsDesc tbGoodsDesc = goodsDescMapper.selectByPrimaryKey(id);
		//查询商品SKU信息
		TbItemExample tbItemExample = new TbItemExample();
		TbItemExample.Criteria criteria = tbItemExample.createCriteria();
		criteria.andGoodsIdEqualTo(id);
		List<TbItem> tbItems = itemMapper.selectByExample(tbItemExample);

		Goods goods = new Goods();
		goods.setGoods(tbGoods);
		goods.setGoodsDesc(tbGoodsDesc);
		goods.setItemList(tbItems);
		return goods;

	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			//goodsMapper.deleteByPrimaryKey(id);
			TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
			tbGoods.setIsDelete("1");
			goodsMapper.updateByPrimaryKey(tbGoods);
		}
		//删除后将商品状态设置为禁用
		List<TbItem> itemListByGoodsIdandStatus = findItemListByGoodsIdandStatus(ids, "1");
		for (TbItem item : itemListByGoodsIdandStatus) {
			item.setStatus("0");
			itemMapper.updateByPrimaryKey(item);
		}

	}
	
	
		@Override
	public PageResult findPage(TbGoods goods, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbGoodsExample example=new TbGoodsExample();
		Criteria criteria = example.createCriteria();
		criteria.andIsDeleteIsNull();
		if(goods!=null){

			//精确匹配商家编号

						if(goods.getSellerId()!=null && goods.getSellerId().length()>0){
				//criteria.andSellerIdLike("%"+goods.getSellerId()+"%");
							criteria.andSellerIdEqualTo(goods.getSellerId());
			}
			if(goods.getGoodsName()!=null && goods.getGoodsName().length()>0){
				criteria.andGoodsNameLike("%"+goods.getGoodsName()+"%");
			}			if(goods.getAuditStatus()!=null && goods.getAuditStatus().length()>0){
				criteria.andAuditStatusLike("%"+goods.getAuditStatus()+"%");
			}			if(goods.getIsMarketable()!=null && goods.getIsMarketable().length()>0){
				criteria.andIsMarketableLike("%"+goods.getIsMarketable()+"%");
			}			if(goods.getCaption()!=null && goods.getCaption().length()>0){
				criteria.andCaptionLike("%"+goods.getCaption()+"%");
			}			if(goods.getSmallPic()!=null && goods.getSmallPic().length()>0){
				criteria.andSmallPicLike("%"+goods.getSmallPic()+"%");
			}			if(goods.getIsEnableSpec()!=null && goods.getIsEnableSpec().length()>0){
				criteria.andIsEnableSpecLike("%"+goods.getIsEnableSpec()+"%");
			}			if(goods.getIsDelete()!=null && goods.getIsDelete().length()>0){
				criteria.andIsDeleteLike("%"+goods.getIsDelete()+"%");
			}	
		}
		
		Page<TbGoods> page= (Page<TbGoods>)goodsMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}


	@Override
	public void updateStatus(Long[] ids, String status) {
		for (Long id : ids) {
			TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
			tbGoods.setAuditStatus(status);
			goodsMapper.updateByPrimaryKey(tbGoods);
		}
	}


	@Override
	public List<TbItem> findItemListByGoodsIdandStatus(Long[] goodsId, String status) {

		TbItemExample tbItemExample = new TbItemExample();
		TbItemExample.Criteria criteria = tbItemExample.createCriteria();
		criteria.andGoodsIdIn(Arrays.asList(goodsId));
		criteria.andStatusEqualTo(status);
		List<TbItem> tbItems = itemMapper.selectByExample(tbItemExample);

		return tbItems;
	}
}
