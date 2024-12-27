package indi.etern.checkIn.repositories;

import indi.etern.checkIn.entities.record.TrafficRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrafficRecordRepository extends JpaRepository<TrafficRecord,String> {
}
