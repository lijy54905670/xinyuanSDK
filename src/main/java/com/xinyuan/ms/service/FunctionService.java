package com.xinyuan.ms.service;

import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.examples.win32.W32API;
import com.sun.jna.ptr.ByteByReference;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.NativeLongByReference;
import com.xinyuan.ms.Utils.HexUtils;
import com.xinyuan.ms.entity.Test;
import com.xinyuan.ms.mapper.TestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.swing.*;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Timer;

import static com.xinyuan.ms.service.HCNetSDK.*;

@Service
public class FunctionService extends BaseService<TestRepository, Test, Long> {

    @Autowired
    BasicService basicService;
    @Autowired
    WebSocketServer webSocketServer;

    static HCNetSDK hCNetSDK = HCNetSDK.INSTANCE;

    public int lAlarmHandle;//报警布防句柄
    Integer lPreviewHandle;
    public int lListenHandle;//报警监听句柄
    HCNetSDK.NET_DVR_CLIENTINFO m_strClientInfo;//用户参数
    public static int code = 5;
    FMSGCallBack fMSFCallBack;
    FRealDataCallBack fRealDataCallBack;


    public void initMemberFlowUpload() throws InterruptedException {
        int lUserID = basicService.lUserID;//用户句柄
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
        try {
            webSocketServer.BroadCastInfo("布防成功,开始监测抓拍");
        } catch (IOException e) {
            e.printStackTrace();
        }

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

    public void show() {
        int lUserID = basicService.lUserID;
        if (lUserID == -1) {
            System.out.println("请先注册!");
        }

        //如果预览窗口没打开,不在预览

            //获取通道号
            int iChannelNum = 1;//通道号
            if (iChannelNum == -1) {
                System.out.println("通道号不存在");;
                return;
            }

            m_strClientInfo = new HCNetSDK.NET_DVR_CLIENTINFO();
            m_strClientInfo.lChannel = iChannelNum;
            if (fRealDataCallBack == null) {
                fRealDataCallBack = new FRealDataCallBack();
            }

            //在此判断是否回调预览,0,不回调 1 回调

                m_strClientInfo.hPlayWnd = null;
                lPreviewHandle = hCNetSDK.NET_DVR_RealPlay_V30(lUserID,
                        m_strClientInfo, fRealDataCallBack, null, true);

            long previewSucValue = lPreviewHandle.longValue();
            //预览失败时:
            if (previewSucValue == -1) {
                System.out.println("预览失败！");
                return;
            }

            //预览成功的操作

            //显示云台控制窗口

        }

        //如果在预览,停止预览,关闭窗口




    //报警回调函数
    public class FMSGCallBack implements HCNetSDK.FMSGCallBack {
        @Override
        public void invoke(int lCommand, HCNetSDK.NET_DVR_ALARMER pAlarmer, Pointer pAlarmInfo, int dwBufLen, Pointer pUser) {
            System.out.println("监听开始抓拍");
            try {
                webSocketServer.BroadCastInfo("监听开始抓拍");
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                String sAlarmType = new String();
                String[] newRow = new String[3];
                Date today = new Date();
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                String format = dateFormat.format(today);
                Test test = new Test();
                test.setTime(format);
                String[] sIP = new String[2];
                switch (lCommand) {

                    case COMM_ALARM_RULE:    //行为分析信息上传
                        System.out.println("抓拍成功！");
                        try {
                            webSocketServer.BroadCastInfo("抓拍成功！");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        HCNetSDK.NET_VCA_RULE_ALARM strVcaAlarm = new HCNetSDK.NET_VCA_RULE_ALARM();
                        strVcaAlarm.write();
                        Pointer pVcaInfo = strVcaAlarm.getPointer();
                        pVcaInfo.write(0, pAlarmInfo.getByteArray(0, strVcaAlarm.size()), 0, strVcaAlarm.size());
                        strVcaAlarm.read();
                        String type = "OTHER";
                        test.setIpAddress(new String(strVcaAlarm.struDevInfo.struDevIP.sIpV4).trim());
                        test.setChannel(strVcaAlarm.struDevInfo.byChannel);
                        switch (strVcaAlarm.struRuleInfo.wEventTypeEx) {
                            case 41:
                                System.out.println("人员滞留");
                                try {
                                    webSocketServer.BroadCastInfo("人员滞留");
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                type = "RETENTION";
                                break;
                            case 15:
                                System.out.println("人员离岗");
                                try {
                                    webSocketServer.BroadCastInfo("人员离岗");
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                type = "ABSENCE";
                                break;
                            case 1:
                                System.out.println("人员越界");
                                try {
                                    webSocketServer.BroadCastInfo("人员越界");
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                type = "LINEDETECT";
                                break;
                            default:
                                sAlarmType = sAlarmType + new String("：其他行为分析报警，事件类型：")
                                        + strVcaAlarm.struRuleInfo.wEventTypeEx +
                                        "_wPort:" + strVcaAlarm.struDevInfo.wPort +
                                        "_byChannel:" + strVcaAlarm.struDevInfo.byChannel +
                                        "_byIvmsChannel:" + strVcaAlarm.struDevInfo.byIvmsChannel +
                                        "_Dev IP：" + new String(strVcaAlarm.struDevInfo.struDevIP.sIpV4);
                                try {
                                    webSocketServer.BroadCastInfo("其他行为分析报警");
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                break;
                        }
                        test.setType(type.trim());
                        save(test);
                        System.out.println(sAlarmType + "     123");
                        newRow[0] = dateFormat.format(today);
                        //报警类型
                        newRow[1] = sAlarmType;
                        //报警设备IP地址
                        sIP = new String(pAlarmer.sDeviceIP).split("\0", 2);
                        newRow[2] = sIP[0];

//                        if (strVcaAlarm.dwPicDataLen > 0) {
//                            SimpleDateFormat sf = new SimpleDateFormat("yyyyMMddHHmmss");
//                            String newName = sf.format(today);
//                            FileOutputStream fout;
//                            try {
//                                fout = new FileOutputStream("C:\\Users\\yaoli\\Desktop\\pic\\" + "ch" + strVcaAlarm.struDevInfo.byIvmsChannel + "_" + newName + "_" + type + ".jpg");
//                                //将字节写入文件
//                                long offset = 0;
//                                ByteBuffer buffers = strVcaAlarm.pImage.getByteBuffer(offset, strVcaAlarm.dwPicDataLen);
//                                byte[] bytes = new byte[strVcaAlarm.dwPicDataLen];
//                                buffers.rewind();
//                                buffers.get(bytes);
//                                fout.write(bytes);
//                                fout.close();
//                            } catch (FileNotFoundException e) {
//                                e.printStackTrace();
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
//                        }
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
    NET_DVR_IPPARACFG m_strIpparaCfg;//IP参数

    /**
     * 建立设备通道树
     *
     * @return
     */
    public List<Map<String, Object>> CreateDeviceTree() {
        int lUserID = basicService.lUserID;//用户句柄
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
        int lUserID = basicService.lUserID;//用户句柄

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
                if (lnext == HCNetSDK.NET_DVR_ISFINDING) {//搜索中
                    System.out.println("搜索中");
                    continue;
                } else {
                    if (lnext == HCNetSDK.NET_DVR_FILE_NOFIND) {
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
        int lUserID = basicService.lUserID;//用户句柄
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
     */
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
                System.out.println("下载成功");
                m_lDownloadHandle = -1;
                Downloadtimer.cancel();
            }
            if (iPos > 100)                //download exception for network problems or DVR hasten
            {
                hCNetSDK.NET_DVR_StopGetFile(m_lDownloadHandle);
                System.out.println("由于网络原因或DVR忙,下载异常终止");
                m_lDownloadHandle = -1;
                Downloadtimer.cancel();
            }
        }
    }
    static PlayCtrl playControl = PlayCtrl.INSTANCE;
    IntByReference m_lPort;//回调预览时播放库端口指针
    public class FRealDataCallBack implements HCNetSDK.FRealDataCallBack_V30 {
        @Override
        public void invoke(int lRealHandle, int dwDataType, ByteByReference pBuffer, int dwBufSize, Pointer pUser) {
            byte[] bytes = ref2Bytes(pBuffer,dwBufSize);
            switch (dwDataType) {
                case HCNetSDK.NET_DVR_SYSHEAD: //系统头

                    if (!playControl.PlayM4_GetPort(m_lPort)) //获取播放库未使用的通道号
                    {
                        break;
                    }

                    if (dwBufSize > 0) {
                        if (!playControl.PlayM4_SetStreamOpenMode(m_lPort.getValue(), PlayCtrl.STREAME_REALTIME))  //设置实时流播放模式
                        {
                            break;
                        }

                        if (!playControl.PlayM4_OpenStream(m_lPort.getValue(), pBuffer, dwBufSize, 1024 * 1024)) //打开流接口
                        {
                            break;
                        }
                        
                    }
                case HCNetSDK.NET_DVR_STREAMDATA:   //码流数据
                    System.out.println("++++++++++++++++++++++++++");
                    System.out.println(dwBufSize);
                    System.out.println("--------------------------");
            }
            System.out.println(HexUtils.bytes2Hex(bytes));
            System.out.println("****************************");
        }
        public byte[] ref2Bytes(ByteByReference buf, int dwBufSize){
            return buf.getPointer().getByteArray(0,dwBufSize);//通过字节指针获取指定长度的字节数组
        }
        }
        //预览回调

    }
