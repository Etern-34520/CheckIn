package indi.etern.checkIn.controller.rest;

import com.google.gson.Gson;
import indi.etern.checkIn.entities.traffic.DateTraffic;
import indi.etern.checkIn.entities.traffic.UserTraffic;
import indi.etern.checkIn.service.dao.DateTrafficService;
import indi.etern.checkIn.service.dao.UserTrafficService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping(path = "manage/data")
public class ManageData {
    @Autowired
    DateTrafficService dateTrafficService;
    @Autowired
    UserTrafficService userTrafficService;
    
    @GetMapping(params = "type=traffic")
    public String getTrafficJsonData() {
        Gson gson = new Gson();
        final LocalDate now = LocalDate.now();
        List<DateTraffic> dateTraffics = dateTrafficService.getAllFromTo(now.minusDays(6), now);
        List<Map<String, String>> jsonData = new ArrayList<>();
        for (DateTraffic dateTraffic : dateTraffics) {
            final HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("count", String.valueOf(dateTraffic.getCount()));
            hashMap.put("date", dateTraffic.getLocalDate().getMonthValue()+"-"+dateTraffic.getLocalDate().getDayOfMonth());
            jsonData.add(hashMap);
        }
        return gson.toJson(jsonData);
    }
    
    @GetMapping(params = {"type=trafficDetail","pageIndex"})
    public String getTrafficDetailData(int pageIndex){
        Gson gson = new Gson();
        Sort sort = Sort.by(Sort.Direction.DESC, "localDate");
        Pageable pageable = PageRequest.of(pageIndex, 20, sort);
        DateTraffic dateTraffic = dateTrafficService.getByLocalDate(LocalDate.now().minusDays(pageIndex));
        List<Map<String,String>> jsonData = new ArrayList<>();
        for (UserTraffic userTraffic:dateTraffic.getUserTraffics()){
            Map<String, String> userTrafficDataMap = userTraffic.getHeaderInfo();
            userTrafficDataMap.put("ip", userTraffic.getIP());
            userTrafficDataMap.put("qq", String.valueOf(userTraffic.getQQNumber()));
            userTrafficDataMap.put("date", userTraffic.getLocalDate().toString());
            userTrafficDataMap.put("time", userTraffic.getLocalTime().toString());
            userTrafficDataMap.put("dateTime", userTraffic.getLocalDateTime().toString());
            jsonData.add(userTrafficDataMap);
        }
        return gson.toJson(jsonData);
    }
    
    
}
