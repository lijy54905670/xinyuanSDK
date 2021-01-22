package com.xinyuan.ms.web.controller;

import com.xinyuan.ms.service.test2Service;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@Api(description = "用户管理")
@RestController
@RequestMapping("/user11")
public class testController {

    @Autowired
    test2Service test2Service;


    @ApiOperation(value = "开始布防监听", notes = "开始布防监听")
    @RequestMapping(value = "save1111", method = RequestMethod.POST)
    public ResponseEntity<String> save1() throws InterruptedException {
        test2Service.initMemberFlowUpload("192.168.2.199");
        return ResponseEntity.ok("123");
    }

    @ApiOperation(value = "停止监听", notes = "停止监听")
    @RequestMapping(value = "stop", method = RequestMethod.POST)
    public void stop(){
        test2Service.CloseAlarmChan();
    }

    @ApiOperation(value = "树", notes = "树")
    @RequestMapping(value = "tree", method = RequestMethod.POST)
    public ResponseEntity<List<Map<String, Object>>> tree(){
        List<Map<String, Object>> list = test2Service.CreateDeviceTree();
        return ResponseEntity.ok(list);
    }

    @ApiOperation(value = "根据信息查找文件", notes = "根据信息查找文件")
    @RequestMapping(value = "search", method = RequestMethod.POST)
    public ResponseEntity<List<Vector<String>>> search(){
        List<Vector<String>> search = test2Service.search();
        return ResponseEntity.ok(search);
    }


    @ApiOperation(value = "根据文件名下载视频", notes = "根据文件名下载视频")
    @RequestMapping(value = "downloadByName", method = RequestMethod.POST)
    public void downloadByName(@RequestBody String fileName){
        test2Service.download(fileName);
    }

    @ApiOperation(value = "根据时间下载视频", notes = "根据时间下载视频")
    @RequestMapping(value = "downloadByTime", method = RequestMethod.POST)
    public void downloadByTime(@RequestBody String fileName){
        test2Service.download(fileName);
    }


}
