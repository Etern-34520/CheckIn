package indi.etern.checkIn.entities.traffic;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import com.google.gson.Gson;
import indi.etern.checkIn.MVCConfig;
import indi.etern.checkIn.entities.convertor.LocalDateToDateConvertor;
import indi.etern.checkIn.entities.convertor.LocalTimeToTimestampConvertor;
import jakarta.persistence.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.format.annotation.DateTimeFormat;

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
    protected long qqNumber;

    @Column(name = "DATE")
    @Convert(converter = LocalDateToDateConvertor.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    LocalDate localDate;
    @Column(name = "TIME")
    @Convert(converter = LocalTimeToTimestampConvertor.class)
    @JsonSerialize(using = LocalTimeSerializer.class)
    @JsonDeserialize(using = LocalTimeDeserializer.class)
    @JsonFormat(pattern = "HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "HH:mm:ss")
    LocalTime localTime;
    @Column(name = "IP")
    String ipString;
    @Column(name = "HEADER")
    @JsonIgnore
    String deviceInfo;
    @Column(name = "ATTRIBUTES")
    @JsonIgnore
    String attributes;
    @Transient
    @JsonIgnore
    Map<String, String> headerMap;
    @Transient
    @JsonIgnore
    Map<String, String> attributesMap;

    protected UserTraffic() {
        localDate = LocalDate.now();
    }

    public UserTraffic(long qqNumber, HttpServletRequest httpServletRequest) {
        localDate = LocalDate.now();
        localTime = LocalTime.now();
        this.qqNumber = qqNumber;
        ipString = httpServletRequest.getRemoteAddr();
        Enumeration<String> headerNames = httpServletRequest.getHeaderNames();
        headerMap = new HashMap<>();
        attributesMap = new HashMap<>();
        while (headerNames.hasMoreElements()) {
            String headName = headerNames.nextElement();
            String headValue = httpServletRequest.getHeader(headName);//根据请求头的名字获取对应的请求头的值
            headerMap.put(headName, headValue);
        }
        httpServletRequest.getAttributeNames().asIterator().forEachRemaining(name -> {
            final Object attribute = httpServletRequest.getAttribute(name);
            if (attribute instanceof String s) {
                attributesMap.put(name, s);
            }
        });
        Gson gson = MVCConfig.getGson();
        deviceInfo = gson.toJson(headerMap);
        attributes = gson.toJson(attributesMap);
    }

    public Map<String, String> getHeaderMap() {
        if (headerMap == null) {
            headerMap = MVCConfig.getGson().fromJson(deviceInfo, Map.class);
        }
        return headerMap;
    }

    public Map<String, String> getAttributesMap() {
        if (attributesMap == null) {
            attributesMap = MVCConfig.getGson().fromJson(attributes, Map.class);
        }
        return attributesMap;
    }

    public String getIP() {
        return ipString;
    }

    public long getQQNumber() {
        return qqNumber;
    }

    public LocalDate getLocalDate() {
        return localDate;
    }

    public LocalTime getLocalTime() {
        return localTime;
    }

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public LocalDateTime getLocalDateTime() {
        return LocalDateTime.of(localDate, localTime);
    }

    public int getId() {
        return id;
    }
}
