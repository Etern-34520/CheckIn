package indi.etern.checkIn.entities.record;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import indi.etern.checkIn.entities.BaseEntity;
import indi.etern.checkIn.entities.converter.MapConverter;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Entity
@Table(name = "records")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class RequestRecord implements BaseEntity<String> {
    @Id
    @Column(columnDefinition = "char(36)")
    protected String id;
    
    @Column(columnDefinition = "char(32)")
    protected String sessionId;
    
    @Column(name = "record_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
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
    @JsonIgnore
    protected Map<String, String> requestHeaders;
    
    @Convert(converter = MapConverter.class)
    @Column(columnDefinition = "text")
    @JsonIgnore
    protected Map<String, String> requestAttributes;
    
    @Convert(converter = MapConverter.class)
    @Column(columnDefinition = "text")
    @JsonIgnore
    protected Map<String, String> responseHeaders;
    
    @Convert(converter = MapConverter.class)
    @Column(columnDefinition = "text")
    @JsonIgnore
    protected Map<String, Object> extraData;
    
    @Column(name = "related_exam_data_id", columnDefinition = "char(36)")
    @Setter
    protected String relatedExamDataId;
    
    @JsonIgnore
    public Map<String, Object> getExtraData() {
        if (extraData == null) extraData = new HashMap<>();
        return extraData;
    }
    
    public LinkedHashMap<String, Object> toDataMap() {
        LinkedHashMap<String, Object> dataMap = new LinkedHashMap<>();
        dataMap.put("id", id);
        dataMap.put("sessionId", sessionId);
        dataMap.put("time", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(time));
        dataMap.put("qqNumber", QQNumber);
        dataMap.put("ipString", ipString);
        dataMap.put("type", type);
        dataMap.put("status", status);
        dataMap.put("requestHeaders", requestHeaders);
        dataMap.put("requestAttributes", requestAttributes);
        dataMap.put("responseHeaders", responseHeaders);
        dataMap.put("extraData", extraData);
        dataMap.put("relatedExamDataId", relatedExamDataId);
        return dataMap;
    }
    
    public enum Type {
        VISIT, GENERATE, SUBMIT, GET_EXAM_DATA, GET_EXAM_QUESTIONS
    }
    
    public enum Status {
        SUCCESS, ERROR
    }
    
    public static RequestRecord from(HttpServletRequest httpServletRequest, @Nullable HttpServletResponse httpServletResponse) {
        RequestRecord requestRecord = new RequestRecord();
        requestRecord.id = UUID.randomUUID().toString();
        requestRecord.sessionId = httpServletRequest.getSession().getId();
        requestRecord.time = LocalDateTime.now();
        requestRecord.ipString = httpServletRequest.getRemoteAddr();
        requestRecord.status = Status.SUCCESS;
        Enumeration<String> requestHeaderNames = httpServletRequest.getHeaderNames();
        Map<String, String> requestHeaderMap = new HashMap<>();
        Map<String, String> responseHeaderMap = new HashMap<>();
        Map<String, String> attributesMap = new HashMap<>();
        if (httpServletResponse != null) {
            Collection<String> responseHeaderNames = httpServletResponse.getHeaderNames();
            for (String headName : responseHeaderNames) {
                String headValue = httpServletRequest.getHeader(headName);
                responseHeaderMap.put(headName, headValue);
            }
        }
        while (requestHeaderNames.hasMoreElements()) {
            String headName = requestHeaderNames.nextElement();
            if (!headName.equals("cookie")) {
                String headValue = httpServletRequest.getHeader(headName);
                requestHeaderMap.put(headName, headValue);
            }
        }
        httpServletRequest.getAttributeNames().asIterator().forEachRemaining(name -> {
            final Object attribute = httpServletRequest.getAttribute(name);
            if (attribute instanceof String s) {
                attributesMap.put(name, s);
            }
        });
        requestRecord.requestHeaders = requestHeaderMap;
        requestRecord.responseHeaders = responseHeaderMap;
        requestRecord.requestAttributes = attributesMap;
        return requestRecord;
    }
    
    public static RequestRecord from(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Throwable throwable) {
        RequestRecord requestRecord = from(httpServletRequest, httpServletResponse);
        requestRecord.status = Status.ERROR;
        Map<String, Object> throwableMap = new HashMap<>();
        throwableMap.put("name", throwable.getClass().getName());
        throwableMap.put("message", throwable.getMessage());
        throwableMap.put("stackTrace", Arrays.stream(throwable.getStackTrace()).map(StackTraceElement::toString).toList());
        requestRecord.getExtraData().put("throwable", throwableMap);
        return requestRecord;
    }
}