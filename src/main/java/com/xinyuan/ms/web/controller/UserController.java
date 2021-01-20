
package com.xinyuan.ms.web.controller;

//import com.xinyuan.ms.ClientDemo.ClientDemo;
import com.xinyuan.ms.service.BasicService;
import com.xinyuan.ms.service.MemberFlowUploadService;
import com.xinyuan.ms.web.request.LoginRequest;
import com.xinyuan.ms.web.vo.AlarmOutVo;
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

    @Autowired
    MemberFlowUploadService memberFlowUploadService;

    @ApiOperation(value = "保存", notes = "保存")
    @RequestMapping(value = "save", method = RequestMethod.POST)
    public ResponseEntity<BasicInfoVo> save(@RequestBody LoginRequest loginRequest) {
        String msg = basicService.login(loginRequest);
        BasicInfoVo basicCgf = basicService.getBasicCfg();
        return ResponseEntity.ok(basicCgf);
    }

    @ApiOperation(value = "保存", notes = "保存")
    @RequestMapping(value = "save1", method = RequestMethod.POST)
    public ResponseEntity<String> save1(@RequestBody BasicInfoVo basicInfoVo) {
        String msg = basicService.login(new LoginRequest("192.168.2.200",(short)8000,"admin","xinyuan1,"));
        basicService.setBasicCfg(basicInfoVo);
        return ResponseEntity.ok(msg);
    }

    @ApiOperation(value = "通道参数", notes = "通道参数")
    @RequestMapping(value = "save2", method = RequestMethod.POST)
    public ResponseEntity<ChanelVo> save2(@RequestBody LoginRequest loginRequest) {
        String msg = basicService.login(loginRequest);
        ChanelVo chanel = basicService.getChanel();
        return ResponseEntity.ok(chanel);
    }

    @ApiOperation(value = "报警输出参数", notes = "报警输出参数")
    @RequestMapping(value = "baojing", method = RequestMethod.POST)
    public ResponseEntity<AlarmOutVo> baojingshuchu(@RequestBody LoginRequest loginRequest) {
        String msg = basicService.login(loginRequest);
        AlarmOutVo alarmOutVo = basicService.showAlarmOutCfg();
        return ResponseEntity.ok(alarmOutVo);
    }

    @ApiOperation(value = "流量测试", notes = "流量测试")
    @RequestMapping(value = "Test", method = RequestMethod.POST)
    public void PersonTest(@RequestBody int i) {
        memberFlowUploadService.initMemberFlowUpload(i);
    }


}

