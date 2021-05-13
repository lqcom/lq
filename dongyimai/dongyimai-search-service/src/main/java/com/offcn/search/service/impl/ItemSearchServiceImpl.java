package com.offcn.search.service.impl;

import com.alibaba.dubbo.common.threadpool.support.fixed.FixedThreadPool;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.offcn.pojo.TbItem;
import com.offcn.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;

import java.util.*;

@Service(timeout = 3000)
public class ItemSearchServiceImpl implements ItemSearchService {
    @Autowired
    private SolrTemplate solrTemplate;

    @Autowired
    private RedisTemplate redisTemplate;
    //查询商品
    @Override
    public Map<String, Object> search(Map searchMap) {

        Map<String,Object> map = new HashMap<>();


//        Query query = new SimpleQuery();
//        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
//        query.addCriteria(criteria);
//        ScoredPage<TbItem> page = solrTemplate.queryForPage(query, TbItem.class);
//        map.put("rows",page.getContent());
        //多条件查询（去中间空格）
        String keywords =(String) searchMap.get("keywords");
        searchMap.put("keywords",keywords.replace(" ",""));
        //高亮查询
        map.putAll(searchList(searchMap));

        //根据关键字查询商品分类
        List categoryList = searchCategoryList(searchMap);
        map.put("categoryList",categoryList);

        //查询品牌和规格
        //.品牌和规格根据分类联动
        String categoryName =(String) searchMap.get("category");
        if (!"".equals(categoryName)){
            //如果前端传来的数据不为空，就按前端数据查
            map.putAll(searchBrandAndSpecList(categoryName));
        }else {
            //否则默认查找第一项
            if (categoryList.size()>0){
                map.putAll(searchBrandAndSpecList((String) categoryList.get(0)));
            }
        }



        return map;
    }

    //将审核通过的商品保存到solr
    @Override
    public void importList(List<TbItem> listItem) {

        for (TbItem item : listItem) {//设置动态域
            Map map = JSON.parseObject(item.getSpec(), Map.class);
            item.setSpecMap(map);
        }

        solrTemplate.saveBeans(listItem);
        solrTemplate.commit();
    }


    //按着goodsId删除solr中的商品
    @Override
    public void deleteByGoodsIds(List goodsIdList) {

        Query query = new SimpleQuery();
        Criteria criteria1 = new Criteria("item_goodsid").in(goodsIdList);
        query.addCriteria(criteria1);
        solrTemplate.delete(query);
        solrTemplate.commit();


    }








    //高亮显示
    private Map searchList(Map searchMap){

        Map<String, Object> map = new HashMap<>();

        //创建高亮查询器
        HighlightQuery query = new SimpleHighlightQuery();
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        //设置查询高亮的域
        HighlightOptions highlightOptions = new HighlightOptions().addField("item_title");
        //拼接前缀
        highlightOptions.setSimplePrefix("<em style='color:red'>");
        //拼接后缀
        highlightOptions.setSimplePostfix("</em>");
        //设置高亮选项
        query.setHighlightOptions(highlightOptions);
        //设置查询条件
        query.addCriteria(criteria);

        //添加筛选条件一 按分类筛选
        if (!"".equals(searchMap.get("category"))){
            Criteria criteria1= new Criteria("item_keywords").is(searchMap.get("category"));
            FilterQuery filterQuery = new SimpleFacetQuery(criteria1);
            query.addFilterQuery(filterQuery);

        }

        //添加筛选条件二 按品牌筛选
        if(!"".equals(searchMap.get("brand"))){

            Criteria criteria1 = new Criteria("item_brand").is(searchMap.get("brand"));
            FilterQuery filterQuery = new SimpleFacetQuery(criteria1);
            query.addFilterQuery(filterQuery);
        }

        //添加筛选条件三 过滤规格
        if(searchMap.get("spec")!=null){
            Map<String,String> specMap =(Map)searchMap.get("spec");
            for (String s : specMap.keySet()) {
                Criteria criteria1 = new Criteria("item_spec_"+s).is(specMap.get(s));
                FilterQuery filterQuery = new SimpleFacetQuery(criteria1);
                query.addFilterQuery(filterQuery);
            }
        }

        //添加筛选条件四 按价格筛选
        if (searchMap.get("price")!=null&&!"".equals(searchMap.get("price"))){


            String price = (String)searchMap.get("price");
            String[] split = price.split("-");
            //如果区间起点不等于0
            if (!"0".equals(split[0])){
                Criteria criteria1 = new Criteria("item_price").greaterThanEqual(split[0]);
                FilterQuery filterQuery = new SimpleFacetQuery(criteria1);
                query.addFilterQuery(filterQuery);
            }
            //如果区间结束不等于*
            if(!"*".equals(split[1])){
                Criteria criteria1 = new Criteria("item_price").lessThanEqual(split[1]);
                FilterQuery filterQuery = new SimpleFacetQuery(criteria1);
                query.addFilterQuery(filterQuery);
            }
        }


        //添加筛选条件五 分页查询
        Integer pageNo = (Integer)searchMap.get("pageNo");//拿到页码
        if (pageNo==null){//如果为空，给默认值
            pageNo=1;
        }
        Integer pageSize = (Integer)searchMap.get("pageSize");//拿到也容量
        if (pageSize==null){//如果为空给默认值
            pageSize=20;
        }
        query.setOffset((pageNo-1)*pageSize);//从第几条记录查询
        query.setRows(pageSize);//配置一页显示多少条数据


        //添加筛选条件六 排序
        String sortValue = (String)searchMap.get("sort");//筛选条件ASC  DESC
        String sortField = (String)searchMap.get("sortField");//筛选字段
        if (sortValue!=null&&!"".equals(sortValue)){
            if ("ASC".equals(sortValue)){
                Sort sort = new Sort(Sort.Direction.ASC,"item_"+sortField);
                query.addSort(sort);
            }
            if ("DESC".equals(sortValue)){
                Sort sort = new Sort(Sort.Direction.DESC,"item_"+sortField);
                query.addSort(sort);
            }

        }


        //查询
        HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(query, TbItem.class);

        for (HighlightEntry<TbItem> tbItemHighlightEntry : page.getHighlighted()) {
            //拿到里面没有拼接高亮的数据
            TbItem entity = tbItemHighlightEntry.getEntity();
            //拿到高亮数据
            if (tbItemHighlightEntry.getHighlights().size()>0&&tbItemHighlightEntry.getHighlights().get(0).getSnipplets().size()>0){
                String s = tbItemHighlightEntry.getHighlights().get(0).getSnipplets().get(0);
                //用高亮数据替换掉没有高亮的数据
                entity.setTitle(s);
            }

        }
        map.put("rows",page.getContent());
        map.put("totalPages",page.getTotalPages());//返回总页数
        map.put("total",page.getTotalElements());//返回总记录数
        return map;


    }

    //查询分类列表
    private List searchCategoryList(Map searchMap){

        List list = new ArrayList();

        //创建普通查询器
        Query query = new SimpleQuery();
        //设置查询条件
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        //添加设置好的查询条件
        query.addCriteria(criteria);

        //设置分组选项
        //按item_category来分组
        GroupOptions groupOptions = new GroupOptions().addGroupByField("item_category");
        query.setGroupOptions(groupOptions);

        //查询
        GroupPage<TbItem> page = solrTemplate.queryForGroupPage(query, TbItem.class);
        List<GroupEntry<TbItem>> item_category = page.getGroupResult("item_category").getGroupEntries().getContent();

        for (GroupEntry<TbItem> tbItemGroupEntry : item_category) {
            //将分组数据存入list集合
            list.add(tbItemGroupEntry.getGroupValue());
        }


        return list;
    }

    //查询品牌和规格列表
    private Map searchBrandAndSpecList(String category){

        Map map = new HashMap();
        //获取对应的模板id
        Object typeId = redisTemplate.boundHashOps("itemCat").get(category);
        if (typeId!=null){

            //查询品牌
            List brandList=(List)redisTemplate.boundHashOps("brandList").get(typeId);

            //查询规格
           List specList = (List) redisTemplate.boundHashOps("specList").get(typeId);

           map.put("brandList",brandList);
           map.put("specList",specList);
        }
        return map;

    }

}
