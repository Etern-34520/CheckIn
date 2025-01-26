package indi.etern.checkIn.repositories;

import indi.etern.checkIn.entities.question.statistic.QuestionStatistic;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionStatisticRepository extends JpaRepository<QuestionStatistic,String> {
}