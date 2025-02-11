package indi.etern.checkIn.action.records;

import indi.etern.checkIn.action.MapResultAction;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.entities.exam.ExamData;
import indi.etern.checkIn.service.dao.ExamDataService;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Action("getExamRecordDetail")
public class GetExamRecordDetailAction extends MapResultAction {
    private final ExamDataService examDataService;
    private String id;
    
    public GetExamRecordDetailAction(ExamDataService examDataService) {
        this.examDataService = examDataService;
    }
    
    @Override
    public String requiredPermissionName() {
        return "";
    }
    
    @Override
    protected Optional<LinkedHashMap<String, Object>> doAction() throws Exception {
        LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        final ExamData examData = examDataService.findById(id).orElseThrow(() -> new RuntimeException("ExamData not found"));
        final LinkedHashMap<String, Object> examDataMap = examData.toDataMap();
        result.put("examData", examDataMap);
        return Optional.of(result);
    }
    
    @Override
    public void initData(Map<String, Object> initData) {
        id = initData.get("id").toString();
    }
}
