package indi.etern.checkIn.service.dao;

import indi.etern.checkIn.entities.exam.ExamData;
import indi.etern.checkIn.entities.question.impl.Question;
import indi.etern.checkIn.entities.question.interfaces.answer.Answer;
import indi.etern.checkIn.entities.question.statistic.QuestionStatistic;
import indi.etern.checkIn.repositories.QuestionStatisticRepository;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Service
public class QuestionStatisticService {
    public static QuestionStatisticService singletonInstance;
    private final QuestionService questionService;
    @Resource
    private QuestionStatisticRepository questionStatisticRepository;
    
    public QuestionStatisticService(QuestionService questionService) {
        singletonInstance = this;
        this.questionService = questionService;
    }
    
    public void appendStatistic(ExamData examData) {
        switch (examData.getStatus()) {
            case ONGOING -> handleStatistics(examData, (questionStatistic) -> {
                final List<ExamData> examDataList = questionStatistic.getDrewExamData();
                boolean alreadyContains = examDataList.contains(examData);
                if (!alreadyContains) {
                    questionStatistic.increaseDrewCount();
                    examDataList.add(examData);
                }
            });
            case SUBMITTED -> {
                handleStatistics(examData, (questionStatistic) -> {
                    final int index = examData.getQuestionIds().indexOf(questionStatistic.getId());
                    final Answer<?,?> answer = examData.getAnswers().get(index);
                    final Answer.CheckedResultType checkedResultType = answer.check().checkedResultType();
                    switch (checkedResultType) {
                        case CORRECT -> questionStatistic.increaseCorrectCount();
                        case HALF_CORRECT -> {
                            //TODO
                        }
                        case WRONG -> questionStatistic.increaseWrongCount();
                        case null, default -> {
                            throw new IllegalStateException();
                        }
                    }
                });
            }
            case MANUAL_INVALIDED -> {
                handleStatistics(examData, questionStatistic -> {
                });
            }
        }
    }
    
    private void handleStatistics(ExamData examData, Consumer<QuestionStatistic> consumer) {
        List<Question> questions = questionService.findAllById(examData.getQuestionIds());
        for (Question question : questions) {
            QuestionStatistic statistic = questionStatisticRepository.findById(question.getId()).orElse(null);
            if (statistic == null) {
                statistic = QuestionStatistic.builder()
                        .id(question.getId())
                        .question(question)
                        .drewExamData(new ArrayList<>())
                        .build();
            }
            consumer.accept(statistic);
            questionStatisticRepository.save(statistic);
        }
    }
}
