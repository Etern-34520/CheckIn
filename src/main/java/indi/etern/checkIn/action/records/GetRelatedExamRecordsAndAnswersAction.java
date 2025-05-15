package indi.etern.checkIn.action.records;

import indi.etern.checkIn.action.BaseAction;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.action.interfaces.ExecuteContext;
import indi.etern.checkIn.action.interfaces.InputData;
import indi.etern.checkIn.action.interfaces.OutputData;
import indi.etern.checkIn.entities.exam.ExamData;
import indi.etern.checkIn.service.dao.ExamDataService;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Action("getRelatedExamRecordsAndAnswers")
public class GetRelatedExamRecordsAndAnswersAction extends BaseAction<GetRelatedExamRecordsAndAnswersAction.Input, GetRelatedExamRecordsAndAnswersAction.SuccessOutput> {
    public record Input(String questionId) implements InputData {}
    public record SuccessOutput(List<Map<String,Object>> examDataAnswerList) implements OutputData {
        @Override
        public Result result() {
            return Result.SUCCESS;
        }
    }
    
    private final ExamDataService examDataService;
    
    public GetRelatedExamRecordsAndAnswersAction(ExamDataService examDataService) {
        this.examDataService = examDataService;
    }
    
    @Override
    public void execute(ExecuteContext<Input, SuccessOutput> context) {
        String questionId = context.getInput().questionId;
        List<Map<String,Object>> examDataMapList = examDataService.getExamDataContainsQuestionById(questionId)
                .stream().sorted(Comparator.comparing(ExamData::getGenerateTime).reversed())
                .map(examData -> {
                    Map<String,Object> map = new LinkedHashMap<>();
                    map.put("examData",examData);
                    final var answersMap = examData.getAnswersMap();
                    if (answersMap != null) {
                        map.put("answer", answersMap.get(questionId));
                    }
                    return map;
                }).toList();
        context.resolve(new SuccessOutput(examDataMapList));
    }
}
