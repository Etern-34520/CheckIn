package indi.etern.checkIn.action.records;

import indi.etern.checkIn.action.MapResultAction;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.entities.exam.ExamData;
import indi.etern.checkIn.service.dao.ExamDataService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

@Action("getExamRecords")
public class GetExamRecords extends MapResultAction {
    private LocalDate from;
    private LocalDate to;
    private final ExamDataService examDataService;
    
    public GetExamRecords(ExamDataService examDataService) {
        this.examDataService = examDataService;
    }
    
    @Override
    public String requiredPermissionName() {
        return "";
    }
    
    @Override
    protected Optional<LinkedHashMap<String, Object>> doAction() throws Exception {
        LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        final Set<ExamData> examData = new HashSet<>(examDataService.findAllBySubmitTimeBetween(from, to));
        examData.addAll(examDataService.findAllByGenerateTimeBetween(from, to));
        LinkedHashMap<LocalDate, Map<String,ExamData>> map = new LinkedHashMap<>();
        examData.stream().sorted(Comparator.comparing(ExamData::getGenerateTime).reversed()).forEach((examData1) -> {
            final LocalDateTime submitTime = examData1.getSubmitTime();
            final LocalDateTime generateTime = examData1.getGenerateTime();
            if (submitTime != null) {
                Map<String,ExamData> map1 = map.computeIfAbsent(submitTime.toLocalDate(), k -> new LinkedHashMap<>());
                map1.put(examData1.getId(),examData1);
            }
            Map<String,ExamData> map2 = map.computeIfAbsent(generateTime.toLocalDate(), k -> new LinkedHashMap<>());
            map2.put(examData1.getId(),examData1);
        });
        result.put("examRecords", map);
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
