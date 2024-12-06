package indi.etern.checkIn.service.dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import indi.etern.checkIn.service.web.WebSocketService;
import org.springframework.stereotype.Service;

@Service
public class TrafficRecordService {
    public static TrafficRecordService singletonInstance;
    private final WebSocketService webSocketService;
    private final ObjectMapper objectMapper;

    public TrafficRecordService(WebSocketService webSocketService, ObjectMapper objectMapper) {
        this.webSocketService = webSocketService;
        this.objectMapper = objectMapper;
        TrafficRecordService.singletonInstance = this;
    }
}