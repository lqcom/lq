package com.offcn.sellergoods.service.impl;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.offcn.mapper.TbSpecificationOptionMapper;
import com.offcn.pojo.TbSpecificationOption;
import com.offcn.pojo.TbSpecificationOptionExample;
import com.offcn.pojo.TbSpecificationOptionExample.Criteria;
import com.offcn.sellergoods.service.SpecificationOptionService;

import com.offcn.entity.PageResult;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class SpecificationOptionServiceImpl implements SpecificationOptionService {

	@Autowired
	private TbSpecificationOptionMapper specification_optionMapper;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbSpecificationOption> findAll() {
		return specification_optionMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbSpecificationOption> page=   (Page<TbSpecificationOption>) specification_optionMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbSpecificationOption specification_option) {
		specification_optionMapper.insert(specification_option);		
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbSpecificationOption specification_option){
		specification_optionMapper.updateByPrimaryKey(specification_option);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbSpecificationOption findOne(Long id){
		return specification_optionMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			specification_optionMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbSpecificationOption specification_option, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbSpecificationOptionExample example=new TbSpecificationOptionExample();
		Criteria criteria = example.createCriteria();
		
		if(specification_option!=null){			
						if(specification_option.getOptionName()!=null && specification_option.getOptionName().length()>0){
				criteria.andOptionNameLike("%"+specification_option.getOptionName()+"%");
			}	
		}
		
		Page<TbSpecificationOption> page= (Page<TbSpecificationOption>)specification_optionMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}
	
}
