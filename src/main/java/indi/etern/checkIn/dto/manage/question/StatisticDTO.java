package indi.etern.checkIn.dto.manage.question;

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

    public StatisticDTO(QuestionStatistic questionStatistic) {
        drewCount = questionStatistic.getDrewCount();
        submittedCount = questionStatistic.getSubmittedCount();
        correctCount = questionStatistic.getCorrectCount();
        wrongCount = questionStatistic.getWrongCount();
    }
}
