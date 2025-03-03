package indi.etern.checkIn.action.records;

import indi.etern.checkIn.action.MapResultAction;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.service.dao.ExamDataService;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Action("getExamRecordsByQQ")
public class GetExamRecordsByQqAction extends MapResultAction {
    private final ExamDataService examDataService;
    private long qq;
    
    public GetExamRecordsByQqAction(ExamDataService examDataService) {
        this.examDataService = examDataService;
    }
    
    @Override
    public String requiredPermissionName() {
        if (qq == getCurrentUser().getQQNumber()) return null;
        else return "get exam data";
    }
    
    @Override
    protected Optional<LinkedHashMap<String, Object>> doAction() throws Exception {
        LinkedHashMap<String, Object> result = getSuccessMap();
        result.put("examRecords", examDataService.findAllByQQ(qq));
        return Optional.of(result);
    }
    
    @Override
    public void initData(Map<String, Object> initData) {
        qq = ((Number) initData.get("qq")).longValue();
    }
}
