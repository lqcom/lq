package com.offcn.page.service.impl;

//import com.alibaba.dubbo.config.annotation.Service;
import com.offcn.mapper.TbGoodsDescMapper;
import com.offcn.mapper.TbGoodsMapper;
import com.offcn.mapper.TbItemCatMapper;
import com.offcn.mapper.TbItemMapper;
import com.offcn.page.setvice.ItemPageService;
import com.offcn.pojo.TbGoods;
import com.offcn.pojo.TbGoodsDesc;
import com.offcn.pojo.TbItem;
import com.offcn.pojo.TbItemExample;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfig;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ItemPageServiceImpl implements ItemPageService {

    @Value("${pagedir}")
    private String pagedir;

    @Autowired
    private FreeMarkerConfig freeMarkerConfig;
    @Autowired
    private TbItemMapper itemMapper;
    @Autowired
    private TbItemCatMapper itemCatMapper;
    @Autowired
    private TbGoodsMapper goodsMapper;
    @Autowired
    private TbGoodsDescMapper goodsDescMapper;

    @Override
    public Boolean genItemHtml(Long goodsId) {

        try {
            System.out.println(goodsId);
            Configuration configuration = freeMarkerConfig.getConfiguration();
            Template template = configuration.getTemplate("item.ftl");
            Map dataModel=new HashMap<>();
            //1.加载商品表数据
            TbGoods goods = goodsMapper.selectByPrimaryKey(goodsId);
            dataModel.put("goods", goods);
            //2.加载商品扩展表数据
            TbGoodsDesc goodsDesc = goodsDescMapper.selectByPrimaryKey(goodsId);
            dataModel.put("goodsDesc", goodsDesc);
            //3.商品分类
            String Category1 = itemCatMapper.selectByPrimaryKey(goods.getCategory1Id()).getName();
            String Category2 = itemCatMapper.selectByPrimaryKey(goods.getCategory2Id()).getName();
            String Category3 = itemCatMapper.selectByPrimaryKey(goods.getCategory3Id()).getName();
            dataModel.put("itemCat1",Category1);
            dataModel.put("itemCat2",Category2);
            dataModel.put("itemCat3",Category3);

            //4SKU列表
            TbItemExample tbItemExample = new TbItemExample();
            TbItemExample.Criteria criteria = tbItemExample.createCriteria();
            criteria.andStatusEqualTo("1");//查找状态等于一的
            criteria.andGoodsIdEqualTo(goodsId);
            tbItemExample.setOrderByClause("is_default desc");//按状态降序，保证第一个为默认
            List<TbItem> tbItems = itemMapper.selectByExample(tbItemExample);
            dataModel.put("itemList",tbItems);



            // Writer out=new FileWriter(pagedir+goodsId+".html");
            Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(pagedir+goodsId+".html")),"UTF-8"));
            template.process(dataModel, out);
            out.close();
            System.out.println(pagedir+goodsId+".html");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    @Override
    public boolean deleteItemHtml(Long[] goodsIds) {
        try {
            for (Long goodsId : goodsIds) {
                new File(pagedir + goodsId + ".html").delete();
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
