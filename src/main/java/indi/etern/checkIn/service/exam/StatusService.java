package indi.etern.checkIn.service.exam;

import indi.etern.checkIn.entities.linkUtils.impl.ToPartitionsLink;
import indi.etern.checkIn.entities.question.impl.Partition;
import indi.etern.checkIn.entities.question.impl.Question;
import indi.etern.checkIn.entities.setting.SettingItem;
import indi.etern.checkIn.service.dao.GradingLevelService;
import indi.etern.checkIn.service.dao.PartitionService;
import indi.etern.checkIn.service.dao.QuestionService;
import indi.etern.checkIn.service.dao.SettingService;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Aspect
public class StatusService {
    private final GradingLevelService gradingLevelService;
    private final QuestionService questionService;
    private final PartitionService partitionService;
    private final SettingService settingService;
    private ServerStatuses serverStatuses;
    
    @Transactional
    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        flush();
    }
    
    public StatusService(GradingLevelService gradingLevelService, QuestionService questionService, PartitionService partitionService, SettingService settingService) {
        this.gradingLevelService = gradingLevelService;
        this.questionService = questionService;
        this.partitionService = partitionService;
        this.settingService = settingService;
    }
    
    public StatusInfo checkSubmitAvailability() {
        Status submitAvailability;
        String type;
        String reason = null;
        if (gradingLevelService.findAll().isEmpty()) {
            submitAvailability = Status.UNAVAILABLE;
            type = "gradingLevelsMissing";
            reason = "无分数评级";
        } else {
            submitAvailability = Status.FULLY_AVAILABLE;
            type = "available";
        }
        return new StatusInfo(submitAvailability, type, reason);
    }
    
    public StatusInfo checkGenerateAvailability() {
        Status generateAvailability;
        String type;
        String reason = null;
        try {
            SettingItem settingItem2 = settingService.getItem("generating", "questionAmount");
            int questionAmount = Math.min(settingItem2.getValue(Integer.class), 1);
            
            if (questionService.countEnabled() < questionAmount) {
                generateAvailability = Status.UNAVAILABLE;
                type = "questionsNotEnough";
                reason = "可用题目不足";
            } else {
                int fullyAvailableQuestionsCount = 0;
                try {
                    SettingItem settingItem3 = settingService.getItem("generating", "requiredPartitions");
                    //noinspection unchecked
                    List<String> requiredPartitionIds = (List<String>) settingItem3.getValue(List.class);
                    final List<Partition> partitions = partitionService.findAllByIds(requiredPartitionIds);
                    final int requiredPartitionsQuestionsCount = Math.toIntExact(
                            partitions.stream()
                                    .map(Partition::getQuestionLinks).flatMap(Collection::stream)
                                    .map(ToPartitionsLink::getSource).filter(Question::isEnabled).distinct().count());
                    fullyAvailableQuestionsCount += requiredPartitionsQuestionsCount;
                } catch (NoSuchElementException ignored) {
                }
                
                int min = 0;
                int max;
                try {
                    SettingItem item = settingService.getItem("generating", "partitionRange");
                    final ArrayList<?> value = item.getValue(ArrayList.class);
                    if (value.size() == 2) {
                        min = (int) value.getFirst();
                        max = (int) value.getLast();
                    } else {
                        max = partitionService.count();
                    }
                } catch (NoSuchElementException ignored) {
                    max = partitionService.count();
                }
                
                final List<Partition> partitions = partitionService.findAll();
                partitions.sort(Comparator.comparing(Partition::getEnabledQuestionCount));
                int countMin = 0;
                int countMax = 0;
                Set<Question> uniqueValues = new HashSet<>();
                int limit1 = min;
                int limit2 = max;
                for (Partition partition : partitions) {
                    Set<ToPartitionsLink> questionLinks = partition.getQuestionLinks();
                    for (ToPartitionsLink toPartitionsLink : questionLinks) {
                        Question source = toPartitionsLink.getSource();
                        if (source.isEnabled()) {
                            if (uniqueValues.add(source)) {
                                if (limit1 > 0) {
                                    countMin++;
                                }
                                if (limit2 > 0) {
                                    countMax++;
                                }
                            }
                        }
                    }
                    if (limit1-- <= 0 && limit2-- <= 0) break;
                }
                if (fullyAvailableQuestionsCount + countMin >= questionAmount) {
                    generateAvailability = Status.FULLY_AVAILABLE;
                    type = "available";
                } else if (fullyAvailableQuestionsCount + countMax < questionAmount) {
                    generateAvailability = Status.UNAVAILABLE;
                    type = "questionsNotEnough";
                    reason = "可用题目不足";
                } else {
                    generateAvailability = Status.MAY_FAIL;
                    type = "questionsMayNotBeEnough";
                    reason = "可用题目在选择某些分区进行生成时可能不足";
                }
            }
            
        } catch (NoSuchElementException e) {
            generateAvailability = Status.UNAVAILABLE;
            type = "questionAmountSettingMissing";
            reason = "试题生成题目数量设置缺失";
        }
        return new StatusInfo(generateAvailability, type, reason);
    }
    
//    @AfterReturning("relationChanged()")
    @Transactional(readOnly = true)
    public synchronized void flush() {
        serverStatuses = new ServerStatuses(checkGenerateAvailability(), checkSubmitAvailability());
    }
    
    public ServerStatuses getStatus() {
        return serverStatuses;
    }
    
/*
    @Pointcut("execution(* indi.etern.checkIn.service.dao.GradingLevelService.delete*(..)) || " +
            "execution(* indi.etern.checkIn.service.dao.GradingLevelService.save*(..)) || " +
            "execution(* indi.etern.checkIn.service.dao.QuestionService.save*(..)) || " +
            "execution(* indi.etern.checkIn.service.dao.QuestionService.delete(..)) || " +
            "execution(* indi.etern.checkIn.service.dao.PartitionService.save(..)) || " +
            "execution(* indi.etern.checkIn.service.dao.PartitionService.delete(..)) || " +
            "execution(* indi.etern.checkIn.service.dao.SettingService.set*(..))")
    public void relationChanged() {}
*/
    
    public enum Status {
        FULLY_AVAILABLE,
        MAY_FAIL,
        UNAVAILABLE,
    }
    
    public record StatusInfo(Status status, String type, String reason) {
    }
    
    public record ServerStatuses(StatusInfo generateAvailability, StatusInfo submitAvailability) {
    }
}