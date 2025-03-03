package indi.etern.checkIn.action.records;

import indi.etern.checkIn.action.TransactionalAction;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.auth.JwtTokenProvider;
import indi.etern.checkIn.entities.exam.ExamData;
import indi.etern.checkIn.entities.user.User;
import indi.etern.checkIn.service.dao.ExamDataService;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Action("getExamRecordDetail")
public class GetExamRecordDetailAction extends TransactionalAction {
    private final ExamDataService examDataService;
    private final JwtTokenProvider jwtTokenProvider;
    private ExamData examData;
    
    public GetExamRecordDetailAction(ExamDataService examDataService, JwtTokenProvider jwtTokenProvider) {
        this.examDataService = examDataService;
        this.jwtTokenProvider = jwtTokenProvider;
    }
    
    @Override
    public String requiredPermissionName() {
        if (examData.getQqNumber() == getCurrentUser().getQQNumber())
            return null;
        else
            return "get exam data";
    }
    
    @Override
    protected Optional<LinkedHashMap<String, Object>> doAction() throws Exception {
        LinkedHashMap<String, Object> result = getSuccessMap();
        final User currentUser = getCurrentUser();
        final boolean accessibleToOthersSubmissions = jwtTokenProvider.isUserHasPermission(currentUser,"get exam submission data");
        final LinkedHashMap<String, Object> examDataMap = examData.toDataMap();
        if (currentUser.getQQNumber() != examData.getQqNumber() && !accessibleToOthersSubmissions) {
            examDataMap.remove("answers");
        }
        result.put("examData", examDataMap);
        return Optional.of(result);
    }
    
    @Override
    public void initData(Map<String, Object> initData) {
        String id = initData.get("id").toString();
        examData = examDataService.findById(id).orElseThrow(() -> new RuntimeException("ExamData not found"));
    }
}
