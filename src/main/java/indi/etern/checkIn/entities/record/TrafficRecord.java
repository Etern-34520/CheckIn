package indi.etern.checkIn.entities.record;

import indi.etern.checkIn.entities.BaseEntity;
import indi.etern.checkIn.entities.convertor.MapConverter;
import jakarta.persistence.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.*;
import org.springframework.context.annotation.Lazy;

import java.time.LocalDateTime;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
    
    @Convert(converter = MapConverter.class)
    @Lazy
    @Column(columnDefinition = "text")
    protected Map<String,String> header;
    
    @Convert(converter = MapConverter.class)
    @Lazy
    @Column(columnDefinition = "text")
    protected Map<String,String> attributes;
    
    @Convert(converter = MapConverter.class)
    @Lazy
    @Column(columnDefinition = "text")
    protected Map<String,Object> extraData;
    
    public Map<String,Object> getExtraData() {
        if (extraData == null) extraData = new HashMap<>();
        return extraData;
    }
    
    public enum Type {
        VISIT,GENERATE,SUBMIT
    }
    
    public static TrafficRecord from(HttpServletRequest httpServletRequest) {
        TrafficRecord trafficRecord = new TrafficRecord();
        trafficRecord.id = UUID.randomUUID().toString();
        trafficRecord.sessionId = httpServletRequest.getSession().getId();
        trafficRecord.time = LocalDateTime.now();
        trafficRecord.ipString = httpServletRequest.getRemoteAddr();
        Enumeration<String> headerNames = httpServletRequest.getHeaderNames();
        Map<String, String> headerMap = new HashMap<>();
        Map<String, String> attributesMap = new HashMap<>();
        while (headerNames.hasMoreElements()) {
            String headName = headerNames.nextElement();
            String headValue = httpServletRequest.getHeader(headName);
            headerMap.put(headName, headValue);
        }
        httpServletRequest.getAttributeNames().asIterator().forEachRemaining(name -> {
            final Object attribute = httpServletRequest.getAttribute(name);
            if (attribute instanceof String s) {
                attributesMap.put(name, s);
            }
        });
        trafficRecord.header = headerMap;
        trafficRecord.attributes = attributesMap;
        return trafficRecord;
    }
}