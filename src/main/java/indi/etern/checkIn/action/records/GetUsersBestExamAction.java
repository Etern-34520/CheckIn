package indi.etern.checkIn.action.records;

import indi.etern.checkIn.action.TransactionalAction;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.entities.exam.ExamData;
import indi.etern.checkIn.entities.user.User;
import indi.etern.checkIn.service.dao.ExamDataService;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Optional;

@Action("getUsersLatestExamRecord")
public class GetUsersBestExamAction extends TransactionalAction {
    private final ExamDataService examDataService;
    
    public GetUsersBestExamAction(ExamDataService examDataService) {
        this.examDataService = examDataService;
    }
    
    @Override
    public String requiredPermissionName() {
        return null;
    }
    
    @Override
    protected Optional<LinkedHashMap<String, Object>> doAction() throws Exception {
        final User currentUser = getCurrentUser();
        Optional<ExamData> optionalExamData = examDataService.findAllByQQ(currentUser.getQQNumber())
                .stream()
                .max(Comparator.comparing(examData -> examData));
        LinkedHashMap<String, Object> result = getSuccessMap();
        if (optionalExamData.isPresent()) {
            final LinkedHashMap<String, Object> examDataMap = optionalExamData.get().toDataMap();
            examDataMap.remove("answers");
            result.put("examData", examDataMap);
            return Optional.of(result);
        } else {
            result.put("examData", "not found");
            return Optional.of(result);
        }
    }
}
