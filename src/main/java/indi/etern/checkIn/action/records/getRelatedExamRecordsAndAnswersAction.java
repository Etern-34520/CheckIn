package indi.etern.checkIn.action.records;

import indi.etern.checkIn.action.MapResultAction;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.auth.JwtTokenProvider;
import indi.etern.checkIn.entities.exam.ExamData;
import indi.etern.checkIn.entities.user.User;
import indi.etern.checkIn.service.dao.ExamDataService;

import java.util.*;

@Action("get related exam records and answers")
public class getRelatedExamRecordsAndAnswersAction extends MapResultAction {
    
    private final ExamDataService examDataService;
    private final JwtTokenProvider jwtTokenProvider;
    private String questionId;
    
    public getRelatedExamRecordsAndAnswersAction(ExamDataService examDataService, JwtTokenProvider jwtTokenProvider) {
        this.examDataService = examDataService;
        this.jwtTokenProvider = jwtTokenProvider;
    }
    
    @Override
    public String requiredPermissionName() {
        return null;
    }
    
    @Override
    protected Optional<LinkedHashMap<String, Object>> doAction() throws Exception {
        final User currentUser = getCurrentUser();
        final boolean accessibleToOthersSubmissions = jwtTokenProvider.isUserHasPermission(currentUser, "get exam submission data");
        List<Map<String,Object>> examDataMapList = examDataService.getExamDataContainsQuestionById(questionId)
                .stream().sorted(Comparator.comparing(ExamData::getGenerateTime).reversed())
                .map(examData -> {
                    Map<String,Object> map = new LinkedHashMap<>();
                    map.put("examData",examData);
                    map.put("answer",examData.getAnswersMap().get(questionId));
                    return map;
                }).toList();
        final LinkedHashMap<String, Object> result = getSuccessMap();
        result.put("examDataAnswerList", examDataMapList);
        return Optional.of(result);
    }
    
    @Override
    public void initData(Map<String, Object> initData) {
        questionId = initData.get("questionId").toString();
    }
}
