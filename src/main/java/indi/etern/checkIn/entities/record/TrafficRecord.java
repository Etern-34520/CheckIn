package indi.etern.checkIn.entities.record;

import com.fasterxml.jackson.databind.ObjectMapper;
import indi.etern.checkIn.MVCConfig;
import indi.etern.checkIn.entities.convertor.MapConverter;
import jakarta.persistence.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.*;
import org.springframework.context.annotation.Lazy;

import java.time.LocalDateTime;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "records")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class TrafficRecord {
    @Id
    protected String id;
    protected String sessionId;
    @Column(name = "record_time")
    protected LocalDateTime time;
    protected long qq;
    protected String ipString;
    protected Type type;
    
    @Convert(converter = MapConverter.class)
    @Lazy
    protected Map<String,String> header;
    
    @Convert(converter = MapConverter.class)
    @Lazy
    protected Map<String,String> attributes;
    
    @Convert(converter = MapConverter.class)
    @Lazy
    protected Map<String,String> extraData;
    public enum Type {
        VISIT,GENERATE,SUBMIT,GET_RESULT
    }
    
    public static TrafficRecord from(HttpServletRequest httpServletRequest) {
        TrafficRecord trafficRecord = new TrafficRecord();
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
        ObjectMapper objectMapper = MVCConfig.getObjectMapper();
        trafficRecord.header = headerMap;
        trafficRecord.attributes = attributesMap;
        return trafficRecord;
    }
}