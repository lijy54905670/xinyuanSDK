package com.xinyuan.ms.service;

import org.springframework.stereotype.Service;

@Service
public class loginTestService {
    HCNetSDK hCNetSDK = HCNetSDK.INSTANCE;
    com.xinyuan.ms.service.HCNetSDK.NET_DVR_DEVICEINFO_V40 m_strDeviceInfo = new HCNetSDK.NET_DVR_DEVICEINFO_V40();
    HCNetSDK.NET_DVR_USER_LOGIN_INFO m_strLoginInfo = new HCNetSDK.NET_DVR_USER_LOGIN_INFO();


    int lUserID;
    public boolean login(String ip, short port, String userName, String pwd) {// 注册设备-登录参数，包括设备地址、登录用户、密码等
    m_strLoginInfo.sDeviceAddress = new byte[hCNetSDK.NET_DVR_DEV_ADDRESS_MAX_LEN];
        System.arraycopy(ip.getBytes(), 0, m_strLoginInfo.sDeviceAddress, 0, ip.length());
    m_strLoginInfo.sUserName = new byte[hCNetSDK.NET_DVR_LOGIN_USERNAME_MAX_LEN];
        System.arraycopy(userName.getBytes(), 0, m_strLoginInfo.sUserName, 0,userName.length());
    m_strLoginInfo.sPassword = new byte[hCNetSDK.NET_DVR_LOGIN_PASSWD_MAX_LEN];
        System.arraycopy(pwd.getBytes(), 0, m_strLoginInfo.sPassword, 0, pwd.length());
    m_strLoginInfo.wPort = port;
    m_strLoginInfo.bUseAsynLogin = false; //是否异步登录：0- 否，1- 是
        m_strLoginInfo.write();



        hCNetSDK.NET_DVR_Init();
    //设备基本信息
    HCNetSDK.NET_DVR_DEVICEINFO_V30 m_str = new HCNetSDK.NET_DVR_DEVICEINFO_V30();
        System.out.println(m_str);
    //调用登录接口
    lUserID = hCNetSDK.NET_DVR_Login_V40(m_strLoginInfo, m_strDeviceInfo);
    //lUserID = hCNetSDK.NET_DVR_Login_V40(loginRequest.getIp(), (short) loginRequest.getPort(), loginRequest.getUserName(), loginRequest.getPwd(), m_str);
        System.out.println(1);
    //long userID = lUserID.longValue();
        if (lUserID == -1) {
        System.out.println("登录失败");
        return false;
    } else {
        System.out.println("登录成功");
        return true;
    }
}
}
