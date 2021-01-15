package com.xinyuan.ms.web.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
@Data
@AllArgsConstructor
public class LoginRequest {

        @ApiModelProperty(value = "ip", name = "ip" , example = "192.168.2.200")
        private String ip;

        @ApiModelProperty(value = "port", name = "port" , example = "8000")
        private short port;

        @ApiModelProperty(value = "userName", name = "userName" , example = "admin")
        private String userName;

        @ApiModelProperty(value = "pwd", name = "pwd" , example = "xinyuan1,")
        private String pwd;

}
