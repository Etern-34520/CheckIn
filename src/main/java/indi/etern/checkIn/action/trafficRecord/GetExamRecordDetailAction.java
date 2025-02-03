package indi.etern.checkIn.action.trafficRecord;

import indi.etern.checkIn.action.MapResultAction;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.entities.exam.ExamData;
import indi.etern.checkIn.service.dao.ExamDataService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
        LinkedHashMap<String, Object> examDataMap = new LinkedHashMap<>();
        examDataMap.put("id", examData.getId());
        examDataMap.put("qqNumber", examData.getQqNumber());
        examDataMap.put("status", examData.getStatus());
        examDataMap.put("generateTime", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(examData.getGenerateTime()));
        final LocalDateTime submitTime = examData.getSubmitTime();
        if (submitTime != null)
            examDataMap.put("submitTime", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(submitTime));
        examDataMap.put("requiredPartitionIds", examData.getRequiredPartitionIds());
        examDataMap.put("selectedPartitionIds", examData.getSelectedPartitionIds());
        examDataMap.put("questionAmount", examData.getQuestionAmount());
        examDataMap.put("questionIds", examData.getQuestionIds());
        examDataMap.put("result", examData.getExamResult());
        examDataMap.put("answers", examData.getAnswersMap());
        result.put("examData", examDataMap);
        return Optional.of(result);
    }
    
    @Override
    public void initData(Map<String, Object> initData) {
        id = initData.get("id").toString();
    }
}
