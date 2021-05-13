package com.offcn.sellergoods.controller;


import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Security;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/login")
public class LoginController {

    @RequestMapping("/name")
    public Map name(){

        String name = SecurityContextHolder.getContext().getAuthentication().getName();

        HashMap hashMap = new HashMap();
        hashMap.put("loginName",name);

        return hashMap;
    }
}
