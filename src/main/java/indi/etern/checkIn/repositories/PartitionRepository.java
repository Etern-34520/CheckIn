package indi.etern.checkIn.repositories;

import indi.etern.checkIn.entities.question.interfaces.Partition;
import org.springframework.data.jpa.repository.JpaRepository;
@org.springframework.stereotype.Repository
public interface PartitionRepository extends JpaRepository<Partition,String> {
}
