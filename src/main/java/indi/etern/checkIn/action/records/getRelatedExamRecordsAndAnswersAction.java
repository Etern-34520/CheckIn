package indi.etern.checkIn.action.records;

import indi.etern.checkIn.action.BaseAction1;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.action.interfaces.ExecuteContext;
import indi.etern.checkIn.action.interfaces.InputData;
import indi.etern.checkIn.action.interfaces.OutputData;
import indi.etern.checkIn.auth.JwtTokenProvider;
import indi.etern.checkIn.entities.exam.ExamData;
import indi.etern.checkIn.service.dao.ExamDataService;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Action("getRelatedExamRecordsAndAnswers")
public class getRelatedExamRecordsAndAnswersAction extends BaseAction1<getRelatedExamRecordsAndAnswersAction.Input, getRelatedExamRecordsAndAnswersAction.SuccessOutput> {
    public record Input(String questionId) implements InputData {}
    public record SuccessOutput(List<Map<String,Object>> examDataAnswerList) implements OutputData {
        @Override
        public Result result() {
            return Result.SUCCESS;
        }
    }
    
    private final ExamDataService examDataService;
    private final JwtTokenProvider jwtTokenProvider;
    
    public getRelatedExamRecordsAndAnswersAction(ExamDataService examDataService, JwtTokenProvider jwtTokenProvider) {
        this.examDataService = examDataService;
        this.jwtTokenProvider = jwtTokenProvider;
    }
    
    @Override
    public void execute(ExecuteContext<Input, SuccessOutput> context) {
        String questionId = context.getInput().questionId;
        List<Map<String,Object>> examDataMapList = examDataService.getExamDataContainsQuestionById(questionId)
                .stream().sorted(Comparator.comparing(ExamData::getGenerateTime).reversed())
                .map(examData -> {
                    Map<String,Object> map = new LinkedHashMap<>();
                    map.put("examData",examData);
                    map.put("answer",examData.getAnswersMap().get(questionId));
                    return map;
                }).toList();
        context.resolve(new SuccessOutput(examDataMapList));
    }
}
