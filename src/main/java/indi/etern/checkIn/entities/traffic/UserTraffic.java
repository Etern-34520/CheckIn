package indi.etern.checkIn.entities.traffic;

import com.google.gson.Gson;
import indi.etern.checkIn.entities.convertor.LocalDateToDateConvertor;
import indi.etern.checkIn.entities.convertor.LocalTimeToTimestampConvertor;
import jakarta.persistence.*;
import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "USER_TRAFFICS")
public class UserTraffic {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_traffics_SEQ")
    @SequenceGenerator(name = "user_traffics_SEQ", sequenceName = "user_traffics_SEQ", allocationSize = 1)
    @Column(name = "ID")
    private int id;
    @Column(name = "QQ_NUMBER")
    protected int qqNumber;
    @Column(name = "DATE")
    @Convert(converter = LocalDateToDateConvertor.class)
    LocalDate localDate;
    
    @Column(name = "TIME")
    @Convert(converter = LocalTimeToTimestampConvertor.class)
    LocalTime localTime;
    @Column(name = "IP")
    String ipString;
    @Column(name = "DEVICE_INFO")
    String deviceInfo;
    @Transient
    Map<String,String> headerInfo;
    protected UserTraffic() {
        localDate = LocalDate.now();
    }
    
    public UserTraffic(int qqNumber, HttpServletRequest httpServletRequest) {
        localDate = LocalDate.now();
        localTime = LocalTime.now();
        this.qqNumber = qqNumber;
        ipString = httpServletRequest.getRemoteAddr();
        Enumeration<String> headerNames = httpServletRequest.getHeaderNames();
        headerInfo = new HashMap<>();
        while (headerNames.hasMoreElements()) {
            String headName = headerNames.nextElement();
            String headValue = httpServletRequest.getHeader(headName);//根据请求头的名字获取对应的请求头的值
            headerInfo.put(headName, headValue);
        }
        Gson gson = new Gson();
        deviceInfo = gson.toJson(headerInfo);
    }
    public Map<String,String> getHeaderInfo(){
        if (headerInfo == null){
            headerInfo = new Gson().fromJson(deviceInfo,Map.class);
        }
        return headerInfo;
    }
    
    public String getIP() {
        return ipString;
    }
    
    public int getQQNumber() {
        return qqNumber;
    }
    
    public LocalDate getLocalDate() {
        return localDate;
    }
    
    public LocalTime getLocalTime(){
        return localTime;
    }
    
    public LocalDateTime getLocalDateTime(){
        return LocalDateTime.of(localDate,localTime);
    }
}
