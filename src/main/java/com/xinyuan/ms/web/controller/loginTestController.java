package com.xinyuan.ms.web.controller;

import com.xinyuan.ms.service.loginTestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class loginTestController {
    @Autowired
    loginTestService loginTestService;
    @RequestMapping(value = "loginTest", method = RequestMethod.POST)
    public String  login(String ip,short port,String userName,String pwd) {
        boolean login = loginTestService.login(ip, port, userName, pwd);
        if (login == true){
            return "denglu";
        }else {
            return "index11";
        }
    }
}
