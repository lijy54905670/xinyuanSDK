package com.xinyuan.ms;

import com.xinyuan.ms.ClientDemo.HCNetSDK;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import springfox.documentation.swagger2.annotations.EnableSwagger2;
/**
 * @author liang
 */
@EnableCaching
@EnableSwagger2
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Autowired
    public  HCNetSDK.NET_DVR_DEVICEINFO_V30 get(){
        return new HCNetSDK.NET_DVR_DEVICEINFO_V30();
    }
}
