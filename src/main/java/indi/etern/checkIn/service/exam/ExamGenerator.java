package indi.etern.checkIn.service.exam;

import indi.etern.checkIn.entities.exam.ExamData;
import indi.etern.checkIn.entities.linkUtils.impl.ToPartitionsLink;
import indi.etern.checkIn.entities.question.impl.Partition;
import indi.etern.checkIn.entities.question.impl.Question;
import indi.etern.checkIn.entities.question.impl.QuestionGroup;
import indi.etern.checkIn.entities.setting.SettingItem;
import indi.etern.checkIn.service.dao.PartitionService;
import indi.etern.checkIn.service.dao.QuestionService;
import indi.etern.checkIn.service.dao.QuestionStatisticService;
import indi.etern.checkIn.service.dao.SettingService;
import indi.etern.checkIn.service.exam.specialPartitionLimit.SpecialPartitionLimit;
import indi.etern.checkIn.service.exam.specialPartitionLimit.SpecialPartitionLimitService;
import indi.etern.checkIn.throwable.exam.generate.MinQuestionLimitOutOfBoundsException;
import indi.etern.checkIn.throwable.exam.generate.NotEnoughQuestionsForExamException;
import indi.etern.checkIn.throwable.exam.generate.UnachievableLimitException;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.Period;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("ALL")
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
    
    private void sampleQuestions(Set<Question> drewQuestions, List<Partition> partitions, int questionAmount, Random random, SamplingStrategy samplingStrategy, CompletingStrategy completingStrategy) throws NotEnoughQuestionsForExamException, MinQuestionLimitOutOfBoundsException {
        if (logger.isDebugEnabled()) {
            List<Question> allEnabledQuestions = new ArrayList<>();
            partitions.forEach(partition -> {
                Set<Question> questionList = new HashSet<>();
                final Set<ToPartitionsLink> questionLinks = partition.getQuestionLinks();
                for (ToPartitionsLink questionLink : questionLinks) {
                    Question question = questionLink.getSource();
                    if (question.isEnabled()) {
                        questionList.add(question);
                    }
                }
                allEnabledQuestions.addAll(questionList);
            });
            AtomicInteger allEnabledQuestionsCount = new AtomicInteger();
            allEnabledQuestions.forEach(question -> {
                if (question instanceof QuestionGroup questionGroup) {
                    allEnabledQuestionsCount.addAndGet(questionGroup.getQuestionLinks().size());
                } else {
                    allEnabledQuestionsCount.getAndIncrement();
                }
            });
            logger.debug("all enabled questions[{}]: {}", allEnabledQuestionsCount, allEnabledQuestions.stream().map(Question::getId).toList());
            logger.debug("sample questions({}) for partitions({})", questionAmount, partitions);
        }
        
        try {
            getQuestions(partitions, random, samplingStrategy, questionAmount, drewQuestions);
        } catch (NotEnoughQuestionsForExamException e) {
            List<Partition> completingPartitions = completingStrategy.getCompletingPartitions();
            logger.debug("completing partitions: {}", completingPartitions);

//            int completingCount = questionAmount - QuestionRealCountCounter.count(drewQuestions);
            logger.debug("sample questions({}) for completing partitions({})", questionAmount, completingPartitions);
            getQuestions(completingPartitions, random, samplingStrategy, questionAmount, drewQuestions);
        }
    }
    
    private void getQuestions(List<Partition> partitions, Random random, SamplingStrategy samplingStrategy, int questionAmount, Set<Question> sampledQuestions) throws NotEnoughQuestionsForExamException, MinQuestionLimitOutOfBoundsException {
        Map<Partition, SpecialPartitionLimit> specialPartitionLimitMap = specialPartitionLimitService.getLimits(partitions);
        Map<Partition, PartitionQuestionSampler> samplersMap = new HashMap<>();
        partitions.forEach((partition) -> {
            final PartitionQuestionSampler sampler = new PartitionQuestionSampler(partition, random);
            sampler.setSpecialPartitionLimit(specialPartitionLimitMap.get(partition));
            samplersMap.put(partition, sampler);
        });
        samplersMap.values().forEach(sampler -> {
            sampler.invalidAll(sampledQuestions);
            sampledQuestions.addAll(sampler.initLeastQuestions());
        });
        
        if (questionAmount < 0) {
            throw new MinQuestionLimitOutOfBoundsException();
        } else if (questionAmount == 0) {
            logger.debug("no need to sample more questions");
        } else {
            try {
                samplingStrategy.sampleQuestions(sampledQuestions, samplersMap, random, questionAmount);
            } catch (IndexOutOfBoundsException e) {
                throw new UnachievableLimitException(e);
            }
        }
    }
    
    public ExamData generateExam(long qq, List<Partition> selectedPartitions) {
        try {
            final SamplingStrategy samplingStrategy = getSamplingStrategy();
            final CompletingStrategy completingStrategy = getCompletingStrategy();
            
            SettingItem settingItem2 = settingService.getItem("generating", "questionAmount");
            int questionAmount = settingItem2.getValue(Integer.class);
            logger.debug("question amount: {}", questionAmount);
            
            List<String> requiredPartitionIds;
            
            try {
                SettingItem settingItem3 = settingService.getItem("generating", "requiredPartitions");
                //noinspection unchecked
                requiredPartitionIds = (List<String>) settingItem3.getValue(List.class);
            } catch (Exception ignored) {
                requiredPartitionIds = Collections.emptyList();
            }
            
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
            sampleQuestions(questions, partitions, questionAmount, new Random(), samplingStrategy, completingStrategy);
            
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
    
    private SamplingStrategy getSamplingStrategy() {
        SettingItem settingItem = settingService.getItem("generating", "samplingStrategy");
        String drawingStrategyName = settingItem.getValue(String.class);
        SamplingStrategy samplingStrategy = SamplingStrategy.valueOf(drawingStrategyName.toUpperCase());
        logger.debug("use drawing strategy: {}", samplingStrategy);
        return samplingStrategy;
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
            final SamplingStrategy samplingStrategy = getSamplingStrategy();
            final CompletingStrategy completingStrategy = getCompletingStrategy();
            List<Partition> selectedPartitions = partitionService.findAllByIds(examData.getSelectedPartitionIds());
            final Set<Question> questions = new LinkedHashSet<>(
                    examData.getQuestionIds().stream()
                            .map(questionService::findById)
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .toList()
            );
            sampleQuestions(questions, selectedPartitions, examData.getQuestionAmount(), new Random(), samplingStrategy, completingStrategy);
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