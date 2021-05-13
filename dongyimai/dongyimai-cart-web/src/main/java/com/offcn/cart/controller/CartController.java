package com.offcn.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.offcn.cart.service.CartService;
import com.offcn.entity.Result;
import com.offcn.group.Cart;
import com.offcn.util.CookieUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController()
@RequestMapping("/cart")
public class CartController {

    @Reference(timeout = 6000)
    private CartService cartService;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private HttpServletResponse response;

    @RequestMapping("/findCartList")
    public List<Cart> findCartList(){

        //获取登陆人账号，判断是否登录
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println(username);

        String cartList = CookieUtil.getCookieValue(request, "cartList", "UTF-8");
        if (cartList==null||"".equals(cartList)){
            cartList="[]";
        }
        List<Cart> carts_cookie = JSON.parseArray(cartList, Cart.class);

        //如果未登录
        if (username.equals("anonymousUser")){
            System.out.println("duqubendi");
            return carts_cookie;
        }
        //登录后在redis中查找
        List<Cart> carts_redis = cartService.findCartListFromRedis(username);

        //登录后当cookie中有数据，合并
        if (carts_cookie.size()>0){
            //合并购物车
            carts_redis = cartService.mergeCartList(carts_redis, carts_cookie);
            //合并完清空cookie
            CookieUtil.deleteCookie(request,response,"cartList");
            //合并完购物车后将数据存入redis
            cartService.saveCartListToRedis(carts_redis,username);

        }
        System.out.println("redis shuju");
        return carts_redis;

    }

    @RequestMapping("/addGoodsToCartList")
    @CrossOrigin(origins="http://localhost:9105",allowCredentials="true")//allowCredentials="true"  可以缺省
    public Result addGoodsToCartList(Long itemId,Integer num){

         /*
            1.Access-Control-Allow-Origin字段:
            Access-Control-Allow-Origin是HTML5中定义的一种解决资源跨域的策略
            是通过服务器端返回带有Access-Control-Allow-Origin标识的Response header，用来解决资源的跨域权限问题
            使用方法，在response添加 Access-Control-Allow-Origin
            例如:Access-Control-Allow-Origin:www.google.com
            也可以设置为 * 表示该资源谁都可以用

            2.Access-Control-Allow-Credentials字段
            CORS请求默认不发送Cookie和HTTP认证信息。如果要把Cookie发到服务器，
            一方面要服务器同意，指定Access-Control-Allow-Credentials字段。
            另一方面，开发者必须在AJAX请求中打开withCredentials属性。
            否则，即使服务器同意发送Cookie，浏览器也不会发送。或者，服务器要求设置Cookie，浏览器也不会处理。
         */
//        response.setHeader("Access-Control-Allow-Origin", "http://localhost:9105");
//        response.setHeader("Access-Control-Allow-Credentials", "true");

        //获取登陆人账号，判断是否登录
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println(username);

        try {
            //获取购物车数据
            List<Cart> cartList = findCartList();
            cartList = cartService.addGoodsToCartList(cartList, itemId, num);
            //CookieUtil.setCookie(request,response,"cartList", JSON.toJSONString(cartList),3600*24,"UTF-8");


            if (username.equals("anonymousUser")){
                CookieUtil.setCookie(request,response,"cartList", JSON.toJSONString(cartList),3600*24,"UTF-8");
                return new Result(true,"添加成功");
            }else {
                cartService.saveCartListToRedis(cartList,username);
                return new Result(true,"添加成功");
            }

        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"添加失败");
        }

    }

}
