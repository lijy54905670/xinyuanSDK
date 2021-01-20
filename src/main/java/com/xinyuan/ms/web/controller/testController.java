package com.xinyuan.ms.web.controller;

import com.xinyuan.ms.service.test2Service;
import com.xinyuan.ms.service.testService;
import com.xinyuan.ms.web.request.LoginRequest;
import com.xinyuan.ms.web.vo.BasicInfoVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@Api(description = "用户管理")
@RestController
@RequestMapping("/user11")
public class testController {
    @Autowired
    testService testService1;
    @Autowired
    test2Service test2Service;

    @ApiOperation(value = "保存", notes = "保存")
    @RequestMapping(value = "save111", method = RequestMethod.POST)
    public ResponseEntity<String> save() {
      testService1.alarm();
        return ResponseEntity.ok("123");
    }

    @ApiOperation(value = "保存", notes = "保存")
    @RequestMapping(value = "save1111", method = RequestMethod.POST)
    public ResponseEntity<String> save1() throws InterruptedException {
       test2Service.initMemberFlowUpload("192.168.2.199");
        return ResponseEntity.ok("123");
    }
}
