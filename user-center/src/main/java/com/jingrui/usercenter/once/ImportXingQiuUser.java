package com.jingrui.usercenter.once;

import com.alibaba.excel.EasyExcel;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ImportXingQiuUser {

    public static void main(String[] args) {
        String fileName = "D:\\鱼皮用户管理界面\\user-center\\user-center\\src\\main\\resources\\xingqiumembers.xlsx";
        //这里需要指定读用哪个class去读，然后读取第一个sheet同步读取会自动finish
        List<XingQiuTableUserInfo> userInfoList = EasyExcel.read(fileName).head(XingQiuTableUserInfo.class).sheet().doReadSync();

        System.out.println("总个数=" + userInfoList.size());
        Map<String, List<XingQiuTableUserInfo>> listMap = userInfoList.stream().
                filter(userInfo -> StringUtils.isNotEmpty(userInfo.getUsername()))
                .collect(Collectors.groupingBy(XingQiuTableUserInfo::getUsername));
        for (Map.Entry<String, List<XingQiuTableUserInfo>> stringListEntry : listMap.entrySet()) {
            if(stringListEntry.getValue().size() > 1){
                System.out.println("username = " + stringListEntry.getKey());
            }
        }
        System.out.println("不重复的昵称数=" + listMap.keySet().size());
    }
}
