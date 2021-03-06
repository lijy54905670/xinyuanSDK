package com.xinyuan.ms.service;

import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.xinyuan.ms.service.HCNetSDK.COMM_ALARM_RULE;

@Service
public class test2Service {
    static HCNetSDK hCNetSDK = HCNetSDK.INSTANCE;
    static String m_sUsername = "admin";//设备用户名
    static String m_sPassword = "12345ABCDE";//设备密码
    static short m_sPort = 8000;//端口号，这是默认的
    public NativeLong lUserID;//用户句柄
    public int lAlarmHandle;//报警布防句柄
    public int lListenHandle;//报警监听句柄
    public NativeLong RemoteConfig;
    public static int code = 5;
    FMSGCallBack fMSFCallBack;


    public void initMemberFlowUpload(String m_sDeviceIP) throws InterruptedException {
        // 初始化
        Boolean flag = hCNetSDK.NET_DVR_Init();
        if (flag) {
            System.out.println("初始化成功");
        } else {
            System.out.println("初始化失败");
        }
        //设置连接时间与重连时间
        hCNetSDK.NET_DVR_SetConnectTime(20000, 1);
        hCNetSDK.NET_DVR_SetReconnect(100000, true);
        //设备信息, 输出参数
        HCNetSDK.NET_DVR_DEVICEINFO_V40 m_strDeviceInfo = new HCNetSDK.NET_DVR_DEVICEINFO_V40();
        HCNetSDK.NET_DVR_USER_LOGIN_INFO m_strLoginInfo = new HCNetSDK.NET_DVR_USER_LOGIN_INFO();
        // 注册设备-登录参数，包括设备地址、登录用户、密码等
        m_strLoginInfo.sDeviceAddress = new byte[hCNetSDK.NET_DVR_DEV_ADDRESS_MAX_LEN];
        System.arraycopy(m_sDeviceIP.getBytes(), 0, m_strLoginInfo.sDeviceAddress, 0, m_sDeviceIP.length());
        m_strLoginInfo.sUserName = new byte[hCNetSDK.NET_DVR_LOGIN_USERNAME_MAX_LEN];
        System.arraycopy(m_sUsername.getBytes(), 0, m_strLoginInfo.sUserName, 0, m_sUsername.length());
        m_strLoginInfo.sPassword = new byte[hCNetSDK.NET_DVR_LOGIN_PASSWD_MAX_LEN];
        System.arraycopy(m_sPassword.getBytes(), 0, m_strLoginInfo.sPassword, 0, m_sPassword.length());
        m_strLoginInfo.wPort = m_sPort;
        m_strLoginInfo.bUseAsynLogin = false; //是否异步登录：0- 否，1- 是
        m_strLoginInfo.write();

        //设备信息, 输出参数
        int lUserID = hCNetSDK.NET_DVR_Login_V40(m_strLoginInfo, m_strDeviceInfo);
        if (lUserID < 0) {
            System.out.println("hCNetSDK.NET_DVR_Login_V30()  " + "\n" + hCNetSDK.NET_DVR_GetErrorMsg(null));
            hCNetSDK.NET_DVR_Cleanup();
            return;
        }

        if (fMSFCallBack == null)
        {
            fMSFCallBack = new FMSGCallBack();
        }


        //设置报警回调函数-------------------------------------------------------------
        if (!hCNetSDK.NET_DVR_SetDVRMessageCallBack_V30(fMSFCallBack, null)) {
            System.out.println("设置回调函数失败" + hCNetSDK.NET_DVR_GetLastError());
            return;
        } else {
            System.out.println("设置回调函数成功");
        }
        //启用布防
        HCNetSDK.NET_DVR_SETUPALARM_PARAM lpSetupParam = new HCNetSDK.NET_DVR_SETUPALARM_PARAM();
        lpSetupParam.dwSize = 0;
        lpSetupParam.byLevel = 1;//布防优先级：0- 一等级（高），1- 二等级（中）
        lpSetupParam.byAlarmInfoType = 1;//上传报警信息类型: 0- 老报警信息(NET_DVR_PLATE_RESULT), 1- 新报警信息(NET_ITS_PLATE_RESULT)
        //布防-----------------------------------------------------------------------
        lAlarmHandle = hCNetSDK.NET_DVR_SetupAlarmChan_V41(lUserID, lpSetupParam);//建立报警上传通道，获取报警等信息。
        if (lAlarmHandle < 0) {
            System.out.println("NET_DVR_SetupAlarmChan_V41 error, %d\n" + hCNetSDK.NET_DVR_GetLastError());
            hCNetSDK.NET_DVR_Logout(lUserID);
            hCNetSDK.NET_DVR_Cleanup();
            return;
        }
        System.out.println("布防成功,开始监测抓拍");

        //启动监听--------------------------------------------------------------------
        int iListenPort = 8000;
        String m_sListenIP = "127.0.0.1";

        lListenHandle = hCNetSDK.NET_DVR_StartListen_V30(m_sListenIP, (short) iListenPort, fMSFCallBack, null);
        if (lListenHandle < 0) {
            System.out.println("启动监听失败");
        } else {
            System.out.println("启动监听成功");
        }
    }

  /*  public boolean MsesGCallBack(int lCommand, HCNetSDK.NET_DVR_ALARMER pAlarmer, Pointer pAlarmInfo, int dwBufLen, Pointer pUser) {
        System.out.println("监听开始抓拍");
        try {
            String sAlarmType = new String();
            String[] newRow = new String[3];
            Date today = new Date();
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            String[] sIP = new String[2];
            switch (lCommand) {

                case COMM_ALARM_RULE:    //行为分析信息上传
                    System.out.println("抓拍成功！");
                    HCNetSDK.NET_VCA_RULE_ALARM strVcaAlarm = new HCNetSDK.NET_VCA_RULE_ALARM();
                    strVcaAlarm.write();
                    Pointer pVcaInfo = strVcaAlarm.getPointer();
                    pVcaInfo.write(0, pAlarmInfo.getByteArray(0, strVcaAlarm.size()), 0, strVcaAlarm.size());
                    strVcaAlarm.read();
                    String type = "OTHER";

                    switch (strVcaAlarm.struRuleInfo.wEventTypeEx) {
                        case 41:
                            System.out.println("人员滞留");
                            type = "RETENTION";
                            break;
                        case 15:
                            System.out.println("人员离岗");
                            type = "ABSENCE";
                            break;
                        case 1:
                            System.out.println("人员越界");
                            type = "LINEDETECT";
                            break;
                        default:
                            sAlarmType = sAlarmType + new String("：其他行为分析报警，事件类型：")
                                    + strVcaAlarm.struRuleInfo.wEventTypeEx +
                                    "_wPort:" + strVcaAlarm.struDevInfo.wPort +
                                    "_byChannel:" + strVcaAlarm.struDevInfo.byChannel +
                                    "_byIvmsChannel:" + strVcaAlarm.struDevInfo.byIvmsChannel +
                                    "_Dev IP：" + new String(strVcaAlarm.struDevInfo.struDevIP.sIpV4);
                            break;
                    }

                    System.out.println(sAlarmType + "     123");
                    newRow[0] = dateFormat.format(today);
                    //报警类型
                    newRow[1] = sAlarmType;
                    //报警设备IP地址
                    sIP = new String(pAlarmer.sDeviceIP).split("\0", 2);
                    newRow[2] = sIP[0];

                    if (strVcaAlarm.dwPicDataLen > 0) {
                        SimpleDateFormat sf = new SimpleDateFormat("yyyyMMddHHmmss");
                        String newName = sf.format(today);
                        FileOutputStream fout;
                        try {
                            fout = new FileOutputStream("C:\\Users\\yaoli\\Desktop\\pic\\" + "ch" + strVcaAlarm.struDevInfo.byIvmsChannel +"_"+ newName +"_"+type + ".jpg");
                            //将字节写入文件
                            long offset = 0;
                            ByteBuffer buffers = strVcaAlarm.pImage.getByteBuffer(offset, strVcaAlarm.dwPicDataLen);
                            byte[] bytes = new byte[strVcaAlarm.dwPicDataLen];
                            buffers.rewind();
                            buffers.get(bytes);
                            fout.write(bytes);
                            fout.close();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    */

    //报警回调函数
    public class FMSGCallBack implements HCNetSDK.FMSGCallBack {
        @Override
        public void invoke(int lCommand, HCNetSDK.NET_DVR_ALARMER pAlarmer, Pointer pAlarmInfo, int dwBufLen, Pointer pUser) {
            System.out.println("监听开始抓拍");
            try {
                String sAlarmType = new String();
                String[] newRow = new String[3];
                Date today = new Date();
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                String[] sIP = new String[2];
                switch (lCommand) {

                    case COMM_ALARM_RULE:    //行为分析信息上传
                        System.out.println("抓拍成功！");
                        HCNetSDK.NET_VCA_RULE_ALARM strVcaAlarm = new HCNetSDK.NET_VCA_RULE_ALARM();
                        strVcaAlarm.write();
                        Pointer pVcaInfo = strVcaAlarm.getPointer();
                        pVcaInfo.write(0, pAlarmInfo.getByteArray(0, strVcaAlarm.size()), 0, strVcaAlarm.size());
                        strVcaAlarm.read();
                        String type = "OTHER";

                        switch (strVcaAlarm.struRuleInfo.wEventTypeEx) {
                            case 41:
                                System.out.println("人员滞留");
                                type = "RETENTION";
                                break;
                            case 15:
                                System.out.println("人员离岗");
                                type = "ABSENCE";
                                break;
                            case 1:
                                System.out.println("人员越界");
                                type = "LINEDETECT";
                                break;
                            default:
                                sAlarmType = sAlarmType + new String("：其他行为分析报警，事件类型：")
                                        + strVcaAlarm.struRuleInfo.wEventTypeEx +
                                        "_wPort:" + strVcaAlarm.struDevInfo.wPort +
                                        "_byChannel:" + strVcaAlarm.struDevInfo.byChannel +
                                        "_byIvmsChannel:" + strVcaAlarm.struDevInfo.byIvmsChannel +
                                        "_Dev IP：" + new String(strVcaAlarm.struDevInfo.struDevIP.sIpV4);
                                break;
                        }

                        System.out.println(sAlarmType + "     123");
                        newRow[0] = dateFormat.format(today);
                        //报警类型
                        newRow[1] = sAlarmType;
                        //报警设备IP地址
                        sIP = new String(pAlarmer.sDeviceIP).split("\0", 2);
                        newRow[2] = sIP[0];

                        if (strVcaAlarm.dwPicDataLen > 0) {
                            SimpleDateFormat sf = new SimpleDateFormat("yyyyMMddHHmmss");
                            String newName = sf.format(today);
                            FileOutputStream fout;
                            try {
                                fout = new FileOutputStream("C:\\Users\\yaoli\\Desktop\\pic\\" + "ch" + strVcaAlarm.struDevInfo.byIvmsChannel +"_"+ newName +"_"+type + ".jpg");
                                //将字节写入文件
                                long offset = 0;
                                ByteBuffer buffers = strVcaAlarm.pImage.getByteBuffer(offset, strVcaAlarm.dwPicDataLen);
                                byte[] bytes = new byte[strVcaAlarm.dwPicDataLen];
                                buffers.rewind();
                                buffers.get(bytes);
                                fout.write(bytes);
                                fout.close();
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //报警撤防
    public void CloseAlarmChan() {
        if (lAlarmHandle != -1)
        {
            hCNetSDK.NET_DVR_CloseAlarmChan_V30(lAlarmHandle);
            System.out.println("撤防成功");
        }
        //停止监听
        if (lListenHandle != -1)
        {
            hCNetSDK.NET_DVR_StopListen_V30(lListenHandle);
            System.out.println("停止监听成功");
        }
    }


}
