package com.offcn.sellergoods.service.impl;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.offcn.mapper.TbSpecificationOptionMapper;
import com.offcn.pojo.TbSpecificationOption;
import com.offcn.pojo.TbSpecificationOptionExample;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.offcn.mapper.TbTypeTemplateMapper;
import com.offcn.pojo.TbTypeTemplate;
import com.offcn.pojo.TbTypeTemplateExample;
import com.offcn.pojo.TbTypeTemplateExample.Criteria;
import com.offcn.sellergoods.service.TypeTemplateService;

import com.offcn.entity.PageResult;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class TypeTemplateServiceImpl implements TypeTemplateService {

	@Autowired
	private TbTypeTemplateMapper type_templateMapper;

	@Autowired
	private TbSpecificationOptionMapper specificationOptionMapper;

	@Autowired
	private RedisTemplate redisTemplate;
	/**
	 * 查询全部
	 */
	@Override
	public List<TbTypeTemplate> findAll() {
		return type_templateMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbTypeTemplate> page=   (Page<TbTypeTemplate>) type_templateMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbTypeTemplate type_template) {
		type_templateMapper.insert(type_template);		
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbTypeTemplate type_template){
		type_templateMapper.updateByPrimaryKey(type_template);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbTypeTemplate findOne(Long id){
		return type_templateMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			type_templateMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbTypeTemplate type_template, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbTypeTemplateExample example=new TbTypeTemplateExample();
		Criteria criteria = example.createCriteria();
		
		if(type_template!=null){			
						if(type_template.getName()!=null && type_template.getName().length()>0){
				criteria.andNameLike("%"+type_template.getName()+"%");
			}			if(type_template.getSpecIds()!=null && type_template.getSpecIds().length()>0){
				criteria.andSpecIdsLike("%"+type_template.getSpecIds()+"%");
			}			if(type_template.getBrandIds()!=null && type_template.getBrandIds().length()>0){
				criteria.andBrandIdsLike("%"+type_template.getBrandIds()+"%");
			}			if(type_template.getCustomAttributeItems()!=null && type_template.getCustomAttributeItems().length()>0){
				criteria.andCustomAttributeItemsLike("%"+type_template.getCustomAttributeItems()+"%");
			}	
		}
		
		Page<TbTypeTemplate> page= (Page<TbTypeTemplate>)type_templateMapper.selectByExample(example);

			saveToRedis();//存入数据到缓存
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Override
	public List<Map> selectOptionList() {
		return type_templateMapper.selectOptionList();
	}

	@Override
	public List<Map> findSpecList(Long id) {

		//查询模板表中对应的规格
		TbTypeTemplate tbTypeTemplate = type_templateMapper.selectByPrimaryKey(id);
		List<Map> list= JSON.parseArray(tbTypeTemplate.getSpecIds(),Map.class);

		//[{"id":27,"text":"网络"},{"id":32,"text":"机身内存"}]
		for (Map map : list) {

			//拿到每个规格的id
			Long specId = new Long((Integer) map.get("id"));

			//在规格选项表中查询对应的规格选项
			TbSpecificationOptionExample tbSpecificationOptionExample = new TbSpecificationOptionExample();
			TbSpecificationOptionExample.Criteria criteria = tbSpecificationOptionExample.createCriteria();
			criteria.andSpecIdEqualTo(specId);
			List<TbSpecificationOption> tbSpecificationOptions = specificationOptionMapper.selectByExample(tbSpecificationOptionExample);
			map.put("options",tbSpecificationOptions);
		}

		return list;
	}


	//数据存储Redis缓存方法
	private void saveToRedis(){

		List<TbTypeTemplate> all = findAll();
		for (TbTypeTemplate tbTypeTemplate : all) {
			//查询模板对应的皮品牌
			List<Map> list = JSON.parseArray(tbTypeTemplate.getBrandIds(), Map.class);
			//缓存到Redis
			redisTemplate.boundHashOps("brandList").put(tbTypeTemplate.getId(),list);

			//查询模板对应的规格和规格选项
			List<Map> specList = findSpecList(tbTypeTemplate.getId());
			//缓存到Redis
			redisTemplate.boundHashOps("specList").put(tbTypeTemplate.getId(),specList);
		}
	}

}
