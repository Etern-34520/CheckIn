package indi.etern.checkIn.action.trafficRecord;

import indi.etern.checkIn.action.TransactionalAction;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.entities.record.TrafficRecord;
import indi.etern.checkIn.service.dao.TrafficRecordService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Action("getTrafficRecordInRange")
public class getTrafficRecordInRangeAction extends TransactionalAction {
    LocalDate fromLocalDate;
    LocalDate toLocalDate;
    final TrafficRecordService trafficRecordService;
    
    public getTrafficRecordInRangeAction(TrafficRecordService trafficRecordService) {
        this.trafficRecordService = trafficRecordService;
    }
    
    @Override
    public String requiredPermissionName() {
        return "";
    }
    
    @Override
    protected Optional<LinkedHashMap<String, Object>> doAction() throws Exception {
        List<TrafficRecord> trafficRecords = trafficRecordService.findByLocalDateFromTo(fromLocalDate,toLocalDate);
        Map<LocalDate,List<TrafficRecord>> localDateListMap = new HashMap<>();
        trafficRecords.forEach((trafficRecord) -> {
            LocalDateTime localDateTime = trafficRecord.getTime();
            LocalDate localDate = localDateTime.toLocalDate();
            List<TrafficRecord> list = localDateListMap.computeIfAbsent(localDate, k -> new ArrayList<>());
            list.add(trafficRecord);
        });
        
        LinkedHashMap<String,Object> result = new LinkedHashMap<>();
        result.put("trafficRecords",localDateListMap);
        return Optional.of(result);
    }
    
    @Override
    public void initData(Map<String, Object> initData) {
        String fromTimestamp = initData.get("from").toString();
        String toTimestamp = initData.get("from").toString();
        fromLocalDate = LocalDate.parse(fromTimestamp);
        toLocalDate = LocalDate.parse(toTimestamp);
    }
}
