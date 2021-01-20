package com.xinyuan.ms.mapper;

import com.sun.jna.Pointer;
import com.xinyuan.ms.entity.User;
import com.xinyuan.ms.entity.hkwsTest;
import com.xinyuan.ms.service.BaseService;
import com.xinyuan.ms.service.HCNetSDK;
import org.postgresql.core.NativeQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.xml.crypto.Data;
import java.lang.annotation.Native;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;


public class MemberFlowUPloadCallBackImpl extends BaseService<hkwsTestRepository, hkwsTest,Long> implements HCNetSDK.FMSGCallBack_V31 {
    /*@Autowired
    private StatisticsMemberInOutService statisticsMemberInOutService;*/
    @Autowired
    EntityManager entityManager;

    @Transactional
    @Override
    public boolean invoke(int lCommand, HCNetSDK.NET_DVR_ALARMER pAlarmer, Pointer pAlarmInfo, int dwBufLen, Pointer pUser) {
        System.out.println("进入回调了");
        try {
            String sAlarmType = new String();
            //报警时间
            Date today = new Date();
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            String[] sIP = new String[2];

            sAlarmType = new String("lCommand=") + lCommand;
            //lCommand是传的报警类型
            HCNetSDK.NET_DVR_PDC_ALRAM_INFO strPDCResult = new HCNetSDK.NET_DVR_PDC_ALRAM_INFO();
            strPDCResult.write();
            Pointer pPDCInfo = strPDCResult.getPointer();
            pPDCInfo.write(0, pAlarmInfo.getByteArray(0, strPDCResult.size()), 0, strPDCResult.size());
            strPDCResult.read();

            if (strPDCResult.byMode == 0) {
                strPDCResult.uStatModeParam.setType(HCNetSDK.NET_DVR_STATFRAME.class);
                sAlarmType = sAlarmType + "：客流量统计，进入人数：" + strPDCResult.dwEnterNum + "，离开人数：" + strPDCResult.dwLeaveNum +
                        ", byMode:" + strPDCResult.byMode + ", dwRelativeTime:" + strPDCResult.uStatModeParam.struStatFrame.dwRelativeTime +
                        ", dwAbsTime:" + strPDCResult.uStatModeParam.struStatFrame.dwAbsTime;
                System.out.println("准备插入数据库");
//                StringBuffer sql = new StringBuffer("INSERT INTO sys_hkws (in_person, out_person, time) VALUES (");
//                sql.append(strPDCResult.dwEnterNum);
//                sql.append(",");
//                sql.append(strPDCResult.dwLeaveNum + ",'");
                Date data = new Date();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-DD HH:mm:ss");
                String str = simpleDateFormat.format(data);
//                sql.append(str + "')");
//
//                Query query = entityManager.createQuery(sql.toString());
//                query.executeUpdate();
                System.out.println("数据库插入");
                hkwsTest hkwsTest = new hkwsTest();
                hkwsTest.setInPerson(strPDCResult.dwEnterNum);
                hkwsTest.setInPerson(strPDCResult.dwLeaveNum);
                hkwsTest.setTime(str);
                save(hkwsTest);

            }
            if (strPDCResult.byMode == 1) {
                strPDCResult.uStatModeParam.setType(HCNetSDK.NET_DVR_STATTIME.class);
                //在这里实现数据的保存等业务逻辑，下面注释的代码是SDK提供的参考示例
               /* StringBuffer sql = new StringBuffer("INSERT INTO sys_hkws (in_person, out_person, time) VALUES ('");
                sql.append(strPDCResult.dwEnterNum);
                sql.append("','");
                sql.append(strPDCResult.dwLeaveNum + "','");
                Date data = new Date();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-DD HH:mm:ss");
                String str = simpleDateFormat.format(data);
                sql.append(str + ")");
                Query query = entityManager.createQuery(sql.toString());
                query.executeUpdate();
                System.out.println("数据库插入121212");*/

/*                    String strtmStart = "" + String.format("%04d", strPDCResult.uStatModeParam.struStatTime.tmStart.dwYear) +
                            String.format("%02d", strPDCResult.uStatModeParam.struStatTime.tmStart.dwMonth) +
                            String.format("%02d", strPDCResult.uStatModeParam.struStatTime.tmStart.dwDay) +
                            String.format("%02d", strPDCResult.uStatModeParam.struStatTime.tmStart.dwHour) +
                            String.format("%02d", strPDCResult.uStatModeParam.struStatTime.tmStart.dwMinute) +
                            String.format("%02d", strPDCResult.uStatModeParam.struStatTime.tmStart.dwSecond);
                    String strtmEnd = "" + String.format("%04d", strPDCResult.uStatModeParam.struStatTime.tmEnd.dwYear) +
                            String.format("%02d", strPDCResult.uStatModeParam.struStatTime.tmEnd.dwMonth) +
                            String.format("%02d", strPDCResult.uStatModeParam.struStatTime.tmEnd.dwDay) +
                            String.format("%02d", strPDCResult.uStatModeParam.struStatTime.tmEnd.dwHour) +
                            String.format("%02d", strPDCResult.uStatModeParam.struStatTime.tmEnd.dwMinute) +
                            String.format("%02d", strPDCResult.uStatModeParam.struStatTime.tmEnd.dwSecond);
                    sAlarmType = sAlarmType + "：客流量统计，进入人数：" + strPDCResult.dwEnterNum + "，离开人数：" + strPDCResult.dwLeaveNum +
                            ", byMode:" + strPDCResult.byMode + ", tmStart:" + strtmStart + ",tmEnd :" + strtmEnd;*/
            }
            System.out.println("sAlarmType---》" + sAlarmType);
            //报警类型
            //报警设备IP地址
            sIP = new String(strPDCResult.struDevInfo.struDevIP.sIpV4).split("\0", 2);
            return true;
        } catch (Exception ex) {
            Logger.getLogger(MemberFlowUPloadCallBackImpl.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
}
