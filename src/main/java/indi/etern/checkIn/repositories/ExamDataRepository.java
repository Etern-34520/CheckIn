package indi.etern.checkIn.repositories;

import indi.etern.checkIn.entities.exam.ExamData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExamDataRepository extends JpaRepository<ExamData,String> {
}