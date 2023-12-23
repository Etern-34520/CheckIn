package indi.etern.checkIn.beans;

import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Getter
@Component
public class ManageMenuItemInfo {
    public List<String> menuItemServerStrings = new ArrayList<>();
    public List<String> menuItemUserStrings = new ArrayList<>();
    public ManageMenuItemInfo(){
        menuItemServerStrings.add("主页");
        menuItemServerStrings.add("流量");
        menuItemServerStrings.add("题库");
//        menuItemServerStrings.add("分区");
        menuItemServerStrings.add("信息");
        menuItemUserStrings.add("用户管理");
        menuItemUserStrings.add("权限组管理");
        menuItemUserStrings.add("设置");
    }
    
}
