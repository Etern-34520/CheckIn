package indi.etern.checkIn.repositories;

import indi.etern.checkIn.entities.record.TrafficRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface TrafficRecordRepository extends JpaRepository<TrafficRecord,String> {
    List<TrafficRecord> findAllByTimeBetween(LocalDateTime timeAfter, LocalDateTime timeBefore);
}
