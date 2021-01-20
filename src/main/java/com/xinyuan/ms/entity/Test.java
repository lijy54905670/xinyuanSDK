package com.xinyuan.ms.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.*;

import static javax.persistence.GenerationType.IDENTITY;
@Data
@Entity
@Table(name = "sys_test1")
public class Test {
    @Id
    @ApiModelProperty(value = "主键", name = "id", example = "0")
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @ApiModelProperty(value = "ip地址", name = "ipAddress", example = "1")
    @Column(name = "ipAddress")
    private String ipAddress;

    @ApiModelProperty(value = "通道号", name = "channel", example = "1")
    @Column(name = "channel")
    private byte channel;

    @ApiModelProperty(value = "记录时间", name = "time", example = "1")
    @Column(name = "time")
    private String time;

    @ApiModelProperty(value = "事件类型", name = "type", example = "1")
    @Column(name = "type")
    private String type;
}
