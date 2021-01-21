package com.xinyuan.ms.web.controller;

import com.xinyuan.ms.service.test2Service;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Timer;
import java.util.TimerTask;

@Api(description = "用户管理")
@RestController
@RequestMapping("/user11")
public class testController {

    @Autowired
    test2Service test2Service;


    @ApiOperation(value = "保存", notes = "保存")
    @RequestMapping(value = "save1111", method = RequestMethod.POST)
    public ResponseEntity<String> save1() throws InterruptedException {
//        Timer timer = new Timer();
//        timer.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                try {
//                    test2Service.initMemberFlowUpload("192.168.2.199");
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        },10*60*5,1000*60);
        test2Service.initMemberFlowUpload("192.168.2.199");
        return ResponseEntity.ok("123");
    }

    @ApiOperation(value = "停止监听", notes = "停止监听")
    @RequestMapping(value = "stop", method = RequestMethod.POST)
    public void stop(){
        test2Service.CloseAlarmChan();
    }
}
