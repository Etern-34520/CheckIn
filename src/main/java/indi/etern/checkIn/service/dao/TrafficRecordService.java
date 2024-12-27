package indi.etern.checkIn.service.dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import indi.etern.checkIn.entities.record.TrafficRecord;
import indi.etern.checkIn.repositories.TrafficRecordRepository;
import indi.etern.checkIn.service.web.WebSocketService;
import org.springframework.stereotype.Service;

@Service
public class TrafficRecordService {
    public static TrafficRecordService singletonInstance;
    private final WebSocketService webSocketService;
    private final ObjectMapper objectMapper;
    private final TrafficRecordRepository trafficRecordRepository;

    public TrafficRecordService(WebSocketService webSocketService, ObjectMapper objectMapper, TrafficRecordRepository trafficRecordRepository) {
        this.webSocketService = webSocketService;
        this.objectMapper = objectMapper;
        this.trafficRecordRepository = trafficRecordRepository;
        TrafficRecordService.singletonInstance = this;
    }
    
    public void save(TrafficRecord trafficRecord) {
        trafficRecordRepository.save(trafficRecord);
    }
}