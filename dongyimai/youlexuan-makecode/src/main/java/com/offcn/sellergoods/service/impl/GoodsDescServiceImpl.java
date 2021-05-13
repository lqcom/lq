package com.offcn.sellergoods.service.impl;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.offcn.mapper.TbGoodsDescMapper;
import com.offcn.pojo.TbGoodsDesc;
import com.offcn.pojo.TbGoodsDescExample;
import com.offcn.pojo.TbGoodsDescExample.Criteria;
import com.offcn.sellergoods.service.GoodsDescService;

import com.offcn.entity.PageResult;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class GoodsDescServiceImpl implements GoodsDescService {

	@Autowired
	private TbGoodsDescMapper goods_descMapper;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbGoodsDesc> findAll() {
		return goods_descMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbGoodsDesc> page=   (Page<TbGoodsDesc>) goods_descMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbGoodsDesc goods_desc) {
		goods_descMapper.insert(goods_desc);		
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbGoodsDesc goods_desc){
		goods_descMapper.updateByPrimaryKey(goods_desc);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbGoodsDesc findOne(Long goodsId){
		return goods_descMapper.selectByPrimaryKey(goodsId);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] goodsIds) {
		for(Long goodsId:goodsIds){
			goods_descMapper.deleteByPrimaryKey(goodsId);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbGoodsDesc goods_desc, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbGoodsDescExample example=new TbGoodsDescExample();
		Criteria criteria = example.createCriteria();
		
		if(goods_desc!=null){			
						if(goods_desc.getIntroduction()!=null && goods_desc.getIntroduction().length()>0){
				criteria.andIntroductionLike("%"+goods_desc.getIntroduction()+"%");
			}			if(goods_desc.getSpecificationItems()!=null && goods_desc.getSpecificationItems().length()>0){
				criteria.andSpecificationItemsLike("%"+goods_desc.getSpecificationItems()+"%");
			}			if(goods_desc.getCustomAttributeItems()!=null && goods_desc.getCustomAttributeItems().length()>0){
				criteria.andCustomAttributeItemsLike("%"+goods_desc.getCustomAttributeItems()+"%");
			}			if(goods_desc.getItemImages()!=null && goods_desc.getItemImages().length()>0){
				criteria.andItemImagesLike("%"+goods_desc.getItemImages()+"%");
			}			if(goods_desc.getPackageList()!=null && goods_desc.getPackageList().length()>0){
				criteria.andPackageListLike("%"+goods_desc.getPackageList()+"%");
			}			if(goods_desc.getSaleService()!=null && goods_desc.getSaleService().length()>0){
				criteria.andSaleServiceLike("%"+goods_desc.getSaleService()+"%");
			}	
		}
		
		Page<TbGoodsDesc> page= (Page<TbGoodsDesc>)goods_descMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}
	
}
