package indi.etern.checkIn.action.records;

import indi.etern.checkIn.action.MapResultAction;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.entities.exam.ExamData;
import indi.etern.checkIn.service.dao.ExamDataService;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Action("InvalidExam")
public class ManualInvalidExamAction extends MapResultAction {
    
    private String id;
    private final ExamDataService examDataService;
    
    public ManualInvalidExamAction(ExamDataService examDataService) {
        this.examDataService = examDataService;
    }
    
    @Override
    public String requiredPermissionName() {
        return "manual invalid exam";
    }
    
    @Override
    protected Optional<LinkedHashMap<String, Object>> doAction() throws Exception {
        Optional<ExamData> optionalExamData = examDataService.findById(id);
        optionalExamData.ifPresent(examData -> {
            examData.setStatus(ExamData.Status.MANUAL_INVALIDED);
            examDataService.save(examData);
            examData.sendUpdateExamRecord();
        });
        return Optional.empty();
    }
    
    @Override
    public void initData(Map<String, Object> initData) {
        id = initData.get("examId").toString();
    }
}
