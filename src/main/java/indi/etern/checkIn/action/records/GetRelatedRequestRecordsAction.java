package indi.etern.checkIn.action.records;

import indi.etern.checkIn.action.MapResultAction;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.entities.record.RequestRecord;
import indi.etern.checkIn.service.dao.ExamDataService;
import indi.etern.checkIn.service.dao.RequestRecordService;

import java.util.*;

@Action("getRelatedRequestOfExamData")
public class GetRelatedRequestRecordsAction extends MapResultAction {
    
    private final ExamDataService examDataService;
    private final RequestRecordService requestRecordService;
    private String examDataId;
    
    public GetRelatedRequestRecordsAction(ExamDataService examDataService, RequestRecordService requestRecordService) {
        this.examDataService = examDataService;
        this.requestRecordService = requestRecordService;
    }
    
    @Override
    public String requiredPermissionName() {
        return "get request records";
    }
    
    @Override
    protected Optional<LinkedHashMap<String, Object>> doAction() throws Exception {
        LinkedHashMap<String, Object> result = getSuccessMap();
        final List<RequestRecord> requestRecords = requestRecordService.findAllByExamDataId(examDataId);
        List<RequestRecord> orderedRequestRecords = requestRecords.stream().sorted(Comparator.comparing(RequestRecord::getTime).reversed()).toList();
        result.put("requestRecords", orderedRequestRecords);
        return Optional.of(result);
    }
    
    @Override
    public void initData(Map<String, Object> initData) {
        examDataId = String.valueOf(initData.get("examDataId"));
    }
}
