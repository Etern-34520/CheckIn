package indi.etern.checkIn.action.records;

import indi.etern.checkIn.action.MapResultAction;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.entities.record.RequestRecord;
import indi.etern.checkIn.service.dao.RequestRecordService;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Action("getRequestRecordDetail")
public class GetRequestRecordDetailAction extends MapResultAction {
    private final RequestRecordService requestRecordService;
    private String id;
    
    public GetRequestRecordDetailAction(RequestRecordService requestRecordService) {
        this.requestRecordService = requestRecordService;
    }
    
    @Override
    public String requiredPermissionName() {
        return "";
    }
    
    @Override
    protected Optional<LinkedHashMap<String, Object>> doAction() throws Exception {
        LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        final RequestRecord requestRecord = requestRecordService.findById(id).orElseThrow(() -> new RuntimeException("RequestRecord not found"));
        final LinkedHashMap<String, Object> requestRecordDataMap = requestRecord.toDataMap();
        result.put("requestRecord", requestRecordDataMap);
        return Optional.of(result);
    }
    
    @Override
    public void initData(Map<String, Object> initData) {
        id = initData.get("id").toString();
    }
}
