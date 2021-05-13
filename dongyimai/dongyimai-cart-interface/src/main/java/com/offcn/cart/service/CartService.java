package com.offcn.cart.service;

import com.offcn.group.Cart;

import java.util.List;

public interface CartService {

    //添加商品到购物车
    //cartList原购物车信息
    //itemId商品id
    //num添加到购物车数量
    List<Cart> addGoodsToCartList(List<Cart> cartList,Long itemId,Integer num);

    //在redis中取数据
    List<Cart> findCartListFromRedis(String username);
    //在redis中存数据
    void saveCartListToRedis(List<Cart> cartList,String username);

    //合并购物车
    List<Cart> mergeCartList(List<Cart> cartList1,List<Cart> cartList2);
}
