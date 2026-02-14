package indi.etern.checkIn.service.exam;

import indi.etern.checkIn.api.webSocket.Message;
import indi.etern.checkIn.entities.linkUtils.impl.ToPartitionsLink;
import indi.etern.checkIn.entities.question.impl.Partition;
import indi.etern.checkIn.entities.question.impl.Question;
import indi.etern.checkIn.entities.question.impl.QuestionGroup;
import indi.etern.checkIn.entities.setting.SettingItem;
import indi.etern.checkIn.service.dao.GradingLevelService;
import indi.etern.checkIn.service.dao.PartitionService;
import indi.etern.checkIn.service.dao.QuestionService;
import indi.etern.checkIn.service.dao.SettingService;
import indi.etern.checkIn.service.exam.specialPartitionLimit.SpecialPartitionLimit;
import indi.etern.checkIn.service.exam.specialPartitionLimit.SpecialPartitionLimitService;
import indi.etern.checkIn.service.web.WebSocketService;
import indi.etern.checkIn.utils.TransactionTemplateUtil;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Aspect
public class StatusService {
    public static StatusService singletonInstance;
    private final GradingLevelService gradingLevelService;
    private final QuestionService questionService;
    private final PartitionService partitionService;
    private final SettingService settingService;
    private final SpecialPartitionLimitService specialPartitionLimitService;
    private final WebSocketService webSocketService;
    private ServerStatuses serverStatuses;

    public StatusService(GradingLevelService gradingLevelService, QuestionService questionService, PartitionService partitionService, SettingService settingService, SpecialPartitionLimitService specialPartitionLimitService, WebSocketService webSocketService) {
        this.gradingLevelService = gradingLevelService;
        this.questionService = questionService;
        this.partitionService = partitionService;
        this.settingService = settingService;
        this.specialPartitionLimitService = specialPartitionLimitService;
        singletonInstance = this;
        this.webSocketService = webSocketService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        flush();
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
            int questionAmount = Math.max(settingItem2.getValue(Integer.class), 1);

            List<Partition> requiredPartitions = null;
            int fullyAvailableQuestionsCount = 0;
            int leastRequiredPartitionsQuestionsCount = 0;
            try {
                SettingItem settingItem3 = settingService.getItem("generating", "requiredPartitions");
                //noinspection unchecked
                List<String> requiredPartitionIds = (List<String>) settingItem3.getValue(List.class);
                requiredPartitions = partitionService.findAllByIds(requiredPartitionIds);
                final Map<Partition, SpecialPartitionLimit> limits = specialPartitionLimitService.getAll();
                int requiredPartitionsQuestionsCount = 0;
                Set<Question> uniqueValues = new HashSet<>();
                for (Partition partition : requiredPartitions) {
                    Set<ToPartitionsLink> questionLinks = partition.getQuestionLinks();
                    int partitionQuestionsCount = 0;
                    final SpecialPartitionLimit limit = limits.get(partition);
                    leastRequiredPartitionsQuestionsCount += limit != null ? limit.getMinLimit() : 0;
                    for (ToPartitionsLink toPartitionsLink : questionLinks) {
                        Question source = toPartitionsLink.getSource();
                        if (source.isEnabled() &&
                                uniqueValues.add(source)) {
                            if (limit == null || limit.checkMax(partitionQuestionsCount + 1)) {
                                partitionQuestionsCount += source instanceof QuestionGroup questionGroup ? questionGroup.getQuestionLinks().size() : 1;
                            } else {
                                break;
                            }
                        }
                    }
                    requiredPartitionsQuestionsCount += partitionQuestionsCount;
                }
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
            if (requiredPartitions != null) {
                partitions.removeAll(requiredPartitions);
            }
            partitions.sort(Comparator.comparing(Partition::getEnabledQuestionCount));
            int countMin = 0;
            int countMax = 0;
            Set<Question> uniqueValues1 = new HashSet<>();
            Set<Question> uniqueValues2 = new HashSet<>();
            int minLimit = min;
            int maxLimit = max;
            for (int i = 0; i < partitions.size(); i++) {
                Partition least = partitions.get(i);
                Partition most = partitions.get(partitions.size() - 1 - i);
                Set<ToPartitionsLink> mostLinks = most.getQuestionLinks();
                Set<ToPartitionsLink> leastLinks = least.getQuestionLinks();
                for (ToPartitionsLink mostLink : mostLinks) {
                    Question source = mostLink.getSource();
                    if (source.isEnabled() && uniqueValues1.add(source) && maxLimit > 0) {
                        countMax++;
                    }
                }
                for (ToPartitionsLink leastLink : leastLinks) {
                    Question source = leastLink.getSource();
                    if (source.isEnabled() && uniqueValues2.add(source) && minLimit > 0) {
                        countMin++;
                    }
                }
                if (minLimit-- <= 0 && maxLimit-- <= 0) break;
            }
            if (leastRequiredPartitionsQuestionsCount > questionAmount) {
                generateAvailability = Status.UNAVAILABLE;
                type = "unachievableMinLimits";
                reason = "最低题目数量限制超过设定数量";
            } else if (fullyAvailableQuestionsCount + countMin >= questionAmount) {
                generateAvailability = Status.FULLY_AVAILABLE;
                type = "available";
            } else if (fullyAvailableQuestionsCount + countMax < questionAmount) {
                generateAvailability = Status.UNAVAILABLE;
                type = "questionsNotEnough";
                reason = "可用题目不足";
            } else {
                generateAvailability = Status.MAY_FAIL;
                type = "specificOptionsMayFail";
                reason = "在选择某些分区进行生成时可能失败";
            }
//            }

        } catch (NoSuchElementException e) {
            generateAvailability = Status.UNAVAILABLE;
            type = "questionAmountSettingMissing";
            reason = "试题生成题目数量设置缺失";
        }
        return new StatusInfo(generateAvailability, type, reason);
    }

    //    @AfterReturning("relationChanged()")
    public synchronized void flush() {
        TransactionTemplateUtil.getTransactionTemplate().executeWithoutResult((transactionStatus) -> {
            serverStatuses = new ServerStatuses(checkGenerateAvailability(), checkSubmitAvailability());
            webSocketService.sendMessageToAll(Message.of("updateServiceStatuses", serverStatuses));
        });
    }

    public ServerStatuses getStatus() {
        return serverStatuses;
    }

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