package indi.etern.checkIn.action.records;

import indi.etern.checkIn.action.MapResultAction;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.entities.record.RequestRecord;
import indi.etern.checkIn.service.dao.RequestRecordService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

@Action("getRequestRecords")
public class GetRequestRecords extends MapResultAction {
    
    private final RequestRecordService requestRecordService;
    private LocalDate from;
    private LocalDate to;
    
    public GetRequestRecords(RequestRecordService requestRecordService) {
        this.requestRecordService = requestRecordService;
    }
    
    @Override
    public String requiredPermissionName() {
        return "";
    }
    
    @Override
    protected Optional<LinkedHashMap<String, Object>> doAction() throws Exception {
        LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        final Set<RequestRecord> requestRecords = new HashSet<>(requestRecordService.findByLocalDateFromTo(from, to));
        LinkedHashMap<LocalDate, Map<String, RequestRecord>> map = new LinkedHashMap<>();
        requestRecords.stream().sorted(Comparator.comparing(RequestRecord::getTime).reversed()).forEach((trafficRecord) -> {
            final LocalDateTime submitTime = trafficRecord.getTime();
            Map<String, RequestRecord> map1 = map.computeIfAbsent(submitTime.toLocalDate(), k -> new LinkedHashMap<>());
            map1.put(trafficRecord.getId(),trafficRecord);
        });
        result.put("requestRecords", map);
        return Optional.of(result);
    }
    
    @Override
    public void initData(Map<String, Object> initData) {
        String fromString = initData.get("from").toString();
        from = ZonedDateTime.parse(fromString).withZoneSameInstant(ZoneId.systemDefault()).toLocalDate();
        String toString = initData.get("to").toString();
        to = ZonedDateTime.parse(toString).withZoneSameInstant(ZoneId.systemDefault()).toLocalDate();
    }
}
