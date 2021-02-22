package com.xinyuan.ms.web.controller;

import com.xinyuan.ms.service.FunctionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@Api(description = "功能")
@RestController
@RequestMapping("/function")
public class FunctionController {

    @Autowired
    FunctionService functionService;

    @ApiOperation(value = "开始布防监听", notes = "开始布防监听")
    @RequestMapping(value = "start", method = RequestMethod.POST)
    public ResponseEntity<String> save1() throws InterruptedException {
        functionService.initMemberFlowUpload();
        return ResponseEntity.ok("123");
    }

    @ApiOperation(value = "停止监听", notes = "停止监听")
    @RequestMapping(value = "stop", method = RequestMethod.POST)
    public void stop(){
        functionService.CloseAlarmChan();
    }

    @ApiOperation(value = "设备树", notes = "设备树")
    @RequestMapping(value = "tree", method = RequestMethod.POST)
    public ResponseEntity<List<Map<String, Object>>> tree(){
        List<Map<String, Object>> list = functionService.CreateDeviceTree();
        return ResponseEntity.ok(list);
    }

    @ApiOperation(value = "根据信息查找文件", notes = "根据信息查找文件")
    @RequestMapping(value = "search", method = RequestMethod.POST)
    public ResponseEntity<List<Vector<String>>> search(){
        List<Vector<String>> search = functionService.search();
        return ResponseEntity.ok(search);
    }

    @ApiOperation(value = "根据文件名下载视频", notes = "根据文件名下载视频")
    @RequestMapping(value = "downloadByName", method = RequestMethod.POST)
    public void downloadByName(@RequestBody String fileName){
        functionService.download(fileName);
    }

    @ApiOperation(value = "根据时间下载视频", notes = "根据时间下载视频")
    @RequestMapping(value = "downloadByTime", method = RequestMethod.POST)
    public void downloadByTime(@RequestBody String fileName){
        functionService.download(fileName);
    }
}
