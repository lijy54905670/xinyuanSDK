package com.xinyuan.ms.service;

import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.xinyuan.ms.entity.HCNetSDK;
import com.xinyuan.ms.web.request.LoginRequest;
import com.xinyuan.ms.web.vo.BasicInfoVo;
import org.springframework.stereotype.Service;

@Service
public class BasicService {
//    HCNetSDK hCNetSDK = (HCNetSDK) Native.loadLibrary("C:\\Users\\yaoli\\Desktop\\springBoot-template\\template\\HCNetSDK.dll",
//            HCNetSDK.class);

    HCNetSDK hCNetSDK = HCNetSDK.INSTANCE;


    NativeLong lUserID;

    public String login(LoginRequest loginRequest){
        hCNetSDK.NET_DVR_Init();
        //设备基本信息
        HCNetSDK.NET_DVR_DEVICEINFO_V30 m_str = new HCNetSDK.NET_DVR_DEVICEINFO_V30();
        System.out.println(m_str);
        //调用登录接口
        lUserID = hCNetSDK.NET_DVR_Login_V30(loginRequest.getIp(), (short) loginRequest.getPort(), loginRequest.getUserName(), loginRequest.getPwd(), m_str);
        System.out.println(1);
        long userID = lUserID.longValue();
        if (userID == -1)
        {
            System.out.println("登录失败");
            return "登录失败";
        }
        else
        {
            System.out.println("登录成功");
            return "登录成功";
        }
    }


    HCNetSDK.NET_DVR_DEVICECFG m_strDeviceCfg = new HCNetSDK.NET_DVR_DEVICECFG();
    IntByReference ibrBytesReturned = new IntByReference(0);


    //获取基本信息
    public BasicInfoVo getBasicCfg(){

         m_strDeviceCfg.write();
        Pointer lpPicConfig = m_strDeviceCfg.getPointer();

        boolean getDVRConfigSuc = hCNetSDK.NET_DVR_GetDVRConfig(lUserID, HCNetSDK.NET_DVR_GET_DEVICECFG,
                new NativeLong(0), lpPicConfig, m_strDeviceCfg.size(), ibrBytesReturned);

        m_strDeviceCfg.read();
        if (getDVRConfigSuc != true)
        {
            System.out.println(hCNetSDK.NET_DVR_GetLastError());
            //JOptionPane.showMessageDialog(this, "获取设备参数失败");
            return null;
        }


        System.out.println(m_strDeviceCfg + "  123");
        BasicInfoVo basicInfoVo = new BasicInfoVo();
        String[] sName = new String[2];
        //设备名称
        sName = new String(m_strDeviceCfg.sDVRName).split("\0", 2);
        //设备名称
        basicInfoVo.setSDVName(sName[0]);
        //通道数
        basicInfoVo.setChanNum(m_strDeviceCfg.byChanNum + "");
        //报警输入个数
        basicInfoVo.setAlarmInPortNum(m_strDeviceCfg.byAlarmInPortNum + "");
        //是否循环录像
        basicInfoVo.setDwRecycleRecord(m_strDeviceCfg.dwRecycleRecord);
        //序列号
        basicInfoVo.setSSerialNumber(new String(m_strDeviceCfg.sSerialNumber).trim());
        //硬盘数量
       basicInfoVo.setByDiskNum(m_strDeviceCfg.byDiskNum+"");
        //报警输出个数
        basicInfoVo.setByAlarmOutPortNum(m_strDeviceCfg.byAlarmOutPortNum+"");
        //遥控器id
        basicInfoVo.setDwDVRID(m_strDeviceCfg.dwDVRID+"");
        //软件版本
        String sSoftWareVersion;
        int dwSoftwareVersion = m_strDeviceCfg.dwSoftwareVersion;
        System.out.println(dwSoftwareVersion);
        if (((m_strDeviceCfg.dwSoftwareVersion >> 24) & 0xFF) > 0)
        {
            sSoftWareVersion =String.format("V%d.%d.%d build %02d%02d%02d", (m_strDeviceCfg.dwSoftwareVersion >> 24) & 0xFF, (m_strDeviceCfg.dwSoftwareVersion >> 16) & 0xFF, m_strDeviceCfg.dwSoftwareVersion & 0xFFFF, (m_strDeviceCfg.dwSoftwareBuildDate >> 16) & 0xFFFF, (m_strDeviceCfg.dwSoftwareBuildDate >> 8) & 0xFF, m_strDeviceCfg.dwSoftwareBuildDate & 0xFF);
            basicInfoVo.setSSoftWareVersion(sSoftWareVersion);
        } else
        {
            sSoftWareVersion =String.format("V%d.%d build %02d%02d%02d", (m_strDeviceCfg.dwSoftwareVersion >> 16) & 0xFFFF, m_strDeviceCfg.dwSoftwareVersion & 0xFFFF, (m_strDeviceCfg.dwSoftwareBuildDate >> 16) & 0xFFFF, (m_strDeviceCfg.dwSoftwareBuildDate >> 8) & 0xFF, m_strDeviceCfg.dwSoftwareBuildDate & 0xFF);
            basicInfoVo.setSSoftWareVersion(sSoftWareVersion);
        }

        //DSP软件版本
        String sDSPSoftVersion;
        int dwDSPSoftwareVersion = m_strDeviceCfg.dwDSPSoftwareVersion;
        System.out.println(dwDSPSoftwareVersion);
        sDSPSoftVersion = String.format("V%d.%d build %02d%02d%02d", (m_strDeviceCfg.dwDSPSoftwareVersion >> 16) & 0xFFFF, m_strDeviceCfg.dwDSPSoftwareVersion & 0xFFFF, (m_strDeviceCfg.dwDSPSoftwareBuildDate >> 16) & 0xFFFF - 2000, (m_strDeviceCfg.dwDSPSoftwareBuildDate >> 8) & 0xFF, m_strDeviceCfg.dwDSPSoftwareBuildDate & 0xFF);
        basicInfoVo.setSDSPSoftVersion(sDSPSoftVersion);

        //硬件版本
        String sHardwareVersion;
        int dwHardwareVersion = m_strDeviceCfg.dwHardwareVersion;
        System.out.println(dwHardwareVersion);
        sHardwareVersion = String.format("0x%x", m_strDeviceCfg.dwHardwareVersion);
        basicInfoVo.setSHardwareVersion(sHardwareVersion);

        //前面板版本
        String sPanelVersion;
        int dwPanelVersion = m_strDeviceCfg.dwPanelVersion;
        System.out.println(dwPanelVersion);
        sPanelVersion = String.format("V%d", m_strDeviceCfg.dwPanelVersion);
        basicInfoVo.setSPanelVersion(sPanelVersion);

        //显示设备类型
        if (m_strDeviceCfg.byDVRType <= 26)
        {
            basicInfoVo.setByDVRType(m_strDeviceCfg.byDVRType - 1);
        } else
        {
            if (m_strDeviceCfg.byDVRType >= 30 && m_strDeviceCfg.byDVRType <= 32)
            {
                basicInfoVo.setByDVRType(m_strDeviceCfg.byDVRType - 6);
            } else {
                if (m_strDeviceCfg.byDVRType == 40) {
                    basicInfoVo.setByDVRType(29);
                } else {
                    if (m_strDeviceCfg.byDVRType == 50) {
                        basicInfoVo.setByDVRType(30);
                    } else {
                        if (m_strDeviceCfg.byDVRType == 63) {
                            basicInfoVo.setByDVRType(31);
                        } else {
                            if (m_strDeviceCfg.byDVRType == 60) {
                                basicInfoVo.setByDVRType(32);
                            } else {
                                if (m_strDeviceCfg.byDVRType >= 71 && m_strDeviceCfg.byDVRType <= 73) {
                                    basicInfoVo.setByDVRType(m_strDeviceCfg.byDVRType - 38);
                                } else {
                                    if (m_strDeviceCfg.byDVRType >= 81 && m_strDeviceCfg.byDVRType <= 88) {
                                        basicInfoVo.setByDVRType(m_strDeviceCfg.byDVRType - 45);
                                    } else {
                                        if (m_strDeviceCfg.byDVRType >= 90 && m_strDeviceCfg.byDVRType <= 92) {
                                            basicInfoVo.setByDVRType(m_strDeviceCfg.byDVRType - 46);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }}


        return basicInfoVo;

    }

    //设置基本信息
    public void setBasicCfg(BasicInfoVo basicInfoVo){


        //设备名
        m_strDeviceCfg.setSDVRName(basicInfoVo.getSDVName().getBytes());
        //
        m_strDeviceCfg.setByChanNum(Byte.parseByte(basicInfoVo.getChanNum()));
        //
        m_strDeviceCfg.setByAlarmInPortNum(Byte.parseByte(basicInfoVo.getAlarmInPortNum()));
        //
        m_strDeviceCfg.setDwRecycleRecord(basicInfoVo.getDwRecycleRecord());
        //
        m_strDeviceCfg.setSSerialNumber(basicInfoVo.getSSerialNumber().getBytes());
        //类型
        m_strDeviceCfg.setByDVRType(Byte.parseByte(basicInfoVo.getByDVRType()+""));
        //
        m_strDeviceCfg.setByDiskNum(Byte.parseByte(basicInfoVo.getByDiskNum()));
        //
        m_strDeviceCfg.setByAlarmOutPortNum(Byte.parseByte(basicInfoVo.byAlarmOutPortNum));
        //
        m_strDeviceCfg.setDwDVRID(Integer.parseInt(basicInfoVo.getDwDVRID()));
        //
//        m_strDeviceCfg.setDwSoftwareVersion(0);
//        //
//        m_strDeviceCfg.setDwDSPSoftwareVersion(0);
//        //
//        m_strDeviceCfg.setDwHardwareVersion(0);
//        //
//        m_strDeviceCfg.setDwPanelVersion(0);

        m_strDeviceCfg.write();

        Pointer lpPicConfig = m_strDeviceCfg.getPointer();

        boolean b = hCNetSDK.NET_DVR_SetDVRConfig(lUserID, HCNetSDK.NET_DVR_SET_DEVICECFG, new NativeLong(0), lpPicConfig, m_strDeviceCfg.size());

        m_strDeviceCfg.read();

//        boolean getDVRConfigSuc = hCNetSDK.NET_DVR_GetDVRConfig(lUserID, HCNetSDK.NET_DVR_GET_DEVICECFG,
//                new NativeLong(0), lpPicConfig, m_strDeviceCfg.size(), ibrBytesReturned);
        m_strDeviceCfg.read();
    }


    public void init(){
        hCNetSDK.NET_DVR_Init();
    }

}
