package indi.etern.checkIn.repositories;

import indi.etern.checkIn.entities.record.RequestRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface RequestRecordRepository extends JpaRepository<RequestRecord,String> {
    List<RequestRecord> findAllByTimeBetween(LocalDateTime timeAfter, LocalDateTime timeBefore);
    List<RequestRecord> findAllBySessionId(String sessionId);
    List<RequestRecord> findAllByRelatedExamDataId(String relatedExamDataId);
}
