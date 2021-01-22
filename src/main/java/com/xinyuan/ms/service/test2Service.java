package com.xinyuan.ms.service;

import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Timer;

import static com.xinyuan.ms.service.HCNetSDK.*;

@Service
public class test2Service {

    @Autowired
    BasicService basicService;

    static HCNetSDK hCNetSDK = HCNetSDK.INSTANCE;
    static String m_sUsername = "admin";//设备用户名
    static String m_sPassword = "12345ABCDE";//设备密码
    static short m_sPort = 8000;//端口号，这是默认的
    public int lUserID;//用户句柄
    public int lAlarmHandle;//报警布防句柄
    public int lListenHandle;//报警监听句柄
    public NativeLong RemoteConfig;
    public static int code = 5;
    FMSGCallBack fMSFCallBack;//回调函数

    HCNetSDK.NET_DVR_IPPARACFG m_strIpparaCfg;//IP参数


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
        lUserID = hCNetSDK.NET_DVR_Login_V40(m_strLoginInfo, m_strDeviceInfo);
        if (lUserID < 0) {
            System.out.println("hCNetSDK.NET_DVR_Login_V30()  " + "\n" + hCNetSDK.NET_DVR_GetErrorMsg(null));
            hCNetSDK.NET_DVR_Cleanup();
            return;
        }

        if (fMSFCallBack == null) {
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
                                fout = new FileOutputStream("C:\\Users\\yaoli\\Desktop\\pic\\" + "ch" + strVcaAlarm.struDevInfo.byIvmsChannel + "_" + newName + "_" + type + ".jpg");
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
        if (lAlarmHandle != -1) {
            hCNetSDK.NET_DVR_CloseAlarmChan_V30(lAlarmHandle);
            System.out.println("撤防成功");
        }
        //停止监听
        if (lListenHandle != -1) {
            hCNetSDK.NET_DVR_StopListen_V30(lListenHandle);
            System.out.println("停止监听成功");
        }
    }


    int m_iTreeNodeNum = 0;//通道树节点数目


    /**
     * 建立设备通道树
     *
     * @return
     */
    public List<Map<String, Object>> CreateDeviceTree() {

        IntByReference ibrBytesReturned = new IntByReference(0);//获取IP接入配置参数
        m_strIpparaCfg = new HCNetSDK.NET_DVR_IPPARACFG();

        m_strIpparaCfg.write();
        Pointer lpIpParaConfig = m_strIpparaCfg.getPointer();
        //获取IP接入配置信息
        boolean bRet = false;
        bRet = hCNetSDK.NET_DVR_GetDVRConfig(lUserID, NET_DVR_GET_IPPARACFG, 0, lpIpParaConfig, m_strIpparaCfg.size(), ibrBytesReturned);
        m_strIpparaCfg.read();


        List<Map<String, Object>> channal = new ArrayList<>();
        if (!bRet) {
            //设备不支持,则表示没有IP通道
            for (int iChannum = 0; iChannum < basicService.m_strDeviceInfo.struDeviceV30.byChanNum; iChannum++) {
                Map<String, Object> map = new HashMap<>();
                map.put("name", "Camera" + (iChannum + basicService.m_strDeviceInfo.struDeviceV30.byStartChan));
                map.put("iChanum", (iChannum + basicService.m_strDeviceInfo.struDeviceV30.byStartChan));
                channal.add(map);
                System.out.println("Camera" + (iChannum + basicService.m_strDeviceInfo.struDeviceV30.byStartChan));
            }
        } else {
            //设备支持IP通道
            for (int iChannum = 0; iChannum < basicService.m_strDeviceInfo.struDeviceV30.byChanNum; iChannum++) {
                if (m_strIpparaCfg.byAnalogChanEnable[iChannum] == 1) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("name", "Camera" + (iChannum + basicService.m_strDeviceInfo.struDeviceV30.byStartChan));
                    map.put("iChanum", (iChannum + basicService.m_strDeviceInfo.struDeviceV30.byStartChan));
                    channal.add(map);
                    System.out.println("Camera" + (iChannum + basicService.m_strDeviceInfo.struDeviceV30.byStartChan));
                    m_iTreeNodeNum++;
                }
            }
            for (int iChannum = 0; iChannum < HCNetSDK.MAX_IP_CHANNEL; iChannum++)
                if (m_strIpparaCfg.struIPChanInfo[iChannum].byEnable == 1) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("name", "Camera" + (iChannum + basicService.m_strDeviceInfo.struDeviceV30.byStartChan));
                    map.put("iChanum", (iChannum + basicService.m_strDeviceInfo.struDeviceV30.byStartChan));
                    channal.add(map);
                    System.out.println("IPCamera" + (iChannum + basicService.m_strDeviceInfo.struDeviceV30.byStartChan));
                }
        }
        return channal;
    }


    /**
     * 根据搜索信息搜索录像文件
     *
     * @return
     */
    public List<Vector<String>> search() {

        HCNetSDK.NET_DVR_FILECOND m_strFilecond = new HCNetSDK.NET_DVR_FILECOND();
        //开始时间
        m_strFilecond.struStartTime = new HCNetSDK.NET_DVR_TIME();
        //结束时间
        m_strFilecond.struStopTime = new HCNetSDK.NET_DVR_TIME();

        m_strFilecond.struStartTime.dwYear = 2021;//开始时间
        m_strFilecond.struStartTime.dwMonth = 1;
        m_strFilecond.struStartTime.dwDay = 1;
        m_strFilecond.struStartTime.dwHour = 0;
        m_strFilecond.struStartTime.dwMinute = 0;
        m_strFilecond.struStartTime.dwSecond = 0;
        m_strFilecond.struStopTime.dwYear = 2021;//结束时间
        m_strFilecond.struStopTime.dwMonth = 1;
        m_strFilecond.struStopTime.dwDay = 21;
        m_strFilecond.struStopTime.dwHour = 23;
        m_strFilecond.struStopTime.dwMinute = 59;
        m_strFilecond.struStopTime.dwSecond = 59;

        m_strFilecond.lChannel = 1;//通道号
        m_strFilecond.dwFileType = 0;//文件类型
        m_strFilecond.dwIsLocked = 0xff;
        m_strFilecond.dwUseCardNo = 0;

        int lFindFile = hCNetSDK.NET_DVR_FindFile_V30(lUserID, m_strFilecond);
        HCNetSDK.NET_DVR_FINDDATA_V30 strFile = new HCNetSDK.NET_DVR_FINDDATA_V30();

        if (lFindFile > -1) {
            System.out.println("file" + lFindFile);
        }

        strFile = new HCNetSDK.NET_DVR_FINDDATA_V30();
        int lnext;
        List<Vector<String>> list = new ArrayList<>();
        while (true) {
            lnext = hCNetSDK.NET_DVR_FindNextFile_V30(lFindFile, strFile);
            if (lnext == HCNetSDK.NET_DVR_FILE_SUCCESS) {
                //搜索成功
                Vector<String> newRow = new Vector<String>();

                //添加文件名信息
                String[] s = new String[2];
                s = new String(strFile.sFileName).split("\0", 2);
                newRow.add(new String(s[0]));

                int iTemp;
                String MyString;
                if (strFile.dwFileSize < 1024 * 1024) {
                    iTemp = (strFile.dwFileSize) / (1024);
                    MyString = iTemp + "K";
                } else {
                    iTemp = (strFile.dwFileSize) / (1024 * 1024);
                    MyString = iTemp + "M   ";
                    iTemp = ((strFile.dwFileSize) % (1024 * 1024)) / (1204);
                    MyString = MyString + iTemp + "K";
                }
                newRow.add(MyString);                            //添加文件大小信息
                newRow.add(strFile.struStartTime.toStringTime());//添加开始时间信息
                newRow.add(strFile.struStopTime.toStringTime()); //添加结束时间信息
                list.add(newRow);

                int i;
            } else {
                if (lnext == com.xinyuan.ms.ClientDemo.HCNetSDK.NET_DVR_ISFINDING) {//搜索中
                    System.out.println("搜索中");
                    continue;
                } else {
                    if (lnext == com.xinyuan.ms.ClientDemo.HCNetSDK.NET_DVR_FILE_NOFIND) {
                        System.out.println("没有找到文件");
                        return list;
                    } else {
                        System.out.println("搜索文件结束");
                        boolean flag = hCNetSDK.NET_DVR_FindClose_V30(lFindFile);
                        if (flag == false) {
                            System.out.println("结束搜索失败");
                        }
                        return list;
                    }
                }
            }
        }
    }


    int m_lDownloadHandle = -1;//句柄
    Timer Downloadtimer;//下载用定时器

    /**
     * 下载文件
     */
    public void download(String sFileName) {
        if (m_lDownloadHandle == -1) {
            String downloadPath = "C:\\Users\\yaoli\\Desktop\\download\\" + sFileName + ".mp4";
            //暂且将文件名作为保存的名字
            m_lDownloadHandle = hCNetSDK.NET_DVR_GetFileByName(lUserID, sFileName, downloadPath);
            if (m_lDownloadHandle >= 0) {
                hCNetSDK.NET_DVR_PlayBackControl(m_lDownloadHandle, NET_DVR_PLAYSTART, 0, null);
                Downloadtimer = new Timer();//新建定时器
                Downloadtimer.schedule(new DownloadTask(), 0, 1000);//0秒后开始响应函数
                //开始时器
            } else {
                return;
            }
        }
    }

    /**
     * 下载进度
     * */
    class DownloadTask extends java.util.TimerTask {
        //定时器函数
        @Override
        public void run() {
            int iPos = hCNetSDK.NET_DVR_GetDownloadPos(m_lDownloadHandle);
            System.out.println(iPos);
            if (iPos < 0)                       //failed
            {
                hCNetSDK.NET_DVR_StopGetFile(m_lDownloadHandle);
                m_lDownloadHandle = -1;
                Downloadtimer.cancel();
            }
            if (iPos == 100)        //end download
            {
                hCNetSDK.NET_DVR_StopGetFile(m_lDownloadHandle);
                m_lDownloadHandle = -1;
                System.out.println("下载成功");
                Downloadtimer.cancel();
            }
            if (iPos > 100)                //download exception for network problems or DVR hasten
            {
                hCNetSDK.NET_DVR_StopGetFile(m_lDownloadHandle);
                System.out.println( "由于网络原因或DVR忙,下载异常终止");
                m_lDownloadHandle = -1;
                Downloadtimer.cancel();
            }
        }
    }

}
