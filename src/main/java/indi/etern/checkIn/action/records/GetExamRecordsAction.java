package indi.etern.checkIn.action.records;

import indi.etern.checkIn.action.MapResultAction;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.auth.JwtTokenProvider;
import indi.etern.checkIn.entities.exam.ExamData;
import indi.etern.checkIn.entities.user.User;
import indi.etern.checkIn.service.dao.ExamDataService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

@Action("getExamRecords")
public class GetExamRecordsAction extends MapResultAction {
    private final JwtTokenProvider jwtTokenProvider;
    private LocalDate from;
    private LocalDate to;
    private final ExamDataService examDataService;
    
    public GetExamRecordsAction(ExamDataService examDataService, JwtTokenProvider jwtTokenProvider) {
        this.examDataService = examDataService;
        this.jwtTokenProvider = jwtTokenProvider;
    }
    
    @Override
    public String requiredPermissionName() {
        return null;
    }
    
    @Override
    protected Optional<LinkedHashMap<String, Object>> doAction() throws Exception {
        LinkedHashMap<String, Object> result = getSuccessMap();
        final Set<ExamData> examDataSet = new HashSet<>(examDataService.findAllBySubmitTimeBetween(from, to));
        examDataSet.addAll(examDataService.findAllByGenerateTimeBetween(from, to));
        LinkedHashMap<LocalDate, Map<String,ExamData>> map = new LinkedHashMap<>();
        final User currentUser = getCurrentUser();
        final boolean accessibleToAll = jwtTokenProvider.isUserHasPermission(currentUser,"get exam data");
        examDataSet.stream().sorted(Comparator.comparing(ExamData::getGenerateTime).reversed()).forEach((examData) -> {
            final LocalDateTime submitTime = examData.getSubmitTime();
            final LocalDateTime generateTime = examData.getGenerateTime();
            if (accessibleToAll || examData.getQqNumber() == currentUser.getQQNumber()) {
                if (submitTime != null) {
                    Map<String,ExamData> map1 = map.computeIfAbsent(submitTime.toLocalDate(), k -> new LinkedHashMap<>());
                    map1.put(examData.getId(),examData);
                }
                Map<String,ExamData> map2 = map.computeIfAbsent(generateTime.toLocalDate(), k -> new LinkedHashMap<>());
                map2.put(examData.getId(),examData);
            }
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
