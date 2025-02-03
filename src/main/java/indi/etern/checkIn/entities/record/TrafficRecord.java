package indi.etern.checkIn.entities.record;

import indi.etern.checkIn.entities.BaseEntity;
import indi.etern.checkIn.entities.converter.MapConverter;
import jakarta.persistence.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.*;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "records")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class TrafficRecord implements BaseEntity<String> {
    @Id
    @Column(columnDefinition = "char(36)")
    protected String id;
    @Column(columnDefinition = "char(32)")
    protected String sessionId;
    @Column(name = "record_time")
    protected LocalDateTime time;
    
    @Setter
    @Column(name = "qq")
    protected long QQNumber;
    protected String ipString;
    
    @Setter
    protected Type type;
    protected Status status;
    
    @Convert(converter = MapConverter.class)
    @Column(columnDefinition = "text")
    protected Map<String,String> requestHeader;
    
    @Convert(converter = MapConverter.class)
    @Column(columnDefinition = "text")
    protected Map<String,String> requestAttributes;
    
    @Convert(converter = MapConverter.class)
    @Column(columnDefinition = "text")
    protected Map<String,String> responseHeader;
    
    @Convert(converter = MapConverter.class)
    @Column(columnDefinition = "text")
    protected Map<String,Object> extraData;
    
    public Map<String,Object> getExtraData() {
        if (extraData == null) extraData = new HashMap<>();
        return extraData;
    }
    
    public enum Type {
        VISIT,GENERATE,SUBMIT,GET_EXAM_DATA,GET_EXAM_QUESTION
    }
    
    public enum Status {
        SUCCESS,ERROR
    }
    
    public static TrafficRecord from(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        TrafficRecord trafficRecord = new TrafficRecord();
        trafficRecord.id = UUID.randomUUID().toString();
        trafficRecord.sessionId = httpServletRequest.getSession().getId();
        trafficRecord.time = LocalDateTime.now();
        trafficRecord.ipString = httpServletRequest.getRemoteAddr();
        trafficRecord.status = Status.SUCCESS;
        Enumeration<String> requestHeaderNames = httpServletRequest.getHeaderNames();
        Collection<String> responseHeaderNames = httpServletResponse.getHeaderNames();
        Map<String, String> requestHeaderMap = new HashMap<>();
        Map<String, String> responseHeaderMap = new HashMap<>();
        Map<String, String> attributesMap = new HashMap<>();
        while (requestHeaderNames.hasMoreElements()) {
            String headName = requestHeaderNames.nextElement();
            String headValue = httpServletRequest.getHeader(headName);
            requestHeaderMap.put(headName, headValue);
        }
        for (String headName : responseHeaderNames) {
            String headValue = httpServletRequest.getHeader(headName);
            responseHeaderMap.put(headName, headValue);
        }
        httpServletRequest.getAttributeNames().asIterator().forEachRemaining(name -> {
            final Object attribute = httpServletRequest.getAttribute(name);
            if (attribute instanceof String s) {
                attributesMap.put(name, s);
            }
        });
        trafficRecord.requestHeader = requestHeaderMap;
        trafficRecord.responseHeader = responseHeaderMap;
        trafficRecord.requestAttributes = attributesMap;
        return trafficRecord;
    }
    
    public static TrafficRecord from(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Throwable throwable) {
        TrafficRecord trafficRecord = from(httpServletRequest, httpServletResponse);
        trafficRecord.status = Status.ERROR;
        Map<String, String> throwableMap = new HashMap<>();
        throwableMap.put("name", throwable.getClass().getName());
        throwableMap.put("message", throwable.getMessage());
        trafficRecord.getExtraData().put("throwable", throwableMap);
        return trafficRecord;
    }
}