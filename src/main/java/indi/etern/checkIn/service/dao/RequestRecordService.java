package indi.etern.checkIn.service.dao;

import indi.etern.checkIn.entities.record.RequestRecord;
import indi.etern.checkIn.repositories.RequestRecordRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
public class RequestRecordService {
    public static RequestRecordService singletonInstance;
    private final RequestRecordRepository requestRecordRepository;

    public RequestRecordService(RequestRecordRepository requestRecordRepository) {
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
    
    public List<RequestRecord> findAllByExamDataId(String examDataId) {
        return requestRecordRepository.findAllByRelatedExamDataId(examDataId);
    }
    
    public void saveAndFlush(RequestRecord requestRecord) {
        requestRecordRepository.saveAndFlush(requestRecord);
    }
}