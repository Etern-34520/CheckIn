package indi.etern.checkIn.service.dao;

import indi.etern.checkIn.entities.exam.ExamData;
import indi.etern.checkIn.entities.question.impl.Question;
import indi.etern.checkIn.entities.setting.SettingItem;
import indi.etern.checkIn.entities.setting.grading.GradingLevel;
import indi.etern.checkIn.repositories.ExamDataRepository;
import indi.etern.checkIn.service.exam.ExamGenerator;
import indi.etern.checkIn.service.exam.ExamResult;
import indi.etern.checkIn.throwable.exam.grading.ExamInvalidException;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class ExamDataService {
    public static ExamDataService singletonInstance;
    final SettingService settingService;
    final GradingLevelService gradingLevelService;
    @Resource
    ExamDataRepository examDataRepository;
    private final QuestionService questionService;
    private final Logger logger = LoggerFactory.getLogger(ExamDataService.class);
    private final ExamGenerator examGenerator;
    private final QuestionStatisticService questionStatisticService;
    
    protected ExamDataService(SettingService settingService, GradingLevelService gradingLevelService, QuestionService questionService, ExamGenerator examGenerator, QuestionStatisticService questionStatisticService) {
        singletonInstance = this;
        this.settingService = settingService;
        this.gradingLevelService = gradingLevelService;
        this.questionService = questionService;
        this.examGenerator = examGenerator;
        this.questionStatisticService = questionStatisticService;
    }
    
    public void save(ExamData examData) {
        examDataRepository.save(examData);
    }
    
    public Optional<ExamData> findById(String id) {
        return examDataRepository.findById(id);
    }
    
    public void saveAndFlush(ExamData examData) {
        examDataRepository.saveAndFlush(examData);
    }
    
    @Transactional
    public ExamResult handleSubmit(ExamData examData, Map<String, Object> answer) throws ExamInvalidException {
        if (examData.getStatus()!= ExamData.Status.ONGOING) {
            throw new ExamInvalidException();
        }
        final ExamResult examResult = examData.checkAnswerMap(answer);
        examData.setStatus(ExamData.Status.SUBMITTED);
        
        //noinspection unchecked
        ArrayList<Number> levelSplit = settingService.getItem("grading","splits").getValue(ArrayList.class);
        List<GradingLevel> gradingLevels = gradingLevelService.findAll();
        
        Float[] levelSplitArray = new Float[levelSplit.size() + 1];
        AtomicInteger index = new AtomicInteger(0);
        levelSplit.forEach(splitScore -> {
            levelSplitArray[index.get()] = splitScore.floatValue();
            index.incrementAndGet();
        });
        
        SettingItem scoreSettingItem = settingService.getItem("grading","questionScore");
        float singleQuestionScore = scoreSettingItem.getValue(Number.class).floatValue();
        
        levelSplitArray[levelSplitArray.length - 1] = examData.getQuestionAmount() * singleQuestionScore;
        
        final int searchResult = Arrays.binarySearch(levelSplitArray, examResult.getScore());
        int levelIndex;
        if (searchResult == levelSplitArray.length - 1) {//max score
            levelIndex = levelSplitArray.length - 2;//avoid overflow
        } else {
            if (searchResult >= 0) levelIndex = searchResult;//at lower point
            else if (searchResult == -1) levelIndex = 0;//at lower point
            else levelIndex = -searchResult - 2;//at part
        }
        final GradingLevel gradingLevel = gradingLevels.get(levelIndex);
        examResult.setLevel(gradingLevel.getName());
        examResult.setMessage(gradingLevel.getMessage());
        examResult.setColorHex(gradingLevel.getColorHex());
        
        examData.setExamResult(examResult);
        examData.setSubmitTime(LocalDateTime.now());
        examDataRepository.save(examData);
        return examResult;
    }
    
    public List<ExamData> findAllBySubmitTimeBetween(LocalDate from, LocalDate to) {
        return examDataRepository.findAllBySubmitTimeBetween(from.atStartOfDay(), to.plusDays(1).atStartOfDay());
    }
    
    public List<ExamData> findAllByGenerateTimeBetween(LocalDate from, LocalDate to) {
        return examDataRepository.findAllByGenerateTimeBetween(from.atStartOfDay(), to.plusDays(1).atStartOfDay());
    }
    
    public Map<String, Object> getExamDataQuestions(int[] indexes, ExamData examData) throws ExamInvalidException {
        if (examData.getStatus() == ExamData.Status.ONGOING) {
            Map<String, Object> result = new HashMap<>();
            List<String> allQuestionIds = examData.getQuestionIds();
            List<String> questionIds = new ArrayList<>();
            for (int index : indexes) {
                questionIds.add(allQuestionIds.get(index));
            }
            final List<Question> questions = new ArrayList<>(questionService.findAllById(questionIds));
            if (questions.size() != indexes.length) {
                logger.warn("some questions of the exam is missing, remediate exam");
                ExamData examData1 = examGenerator.remediateExam(examData);
                examDataRepository.save(examData1);
                questionStatisticService.appendStatistic(examData);
                return getExamDataQuestions(indexes, examData);
//            throw new RuntimeException("Questions not found");
            }
            Map<Integer, Question> indexQuestionMap = new HashMap<>();
            questions.forEach(question -> indexQuestionMap.put(allQuestionIds.indexOf(question.getId()), question));
            result.put("questions", indexQuestionMap);
            return result;
        } else {
            logger.error("Exam[{}] invalided", examData.getId());
            throw new ExamInvalidException();
        }
    }
    
    public Optional<ExamData> findMaxByQQ(long qq) {
        List<ExamData> examDataList = examDataRepository.findAllByQqNumberIs(qq);
        return examDataList.stream().max(Comparator.comparing(ExamData::getExamResult));//TODO expire time
    }
    
    public void invalidAllByQQ(long qq) {
        List<ExamData> examDataList = examDataRepository.findAllByQqNumberAndStatus(qq, ExamData.Status.ONGOING);
        for (ExamData examData : examDataList) {
            examData.setStatus(ExamData.Status.EXPIRED);
        }
        examDataRepository.saveAll(examDataList);
    }
}