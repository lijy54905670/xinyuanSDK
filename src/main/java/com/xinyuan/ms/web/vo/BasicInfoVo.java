package com.xinyuan.ms.web.vo;

import lombok.Data;

/**
 * 基本信息
 */
@Data
public class BasicInfoVo  {
    //设备名
    public String sDVName;
    //通道数
    public String ChanNum;
    //报警输入个数
    public String AlarmInPortNum;
    //是否循环录像
    public int dwRecycleRecord;
    //序列号
    public String sSerialNumber;
    //显示设备类型
    public int byDVRType;
    //硬盘数量
    public String byDiskNum;
    //报警输出个数
    public String byAlarmOutPortNum;
    //遥控器id
    public String dwDVRID;
    //软件版本
    public String sSoftWareVersion;
    //DSP软件版本
    public String sDSPSoftVersion;
    //硬件版本
    public String sHardwareVersion;
    //前面板版本
    public String sPanelVersion;
}
