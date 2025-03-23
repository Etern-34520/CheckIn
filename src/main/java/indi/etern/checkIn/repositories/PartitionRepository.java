package indi.etern.checkIn.repositories;

import indi.etern.checkIn.entities.question.impl.Partition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

@org.springframework.stereotype.Repository
public interface PartitionRepository extends JpaRepository<Partition,String> {
    boolean existsByName(String partitionName);
    Optional<Partition> findByName(String name);
}
