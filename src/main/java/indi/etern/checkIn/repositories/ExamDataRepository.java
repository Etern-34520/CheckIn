package indi.etern.checkIn.repositories;

import indi.etern.checkIn.entities.exam.ExamData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ExamDataRepository extends JpaRepository<ExamData,String> {
    List<ExamData> findAllBySubmitTimeBetween(LocalDateTime from, LocalDateTime to);
    List<ExamData> findAllByGenerateTimeBetween(LocalDateTime localDateTime, LocalDateTime localDateTime1);
    List<ExamData> findAllByQqNumberIs(long qqNumber);
    List<ExamData> findAllByQqNumberAndStatus(long qqNumber, ExamData.Status status);
    
    List<ExamData> findAllByQuestionIdsContains(String questionIds);
}