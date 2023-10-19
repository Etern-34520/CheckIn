package indi.etern.checkIn.beans;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ManageMenuItemInfo {
    public List<String> menuItemServerStrings = new ArrayList<>();
    public List<String> menuItemUserStrings = new ArrayList<>();
    public ManageMenuItemInfo(){
        menuItemServerStrings.add("Home");
        menuItemServerStrings.add("traffic");
        menuItemServerStrings.add("Questions");
        menuItemServerStrings.add("Partition");
        menuItemServerStrings.add("Information");
        menuItemUserStrings.add("Manage");
        menuItemUserStrings.add("Preference");
    }
    
    public List<String> getMenuItemServerStrings(){
        return menuItemServerStrings;
    }
    public List<String> getMenuItemUserStrings(){
        return menuItemUserStrings;
    }
}
