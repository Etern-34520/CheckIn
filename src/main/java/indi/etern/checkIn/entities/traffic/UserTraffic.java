package indi.etern.checkIn.entities.traffic;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import indi.etern.checkIn.MVCConfig;
import indi.etern.checkIn.entities.convertor.LocalDateToDateConvertor;
import indi.etern.checkIn.entities.convertor.LocalTimeToTimestampConvertor;
import jakarta.persistence.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.SneakyThrows;
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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
    @Column(name = "HEADER", columnDefinition = "text")
    @JsonIgnore
    String deviceInfo;
    @Column(name = "ATTRIBUTES", columnDefinition = "text")
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

    @SneakyThrows
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
        ObjectMapper objectMapper = MVCConfig.getObjectMapper();
        deviceInfo = objectMapper.writeValueAsString(headerMap);
        attributes = objectMapper.writeValueAsString(attributesMap);
    }

    @SuppressWarnings("unchecked")
    @SneakyThrows
    public Map<String, String> getHeaderMap() {
        if (headerMap == null) {
            headerMap = MVCConfig.getObjectMapper().readValue(deviceInfo, Map.class);
        }
        return headerMap;
    }
    
    @SuppressWarnings("unchecked")
    @SneakyThrows
    public Map<String, String> getAttributesMap() {
        if (attributesMap == null) {
            attributesMap = MVCConfig.getObjectMapper().readValue(attributes, Map.class);
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
