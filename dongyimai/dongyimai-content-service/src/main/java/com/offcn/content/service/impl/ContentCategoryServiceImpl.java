package com.offcn.content.service.impl;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.offcn.mapper.TbContentCategoryMapper;
import com.offcn.pojo.TbContentCategory;
import com.offcn.pojo.TbContentCategoryExample;
import com.offcn.pojo.TbContentCategoryExample.Criteria;
import com.offcn.content.service.ContentCategoryService;

import com.offcn.entity.PageResult;

/**
 * 内容分类服务实现层
 * @author Administrator
 *
 */
@Service
public class ContentCategoryServiceImpl implements ContentCategoryService {

	@Autowired
	private TbContentCategoryMapper content_categoryMapper;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbContentCategory> findAll() {
		return content_categoryMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbContentCategory> page=   (Page<TbContentCategory>) content_categoryMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbContentCategory content_category) {
		content_categoryMapper.insert(content_category);		
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbContentCategory content_category){
		content_categoryMapper.updateByPrimaryKey(content_category);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbContentCategory findOne(Long id){
		return content_categoryMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			content_categoryMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbContentCategory content_category, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbContentCategoryExample example=new TbContentCategoryExample();
		Criteria criteria = example.createCriteria();
		
		if(content_category!=null){			
						if(content_category.getName()!=null && content_category.getName().length()>0){
				criteria.andNameLike("%"+content_category.getName()+"%");
			}	
		}
		
		Page<TbContentCategory> page= (Page<TbContentCategory>)content_categoryMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}
	
}
