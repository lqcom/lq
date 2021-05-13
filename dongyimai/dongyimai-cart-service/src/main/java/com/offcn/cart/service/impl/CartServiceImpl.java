package com.offcn.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.offcn.cart.service.CartService;
import com.offcn.group.Cart;
import com.offcn.mapper.TbItemMapper;
import com.offcn.pojo.TbItem;
import com.offcn.pojo.TbOrderItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
@Service
@Transactional
public class CartServiceImpl implements CartService {

    @Autowired
    private TbItemMapper tbItemMapper;
    @Autowired
    private RedisTemplate redisTemplate;


    //添加商品到购物车
    //cartList原购物车信息
    //itemId商品id
    //num添加到购物车数量
    @Override
    public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num) {

        //根据商品SKU id查询出SKU信息

        TbItem item = tbItemMapper.selectByPrimaryKey(itemId);
        //判断商品是否存在，状态是否正常
        if (item==null){
            throw new RuntimeException("商品不存在");
        }else if (!"1".equals(item.getStatus())){
            throw new RuntimeException("商品状态不对");
        }

       //获取商家id
        String sellerId = item.getSellerId();

        //根据商家id判断购物车集合中是否存在该商家购物车
        Cart cart = searchCartBySellerId(cartList, sellerId);

        //当购物车中没有该商家
        if (cart==null){
            //新家购物车对象
            cart = new Cart();
            cart.setSellerId(sellerId);
            cart.setSellerName(item.getSeller());
            TbOrderItem orderItem = createOrderItem(item, num);
            List<TbOrderItem> orderItemList = new ArrayList<>();
            orderItemList.add(orderItem);
            cart.setOrderItemList(orderItemList);
            cartList.add(cart);
        }else {//当购物车集合中有该商家购物车
            //判断该商家是否有该商品
            TbOrderItem tbOrderItem = searchOrderItemByItemId(cart.getOrderItemList(), itemId);
            //如果没有
            if (tbOrderItem==null){
                //添加该商品明细
                tbOrderItem = createOrderItem(item, num);
                cart.getOrderItemList().add(tbOrderItem);
            }else {
                //如果有，在购物车明细上修改数量和金额
                tbOrderItem.setNum(tbOrderItem.getNum()+num);
                tbOrderItem.setTotalFee(new BigDecimal(tbOrderItem.getNum()*tbOrderItem.getPrice().doubleValue()));

                //如果数量操作后数量小于等于0移出该商品明细
                if (tbOrderItem.getNum()<=0){
                    cart.getOrderItemList().remove(tbOrderItem);
                }

                //如果移出完商品明细后该商家商品明细小于等于0，移出该商家
                if (cart.getOrderItemList().size()<=0){
                    cartList.remove(cart);
                }

            }

        }
        return cartList;
    }

    //根据商家id判断购物车集合中是否存在该商家购物车
    private Cart searchCartBySellerId(List<Cart> cartList,String sellerId){

        if (cartList!=null&&cartList.size()>0){
            for (Cart cart : cartList) {
                if (cart.getSellerId().equals(sellerId)){
                    return cart;
                }

            }
        }
        return null;
    }

    //创建商品明细
    private TbOrderItem createOrderItem(TbItem item,Integer num){

        //判断数量是否合法
        if (num<=0){
            throw new RuntimeException("数量不合法");
        }

        //添加数据
        TbOrderItem tbOrderItem = new TbOrderItem();
        tbOrderItem.setGoodsId(item.getGoodsId());
        tbOrderItem.setItemId(item.getId());
        tbOrderItem.setNum(num);
        tbOrderItem.setPicPath(item.getImage());
        tbOrderItem.setPrice(item.getPrice());
        tbOrderItem.setSellerId(item.getSellerId());
        tbOrderItem.setTitle(item.getTitle());
        tbOrderItem.setTotalFee(new BigDecimal(item.getPrice().doubleValue()*num));
        return tbOrderItem;
    }

    //根据商品明细id查询
    private TbOrderItem searchOrderItemByItemId(List<TbOrderItem> tbOrderItemList,Long itemId){

        for (TbOrderItem tbOrderItem : tbOrderItemList) {
            if (tbOrderItem.getItemId().longValue()==itemId.longValue()){
                return tbOrderItem;
            }
        }

        return null;
    }

    //在redis中取数据
    @Override
    public List<Cart> findCartListFromRedis(String username) {

        System.out.println("在redis中du数据");
        List<Cart> cartList = (List<Cart>)redisTemplate.boundHashOps("cartList").get(username);
        if (cartList==null){
            cartList = new ArrayList<Cart>();
        }
        return cartList;
    }

    //在redis中存数据
    @Override
    public void saveCartListToRedis(List<Cart> cartList, String username) {
        System.out.println("在redis中xie数据");
        redisTemplate.boundHashOps("cartList").put(username,cartList);
    }

    //合并购物车
    @Override
    public List<Cart> mergeCartList(List<Cart> cartList1, List<Cart> cartList2) {

        System.out.println("hebinggouwuche");
        for (Cart cart : cartList2) {
            for (TbOrderItem tbOrderItem : cart.getOrderItemList()) {
                 cartList1 = addGoodsToCartList(cartList1, tbOrderItem.getItemId(), tbOrderItem.getNum());
            }
        }

        return cartList1;
    }
}
