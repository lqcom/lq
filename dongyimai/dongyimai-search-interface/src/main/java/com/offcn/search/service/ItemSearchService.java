package com.offcn.search.service;


import com.offcn.pojo.TbItem;

import java.util.List;
import java.util.Map;

public interface ItemSearchService {
    Map<String, Object> search(Map searchMap);

    //审核通过，在solr中添加数据
    void importList(List<TbItem> listItem);

    //按着goodsId删除solr中的商品
    void deleteByGoodsIds(List goodsIdList);

}
