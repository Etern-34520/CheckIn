package indi.etern.checkIn.action.records;

import indi.etern.checkIn.action.BaseAction1;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.action.interfaces.ExecuteContext;
import indi.etern.checkIn.action.interfaces.InputData;
import indi.etern.checkIn.action.interfaces.OutputData;
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
public class GetExamRecordsAction extends BaseAction1<GetExamRecordsAction.Input, GetExamRecordsAction.SuccessOutput> {
    public record Input(String from, String to) implements InputData {}
    public record SuccessOutput(Map<LocalDate, Map<String,ExamData>> examData) implements OutputData {
        @Override
        public Result result() {
            return Result.SUCCESS;
        }
    }
    private final JwtTokenProvider jwtTokenProvider;
    private LocalDate from;
    private LocalDate to;
    private final ExamDataService examDataService;
    
    public GetExamRecordsAction(ExamDataService examDataService, JwtTokenProvider jwtTokenProvider) {
        this.examDataService = examDataService;
        this.jwtTokenProvider = jwtTokenProvider;
    }
    
    @Override
    public void execute(ExecuteContext<Input, SuccessOutput> context) {
        final Input input = context.getInput();
        final LocalDate fromDate = ZonedDateTime.parse(input.from).withZoneSameInstant(ZoneId.systemDefault()).toLocalDate();
        final LocalDate toDate = ZonedDateTime.parse(input.to).withZoneSameInstant(ZoneId.systemDefault()).toLocalDate();
        final Set<ExamData> examDataSet = new HashSet<>(examDataService.findAllBySubmitTimeBetween(from, to));
        examDataSet.addAll(examDataService.findAllByGenerateTimeBetween(from, to));
        LinkedHashMap<LocalDate, Map<String,ExamData>> map = new LinkedHashMap<>();
        final User currentUser = context.getCurrentUser();
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
        SuccessOutput output = new SuccessOutput(map);
        context.resolve(output);
    }
}