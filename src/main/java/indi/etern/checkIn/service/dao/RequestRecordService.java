package indi.etern.checkIn.service.dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import indi.etern.checkIn.entities.record.RequestRecord;
import indi.etern.checkIn.repositories.RequestRecordRepository;
import indi.etern.checkIn.service.web.WebSocketService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
public class RequestRecordService {
    public static RequestRecordService singletonInstance;
    private final WebSocketService webSocketService;
    private final RequestRecordRepository requestRecordRepository;

    public RequestRecordService(WebSocketService webSocketService, ObjectMapper objectMapper, RequestRecordRepository requestRecordRepository) {
        this.webSocketService = webSocketService;
        this.requestRecordRepository = requestRecordRepository;
        RequestRecordService.singletonInstance = this;
    }
    
    public void save(RequestRecord requestRecord) {
        requestRecordRepository.save(requestRecord);
    }
    
    public List<RequestRecord> findByLocalDateFromTo(LocalDate from, LocalDate to) {
        return requestRecordRepository.findAllByTimeBetween(from.atStartOfDay(),to.plusDays(1).atStartOfDay());
    }
    
    public List<RequestRecord> findAllBySessionId(String sessionId) {
        return requestRecordRepository.findAllBySessionId(sessionId);
    }
    
    public void saveAll(Collection<RequestRecord> requestRecords) {
        requestRecordRepository.saveAll(requestRecords);
    }
    
    public Optional<RequestRecord> findById(String id) {
        return requestRecordRepository.findById(id);
    }
}