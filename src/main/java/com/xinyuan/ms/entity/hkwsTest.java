package com.xinyuan.ms.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.*;

import java.util.Date;

import static javax.persistence.GenerationType.IDENTITY;

@Data
@Entity
@Table(name = "sys_hkws")
public class hkwsTest {
    @Id
    @ApiModelProperty(value = "主键", name = "id", example = "0")
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @ApiModelProperty(value = "进入人数", name = "inPerson", example = "1")
    @Column(name = "inPerson")
    private int inPerson;

    @ApiModelProperty(value = "离开人数", name = "outPerson", example = "1")
    @Column(name = "outPerson")
    private int outPerson;

    @ApiModelProperty(value = "记录时间", name = "time", example = "1")
    @Column(name = "time")
    private String time;
}
