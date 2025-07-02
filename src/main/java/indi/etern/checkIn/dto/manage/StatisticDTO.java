package indi.etern.checkIn.dto.manage;

import indi.etern.checkIn.entities.question.statistic.QuestionStatistic;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class StatisticDTO {
    int drewCount;
    int submittedCount;
    int correctCount;
    int wrongCount;
    int examDataCount;
    
    public StatisticDTO(QuestionStatistic questionStatistic) {
        drewCount = questionStatistic.getDrewCount();
        submittedCount = questionStatistic.getSubmittedCount();
        correctCount = questionStatistic.getCorrectCount();
        wrongCount = questionStatistic.getWrongCount();
        examDataCount = questionStatistic.getDrewExamData().size();
    }
}
