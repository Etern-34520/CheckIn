package indi.etern.checkIn.service.exam;

import indi.etern.checkIn.entities.exam.ExamData;
import indi.etern.checkIn.entities.question.impl.Partition;
import indi.etern.checkIn.entities.question.impl.Question;
import indi.etern.checkIn.entities.question.impl.group.QuestionGroup;
import indi.etern.checkIn.entities.setting.SettingItem;
import indi.etern.checkIn.service.dao.ExamDataService;
import indi.etern.checkIn.service.dao.PartitionService;
import indi.etern.checkIn.service.dao.SettingService;
import indi.etern.checkIn.service.exam.specialPartitionLimit.SpecialPartitionLimit;
import indi.etern.checkIn.service.exam.specialPartitionLimit.SpecialPartitionLimitService;
import indi.etern.checkIn.service.exam.throwable.MinQuestionLimitOutOfBoundsException;
import indi.etern.checkIn.service.exam.throwable.NotEnoughQuestionsForExamException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class ExamGenerator {
    private final PartitionService partitionService;
    private final SettingService settingService;
    private final ExamDataService examDataService;
    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    public ExamGenerator(PartitionService partitionService, SettingService settingService, ExamDataService examDataService) {
        this.partitionService = partitionService;
        this.settingService = settingService;
        this.examDataService = examDataService;
    }
    
    private LinkedHashSet<Question> drawQuestions(List<Partition> partitions, Random random, DrawingStrategy drawingStrategy, CompletingStrategy completingStrategy, int questionAmount) throws NotEnoughQuestionsForExamException, MinQuestionLimitOutOfBoundsException {
        List<Question> allEnabledQuestions = new ArrayList<>();
        partitions.forEach(partition -> allEnabledQuestions.addAll(partition.getEnabledQuestions()));//避免重复计算
        AtomicInteger allEnabledQuestionsCount = new AtomicInteger();
        allEnabledQuestions.forEach(question -> {
            if (question instanceof QuestionGroup questionGroup) {
                allEnabledQuestionsCount.addAndGet(questionGroup.getQuestionLinks().size());
            } else {
                allEnabledQuestionsCount.getAndIncrement();
            }
        });
        List<Question> allActivePartitionEnabledQuestions = new ArrayList<>(partitions.stream().map(Partition::getEnabledQuestions).flatMap(Collection::stream).toList());
        
        if (logger.isDebugEnabled())
            logger.debug("all enabled questions[{}]: {}", allEnabledQuestionsCount, allActivePartitionEnabledQuestions.stream().map(Question::getId).toList());
        
        LinkedHashSet<Question> drewQuestions = new LinkedHashSet<>();
        
        logger.debug("draw questions({}) for partitions({})", questionAmount, partitions);
        try {
            return getQuestions(partitions, random, drawingStrategy, questionAmount, drewQuestions);
        } catch (NotEnoughQuestionsForExamException e) {
            List<Partition> completingPartitions = completingStrategy.getCompletingPartitions();
            logger.debug("completing partitions: {}", completingPartitions);
            
//            int completingCount = questionAmount - QuestionRealCountCounter.count(drewQuestions);
            logger.debug("draw questions({}) for completing partitions({})", questionAmount, completingPartitions);
            getQuestions(completingPartitions, random, drawingStrategy, questionAmount, drewQuestions);
            return drewQuestions;
        }
    }
    
    private LinkedHashSet<Question> getQuestions(List<Partition> partitions, Random random, DrawingStrategy drawingStrategy, int questionAmount, LinkedHashSet<Question> drewQuestions) throws NotEnoughQuestionsForExamException, MinQuestionLimitOutOfBoundsException {
        Map<Partition, SpecialPartitionLimit> specialPartitionLimitMap = SpecialPartitionLimitService.singletonInstance.getLimits(partitions);
        Map<Partition, PartitionQuestionDrawer> drawersMap = new HashMap<>();
        partitions.forEach((partition) -> {
            final PartitionQuestionDrawer drawer = new PartitionQuestionDrawer(partition, random);
            drawer.setSpecialPartitionLimit(specialPartitionLimitMap.get(partition));
            drawersMap.put(partition, drawer);
        });
        drawersMap.values().forEach(drawer -> {
            drawer.invalidIdentical(drewQuestions);
            drewQuestions.addAll(drawer.initLeastQuestions());
        });
        
//        questionAmount = questionAmount - QuestionRealCountCounter.count(drewQuestions);
        if (questionAmount < 0) {
            throw new MinQuestionLimitOutOfBoundsException();
        } else if (questionAmount == 0) {
            logger.debug("no need to draw more questions");
            return drewQuestions;
        } else {
            drawingStrategy.drawQuestions(drewQuestions, drawersMap, random, questionAmount);
        }
        return drewQuestions;
    }
    
    public ExamData generateExam(long qq, List<Partition> selectedPartitions) throws NotEnoughQuestionsForExamException, MinQuestionLimitOutOfBoundsException {
        SettingItem settingItem = settingService.getItem("drawing", "drawingStrategy");
        String drawingStrategyName = settingItem.getValue(String.class);
        DrawingStrategy drawingStrategy = DrawingStrategy.valueOf(drawingStrategyName.toUpperCase());
        logger.debug("use drawing strategy: {}", drawingStrategy);
        
        SettingItem settingItem1 = settingService.getItem("drawing", "completingStrategy");
        String completingStrategyName = settingItem1.getValue(String.class);
        CompletingStrategy completingStrategy = CompletingStrategy.valueOf(completingStrategyName.toUpperCase());
        logger.debug("use completing strategy: {}", completingStrategy);
        
        SettingItem settingItem2 = settingService.getItem("drawing", "questionAmount");
        int questionAmount = settingItem2.getValue(Integer.class);
        logger.debug("question amount: {}", questionAmount);
        
        SettingItem settingItem3 = settingService.getItem("drawing", "requiredPartitions");
        //noinspection unchecked
        List<Integer> requiredPartitionIds = (List<Integer>) settingItem3.getValue(List.class);
        List<Integer> selectedPartitionIds = selectedPartitions.stream().map(Partition::getId).toList();
        List<Partition> partitions = new ArrayList<>(requiredPartitionIds.size() + selectedPartitionIds.size());
        partitions.addAll(selectedPartitions);
        partitions.addAll(partitionService.findAllByIds(requiredPartitionIds));
        logger.debug("required partitions: {}", partitionService.findAllByIds(requiredPartitionIds));
        logger.debug("selected partitions: {}", partitionService.findAllByIds(selectedPartitionIds));
        
        ExamData examData = ExamData.builder()
                .id(UUID.randomUUID().toString())
                .qqNumber(qq)
                .status(ExamData.Status.DURING)
                .questionIds(drawQuestions(partitions, new Random(), drawingStrategy, completingStrategy, questionAmount).stream().map(Question::getId).toList())
                .selectedPartitionIds(selectedPartitionIds)
                .requiredPartitionIds(requiredPartitionIds)
                .build();
        examDataService.save(examData);
        return examData;
    }
}