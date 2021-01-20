package com.xinyuan.ms.service;

import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.xinyuan.ms.entity.HCNetSDK;
import com.xinyuan.ms.web.request.LoginRequest;
import com.xinyuan.ms.web.vo.AlarmOutVo;
import com.xinyuan.ms.web.vo.BasicInfoVo;
import com.xinyuan.ms.web.vo.ChanelVo;
import org.springframework.stereotype.Service;

import javax.swing.*;

@Service
public class BasicService {
//    HCNetSDK hCNetSDK = (HCNetSDK) Native.loadLibrary("C:\\Users\\yaoli\\Desktop\\springBoot-template\\template\\HCNetSDK.dll",
//            HCNetSDK.class);

    HCNetSDK hCNetSDK = HCNetSDK.INSTANCE;


    NativeLong lUserID;

    public String login(LoginRequest loginRequest) {
        hCNetSDK.NET_DVR_Init();
        //设备基本信息
        HCNetSDK.NET_DVR_DEVICEINFO_V30 m_str = new HCNetSDK.NET_DVR_DEVICEINFO_V30();
        System.out.println(m_str);
        //调用登录接口
        lUserID = hCNetSDK.NET_DVR_Login_V30(loginRequest.getIp(), (short) loginRequest.getPort(), loginRequest.getUserName(), loginRequest.getPwd(), m_str);
        System.out.println(1);
        long userID = lUserID.longValue();
        if (userID == -1) {
            System.out.println("登录失败");
            return "登录失败";
        } else {
            System.out.println("登录成功");
            return "登录成功";
        }
    }


    HCNetSDK.NET_DVR_DEVICECFG m_strDeviceCfg = new HCNetSDK.NET_DVR_DEVICECFG();
    IntByReference ibrBytesReturned = new IntByReference(0);


    //获取基本信息
    public BasicInfoVo getBasicCfg() {

        m_strDeviceCfg.write();
        Pointer lpPicConfig = m_strDeviceCfg.getPointer();

        boolean getDVRConfigSuc = hCNetSDK.NET_DVR_GetDVRConfig(lUserID, HCNetSDK.NET_DVR_GET_DEVICECFG,
                new NativeLong(0), lpPicConfig, m_strDeviceCfg.size(), ibrBytesReturned);

        m_strDeviceCfg.read();
        if (getDVRConfigSuc != true) {
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
        basicInfoVo.setByDiskNum(m_strDeviceCfg.byDiskNum + "");
        //报警输出个数
        basicInfoVo.setByAlarmOutPortNum(m_strDeviceCfg.byAlarmOutPortNum + "");
        //遥控器id
        basicInfoVo.setDwDVRID(m_strDeviceCfg.dwDVRID + "");
        //软件版本
        String sSoftWareVersion;
        int dwSoftwareVersion = m_strDeviceCfg.dwSoftwareVersion;
        System.out.println(dwSoftwareVersion);
        if (((m_strDeviceCfg.dwSoftwareVersion >> 24) & 0xFF) > 0) {
            sSoftWareVersion = String.format("V%d.%d.%d build %02d%02d%02d", (m_strDeviceCfg.dwSoftwareVersion >> 24) & 0xFF, (m_strDeviceCfg.dwSoftwareVersion >> 16) & 0xFF, m_strDeviceCfg.dwSoftwareVersion & 0xFFFF, (m_strDeviceCfg.dwSoftwareBuildDate >> 16) & 0xFFFF, (m_strDeviceCfg.dwSoftwareBuildDate >> 8) & 0xFF, m_strDeviceCfg.dwSoftwareBuildDate & 0xFF);
            basicInfoVo.setSSoftWareVersion(sSoftWareVersion);
        } else {
            sSoftWareVersion = String.format("V%d.%d build %02d%02d%02d", (m_strDeviceCfg.dwSoftwareVersion >> 16) & 0xFFFF, m_strDeviceCfg.dwSoftwareVersion & 0xFFFF, (m_strDeviceCfg.dwSoftwareBuildDate >> 16) & 0xFFFF, (m_strDeviceCfg.dwSoftwareBuildDate >> 8) & 0xFF, m_strDeviceCfg.dwSoftwareBuildDate & 0xFF);
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
        if (m_strDeviceCfg.byDVRType <= 26) {
            basicInfoVo.setByDVRType(m_strDeviceCfg.byDVRType - 1);
        } else {
            if (m_strDeviceCfg.byDVRType >= 30 && m_strDeviceCfg.byDVRType <= 32) {
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
            }
        }


        return basicInfoVo;

    }

    //设置基本信息
    public void setBasicCfg(BasicInfoVo basicInfoVo) {


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
        m_strDeviceCfg.setByDVRType(Byte.parseByte(basicInfoVo.getByDVRType() + ""));
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


    public void init() {
        hCNetSDK.NET_DVR_Init();
    }

    //static HCNetSDK hCNetSDK = HCNetSDK.INSTANCE;
    private HCNetSDK.NET_DVR_PICCFG_V30 m_struPicCfg;//图片参数
    private HCNetSDK.NET_DVR_COMPRESSIONCFG_V30 m_struCompressionCfg;//压缩参数
    private HCNetSDK.NET_DVR_RECORD_V30 m_struRemoteRecCfg;//录像参数
    private HCNetSDK.NET_DVR_DEVICEINFO_V30 m_strDeviceInfo;//设备信息
    private HCNetSDK.NET_DVR_SHOWSTRING_V30 m_strShowString;//叠加字符参数
    //private NativeLong lUserID;//用户ID
    public javax.swing.JComboBox jComboBoxChannelNumber;
// jComboBoxChannelNumber.addActionListener(new java.awt.event.ActionListener()
//
//    {
//        public void actionPerformed (java.awt.event.ActionEvent evt){
//        jComboBoxChannelNumberActionPerformed(evt);
//    }
//    });

    public ChanelVo getChanel() {
        m_strDeviceInfo = new HCNetSDK.NET_DVR_DEVICEINFO_V30();
        jComboBoxChannelNumber = new javax.swing.JComboBox();
        jComboBoxChannelNumber.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxChannelNumberActionPerformed(evt);
            }
        });
        int iChannelNumber = -1;
        int iIndex = jComboBoxChannelNumber.getSelectedIndex();
        if ((iIndex < m_strDeviceInfo.byChanNum) && (iIndex >= -1)) {
            iChannelNumber = iIndex + m_strDeviceInfo.byStartChan;
        } else {
            iChannelNumber = 32 + (iIndex - m_strDeviceInfo.byChanNum) + m_strDeviceInfo.byStartChan;
        }
        IntByReference ibrBytesReturned = new IntByReference(0);//获取图片参数
        m_struPicCfg = new HCNetSDK.NET_DVR_PICCFG_V30();
        m_struPicCfg.write();
        Pointer lpPicConfig = m_struPicCfg.getPointer();
        boolean getDVRConfigSuc = hCNetSDK.NET_DVR_GetDVRConfig(lUserID, HCNetSDK.NET_DVR_GET_PICCFG_V30,
                new NativeLong(1), lpPicConfig, m_struPicCfg.size(), ibrBytesReturned);
        m_struPicCfg.read();
        if (getDVRConfigSuc != true) {
            JOptionPane.showMessageDialog(null, "获取图片参数失败");
            return null;
        }

        ibrBytesReturned = new IntByReference(0);//获取压缩参数
        getDVRConfigSuc = false;
        m_struCompressionCfg = new HCNetSDK.NET_DVR_COMPRESSIONCFG_V30();
        m_struCompressionCfg.write();
        lpPicConfig = m_struCompressionCfg.getPointer();
        getDVRConfigSuc = hCNetSDK.NET_DVR_GetDVRConfig(lUserID, HCNetSDK.NET_DVR_GET_COMPRESSCFG_V30,
                new NativeLong(1), lpPicConfig, m_struCompressionCfg.size(), ibrBytesReturned);
        m_struCompressionCfg.read();
        if (getDVRConfigSuc != true) {
            JOptionPane.showMessageDialog(null, "获取压缩参数失败");
            return null;
        }

        ibrBytesReturned = new IntByReference(0);//获取录像参数
        getDVRConfigSuc = false;
        m_struRemoteRecCfg = new HCNetSDK.NET_DVR_RECORD_V30();
        m_struRemoteRecCfg.write();
        lpPicConfig = m_struRemoteRecCfg.getPointer();
        getDVRConfigSuc = hCNetSDK.NET_DVR_GetDVRConfig(lUserID, HCNetSDK.NET_DVR_GET_RECORDCFG_V30,
                new NativeLong(1), lpPicConfig, m_struRemoteRecCfg.size(), ibrBytesReturned);
        m_struRemoteRecCfg.read();
        if (getDVRConfigSuc != true) {
            System.out.println(hCNetSDK.NET_DVR_GetLastError());
            JOptionPane.showMessageDialog(null, "获取录像参数失败");
            return null;
        }

        //显示参数
        String[] sName = new String[2];
        sName = new String(m_struPicCfg.sChanName).split("\0", 2);
        ChanelVo chanelVo = new ChanelVo();
        chanelVo.setJTextFieldChannelName(sName[0]);
        //压缩参数
        chanelVo.setJComboBoxCompressType(0);

        chanelVo.setJComboBoxImageQulity(m_struCompressionCfg.struNormHighRecordPara.byPicQuality);
        chanelVo.setJComboBoxStreamType(m_struCompressionCfg.struNormHighRecordPara.byStreamType);
        chanelVo.setJComboBoxResolution(m_struCompressionCfg.struNormHighRecordPara.byResolution);
        chanelVo.setJComboBoxBitRateType(m_struCompressionCfg.struNormHighRecordPara.byBitrateType);
        chanelVo.setJComboBoxFrameRate(m_struCompressionCfg.struNormHighRecordPara.dwVideoFrameRate);
        if (((m_struCompressionCfg.struNormHighRecordPara.dwVideoBitrate >> 31) & 0x01) == 1)//位率上限
        {
            chanelVo.setJComboBoxMaxBitRate(22);
            chanelVo.setJTextFieldBitRate((m_struCompressionCfg.struNormHighRecordPara.dwVideoBitrate & 0x7fffffff) / 1024 + "");
        } else {
            chanelVo.setJComboBoxMaxBitRate(m_struCompressionCfg.struNormHighRecordPara.dwVideoBitrate - 2);
            chanelVo.setJTextFieldBitRate("");
        }
        chanelVo.setJTextFieldIInterval(m_struCompressionCfg.struNormHighRecordPara.wIntervalFrameI + "");
        chanelVo.setJComboBoxBpInterval(m_struCompressionCfg.struNormHighRecordPara.byIntervalBPFrame);

        //录像参数
        chanelVo.setJCheckBoxRecord((m_struRemoteRecCfg.dwRecord > 0) ? true : false);
        chanelVo.setJComboBoxPreRecordTime(m_struRemoteRecCfg.dwPreRecordTime);
        chanelVo.setJComboBoxRecordDelay(m_struRemoteRecCfg.dwRecordTime);
        chanelVo.setJTextFieldSaveDays(m_struRemoteRecCfg.dwRecorderDuration + "");
        chanelVo.setJCheckBoxRebundancy((m_struRemoteRecCfg.byRedundancyRec > 0) ? true : false);
        chanelVo.setJCheckBoxAudioRec((m_struRemoteRecCfg.byAudioRec > 0) ? true : false);

        //图像参数
        chanelVo.setJCheckBoxHideArea((m_struPicCfg.dwEnableHide > 0) ? true : false);
        chanelVo.setJCheckBoxMotion((m_struPicCfg.struMotion.byEnableHandleMotion > 0) ? true : false);
        chanelVo.setJCheckBoxSignalLost((m_struPicCfg.struVILost.byEnableHandleVILost > 0) ? true : false);
        chanelVo.setJCheckBoxHideAlarm((m_struPicCfg.struHideAlarm.dwEnableHideAlarm > 0) ? true : false);
        chanelVo.setJCheckBoxOSD((m_struPicCfg.dwShowOsd > 0) ? true : false);
        chanelVo.setJTextFieldOSDX(m_struPicCfg.wOSDTopLeftX + "");
        chanelVo.setJTextFieldOSDY(m_struPicCfg.wOSDTopLeftY + "");
        chanelVo.setJCheckBoxOSDDate((m_struPicCfg.byDispWeek > 0) ? true : false);
        chanelVo.setJComboBoxTimeFormat(m_struPicCfg.byHourOSDType);
        chanelVo.setJComboBoxOSDFormat(m_struPicCfg.byOSDAttrib - 2);
        chanelVo.setJComboBoxDateFormat(m_struPicCfg.byOSDType);
        chanelVo.setJCheckBoxChannelName((m_struPicCfg.dwShowChanName > 0) ? true : false);
        chanelVo.setJTextFieldChannekNameX(m_struPicCfg.wShowNameTopLeftX + "");
        chanelVo.setJTextFieldChannelNameY(m_struPicCfg.wShowNameTopLeftY + "");
        return chanelVo;

//        //设置子结构窗口弹出按钮的可编辑性
//        jButtonSetHideArea.setEnabled(jCheckBoxHideArea.isSelected());
//        jButtonSetMotion.setEnabled(jCheckBoxMotion.isSelected());
//        jButtonSetSignalLost.setEnabled(jCheckBoxSignalLost.isSelected());
//        jButtonSetHideAlarm.setEnabled(jCheckBoxHideAlarm.isSelected());
    }

    private void jComboBoxChannelNumberActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxChannelNumberActionPerformed
        getChanel();
    }
    private HCNetSDK.NET_DVR_ALARMOUTCFG_V30 m_struAlarmOutCfg;
    private javax.swing.JComboBox jComboBoxAlarmOutChannel;
    public AlarmOutVo showAlarmOutCfg() {
        jComboBoxAlarmOutChannel = new javax.swing.JComboBox();
        IntByReference ibrBytesReturned = new IntByReference(0);
        m_struAlarmOutCfg = new HCNetSDK.NET_DVR_ALARMOUTCFG_V30();
        m_struAlarmOutCfg.write();
        Pointer lpConfig = m_struAlarmOutCfg.getPointer();
        boolean getDVRConfigSuc = hCNetSDK.NET_DVR_GetDVRConfig(lUserID, HCNetSDK.NET_DVR_GET_ALARMOUTCFG_V30,
                new NativeLong(jComboBoxAlarmOutChannel.getSelectedIndex()), lpConfig, m_struAlarmOutCfg.size(), ibrBytesReturned);
        m_struAlarmOutCfg.read();
        if (getDVRConfigSuc != true) {
            JOptionPane.showMessageDialog(null, "获取报警输出参数失败");
            return null;
        }
        String[] sName = new String[2];//报警输入名称
        AlarmOutVo alarmOutVo = new AlarmOutVo();
        sName = new String(m_struAlarmOutCfg.sAlarmOutName).split("\0", 2);
        alarmOutVo.setJTextFieldAlarmOutName(sName[0]);

        alarmOutVo.setJTextFieldAlarmOutDelay(m_struAlarmOutCfg.dwAlarmOutDelay + "");
        return alarmOutVo;
    }
}
