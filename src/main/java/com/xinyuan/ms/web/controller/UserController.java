
package com.xinyuan.ms.web.controller;

import com.xinyuan.ms.service.BasicService;
import com.xinyuan.ms.web.request.LoginRequest;
import com.xinyuan.ms.web.vo.BasicInfoVo;
import com.xinyuan.ms.web.vo.ChanelVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
        import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


/**
 * @Author: hzx
 */

@Api(description = "用户管理")
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    BasicService basicService;


    @ApiOperation(value = "登录", notes = "登录")
    @RequestMapping(value = "login", method = RequestMethod.POST)
    public ResponseEntity<String> login(@RequestBody LoginRequest loginRequest) {
        String msg = basicService.login(loginRequest);
        return ResponseEntity.ok("123");
    }

    @ApiOperation(value = "获取基本信息", notes = "获取基本信息")
    @RequestMapping(value = "getBasicCfg", method = RequestMethod.POST)
    public ResponseEntity<BasicInfoVo> save() {
        BasicInfoVo basicCgf = basicService.getBasicCfg();
        return ResponseEntity.ok(basicCgf);
    }

    @ApiOperation(value = "设置基本信息", notes = "设置基本信息")
    @RequestMapping(value = "save1", method = RequestMethod.POST)
    public ResponseEntity<String> save1(@RequestBody BasicInfoVo basicInfoVo) {
        basicService.setBasicCfg(basicInfoVo);
        return ResponseEntity.ok("123");
    }

    @ApiOperation(value = "通道参数", notes = "通道参数")
    @RequestMapping(value = "save2", method = RequestMethod.POST)
    public ResponseEntity<ChanelVo> save2() {
        ChanelVo chanel = basicService.getChanel();
        return ResponseEntity.ok(chanel);
    }

    /*@ApiOperation(value = "报警输出参数", notes = "报警输出参数")
    @RequestMapping(value = "baojing", method = RequestMethod.POST)
    public ResponseEntity<AlarmOutVo> baojingshuchu(@RequestBody LoginRequest loginRequest) {
        String msg = basicService.login(loginRequest);
        AlarmOutVo alarmOutVo = basicService.showAlarmOutCfg();
        return ResponseEntity.ok(alarmOutVo);
    }*/



}

