package indi.etern.checkIn.action.records;

import indi.etern.checkIn.action.BaseAction;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.action.interfaces.ExecuteContext;
import indi.etern.checkIn.action.interfaces.InputData;
import indi.etern.checkIn.action.interfaces.OutputData;
import indi.etern.checkIn.entities.record.RequestRecord;
import indi.etern.checkIn.service.dao.RequestRecordService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

@Action("getRequestRecords")
public class GetRequestRecordsAction extends BaseAction<GetRequestRecordsAction.Input, GetRequestRecordsAction.SuccessOutput> {
    public record Input(String from, String to) implements InputData {}
    public record SuccessOutput(Map<LocalDate, Map<String, RequestRecord>> requestRecords) implements OutputData {
        @Override
        public Result result() {
            return Result.SUCCESS;
        }
    }
    
    private final RequestRecordService requestRecordService;
    
    public GetRequestRecordsAction(RequestRecordService requestRecordService) {
        this.requestRecordService = requestRecordService;
    }
    
    @Override
    public void execute(ExecuteContext<Input, SuccessOutput> context) {
        final Input input = context.getInput();
        LocalDate from = ZonedDateTime.parse(input.from)
                .withZoneSameInstant(ZoneId.systemDefault()).toLocalDate();
        LocalDate to = ZonedDateTime.parse(input.to)
                .withZoneSameInstant(ZoneId.systemDefault()).toLocalDate();
        context.requirePermission("get request records");
        final Set<RequestRecord> requestRecords = new HashSet<>(requestRecordService.findByLocalDateFromTo(from, to));
        LinkedHashMap<LocalDate, Map<String, RequestRecord>> map = new LinkedHashMap<>();
        requestRecords.stream().sorted(Comparator.comparing(RequestRecord::getTime).reversed()).forEach((trafficRecord) -> {
            final LocalDateTime submitTime = trafficRecord.getTime();
            Map<String, RequestRecord> map1 = map.computeIfAbsent(submitTime.toLocalDate(), k -> new LinkedHashMap<>());
            map1.put(trafficRecord.getId(),trafficRecord);
        });
        context.resolve(new SuccessOutput(map));
    }
}
