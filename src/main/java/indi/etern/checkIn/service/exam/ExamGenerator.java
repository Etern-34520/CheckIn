package indi.etern.checkIn.service.exam;

import indi.etern.checkIn.entities.exam.ExamData;
import indi.etern.checkIn.entities.question.impl.Partition;
import indi.etern.checkIn.entities.question.impl.Question;
import indi.etern.checkIn.entities.question.impl.group.QuestionGroup;
import indi.etern.checkIn.entities.setting.SettingItem;
import indi.etern.checkIn.service.dao.*;
import indi.etern.checkIn.service.exam.specialPartitionLimit.SpecialPartitionLimit;
import indi.etern.checkIn.service.exam.specialPartitionLimit.SpecialPartitionLimitService;
import indi.etern.checkIn.throwable.exam.generate.MinQuestionLimitOutOfBoundsException;
import indi.etern.checkIn.throwable.exam.generate.NotEnoughQuestionsForExamException;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.Period;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class ExamGenerator {
    private final PartitionService partitionService;
    private final SettingService settingService;
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final QuestionService questionService;
    private final SpecialPartitionLimitService specialPartitionLimitService;
    
    public ExamGenerator(PartitionService partitionService, SettingService settingService, QuestionService questionService, QuestionStatisticService questionStatisticService, SpecialPartitionLimitService specialPartitionLimitService) {
        this.partitionService = partitionService;
        this.settingService = settingService;
        this.questionService = questionService;
        this.specialPartitionLimitService = specialPartitionLimitService;
    }
    
    private void drawQuestions(Set<Question> drewQuestions, List<Partition> partitions, int questionAmount, Random random, DrawingStrategy drawingStrategy, CompletingStrategy completingStrategy) throws NotEnoughQuestionsForExamException, MinQuestionLimitOutOfBoundsException {
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
        
        logger.debug("draw questions({}) for partitions({})", questionAmount, partitions);
        try {
            getQuestions(partitions, random, drawingStrategy, questionAmount, drewQuestions);
        } catch (NotEnoughQuestionsForExamException e) {
            List<Partition> completingPartitions = completingStrategy.getCompletingPartitions();
            logger.debug("completing partitions: {}", completingPartitions);

//            int completingCount = questionAmount - QuestionRealCountCounter.count(drewQuestions);
            logger.debug("draw questions({}) for completing partitions({})", questionAmount, completingPartitions);
            getQuestions(completingPartitions, random, drawingStrategy, questionAmount, drewQuestions);
        }
    }
    
    private void getQuestions(List<Partition> partitions, Random random, DrawingStrategy drawingStrategy, int questionAmount, Set<Question> drewQuestions) throws NotEnoughQuestionsForExamException, MinQuestionLimitOutOfBoundsException {
        Map<Partition, SpecialPartitionLimit> specialPartitionLimitMap = specialPartitionLimitService.getLimits(partitions);
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
        } else {
            drawingStrategy.drawQuestions(drewQuestions, drawersMap, random, questionAmount);
        }
    }
    
    public ExamData generateExam(long qq, List<Partition> selectedPartitions) {
        try {
            final DrawingStrategy drawingStrategy = getDrawingStrategy();
            final CompletingStrategy completingStrategy = getCompletingStrategy();
            
            SettingItem settingItem2 = settingService.getItem("generating", "questionAmount");
            int questionAmount = settingItem2.getValue(Integer.class);
            logger.debug("question amount: {}", questionAmount);
            
            SettingItem settingItem3 = settingService.getItem("generating", "requiredPartitions");
            //noinspection unchecked
            List<String> requiredPartitionIds = (List<String>) settingItem3.getValue(List.class);
            List<String> selectedPartitionIds = selectedPartitions.stream().map(Partition::getId).toList();
            List<Partition> partitions = new ArrayList<>(requiredPartitionIds.size() + selectedPartitionIds.size());
            partitions.addAll(selectedPartitions);
            //TODO improve throughput
            partitions.addAll(partitionService.findAllByIds(requiredPartitionIds));
            if (logger.isDebugEnabled()) {
                logger.debug("required partitions: {}", partitionService.findAllByIds(requiredPartitionIds));
                logger.debug("selected partitions: {}", partitionService.findAllByIds(selectedPartitionIds));
            }
            
            SettingItem expireTimeSetting = settingService.getItem("exam", "expiredPeriod");
            Period expiredPeriod = expireTimeSetting.getValue(Period.class);
            LinkedHashSet<Question> questions = new LinkedHashSet<>();
            drawQuestions(questions, partitions, questionAmount, new Random(), drawingStrategy, completingStrategy);
            
            return ExamData.builder()
                    .id(UUID.randomUUID().toString())
                    .qqNumber(qq)
                    .status(ExamData.Status.ONGOING)
                    .questionIds(questions.stream().map(Question::getId).toList())
                    .questionAmount(questionAmount)
                    .selectedPartitionIds(selectedPartitionIds)
                    .requiredPartitionIds(requiredPartitionIds)
                    .generateTime(LocalDateTime.now())
                    .expireTime(LocalDateTime.now().plus(expiredPeriod))
                    .build();
        } catch (Exception e) {
            logger.error("generate exam failed", e);
            throw e;
        }
    }
    
    private CompletingStrategy getCompletingStrategy() {
        SettingItem settingItem1 = settingService.getItem("generating", "completingStrategy");
        String completingStrategyName = settingItem1.getValue(String.class);
        CompletingStrategy completingStrategy = CompletingStrategy.valueOf(completingStrategyName.toUpperCase());
        logger.debug("use completing strategy: {}", completingStrategy);
        return completingStrategy;
    }
    
    private DrawingStrategy getDrawingStrategy() {
        SettingItem settingItem = settingService.getItem("generating", "drawingStrategy");
        String drawingStrategyName = settingItem.getValue(String.class);
        DrawingStrategy drawingStrategy = DrawingStrategy.valueOf(drawingStrategyName.toUpperCase());
        logger.debug("use drawing strategy: {}", drawingStrategy);
        return drawingStrategy;
    }
    
    @SneakyThrows
    public ExamData remediateExam(ExamData examData) {
        try {
            final List<String> originalIds = examData.getQuestionIds();
            Question[] existedQuestions = new Question[originalIds.size()];
            originalIds.forEach(id -> {
                Optional<Question> optionalQuestion = questionService.findById(id);
                optionalQuestion.ifPresent(question -> existedQuestions[originalIds.indexOf(id)] = question);
            });
            final DrawingStrategy drawingStrategy = getDrawingStrategy();
            final CompletingStrategy completingStrategy = getCompletingStrategy();
            List<Partition> selectedPartitions = partitionService.findAllByIds(examData.getSelectedPartitionIds());
            final Set<Question> questions = new LinkedHashSet<>(
                    examData.getQuestionIds().stream()
                            .map(questionService::findById)
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .toList()
            );
            drawQuestions(questions, selectedPartitions, examData.getQuestionAmount(), new Random(), drawingStrategy, completingStrategy);
            for (Question question : existedQuestions) {
                if (question != null) {
                    questions.remove(question);
                }
            }
            List<Question> questionsList = new ArrayList<>(questions);
            int remediateIndex = 0;
            for (int i = 0; i < existedQuestions.length; i++) {
                if (existedQuestions[i] == null) {
                    existedQuestions[i] = questionsList.get(remediateIndex);
                    remediateIndex++;
                }
            }
            examData.getQuestionIds().clear();
            final List<String> ids = Arrays.stream(existedQuestions).map(Question::getId).toList();
            examData.getQuestionIds().addAll(ids);
//            examDataService.saveAndFlush(examData);
        } catch (Exception e) {
            logger.error("remediate exam failed", e);
            if (logger.isDebugEnabled()) {
                e.printStackTrace();
            }
            throw e;
        }
        return examData;
    }
}