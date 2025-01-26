package indi.etern.checkIn.service.dao;

import indi.etern.checkIn.entities.exam.ExamData;
import indi.etern.checkIn.entities.setting.SettingItem;
import indi.etern.checkIn.entities.setting.grading.GradingLevel;
import indi.etern.checkIn.repositories.ExamDataRepository;
import indi.etern.checkIn.service.exam.ExamResult;
import indi.etern.checkIn.throwable.exam.grading.ExamInvalidException;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class ExamDataService {
    public static ExamDataService singletonInstance;
    final SettingService settingService;
    final GradingLevelService gradingLevelService;
    @Resource
    ExamDataRepository examDataRepository;
    
    protected ExamDataService(SettingService settingService, GradingLevelService gradingLevelService) {
        singletonInstance = this;
        this.settingService = settingService;
        this.gradingLevelService = gradingLevelService;
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
        ArrayList<Number> levelSplit = settingService.getItem("grading.splits").getValue(ArrayList.class);
        List<GradingLevel> gradingLevels = gradingLevelService.findAll();
        
        Float[] levelSplitArray = new Float[levelSplit.size() + 1];
        AtomicInteger index = new AtomicInteger(0);
        levelSplit.forEach(splitScore -> {
            levelSplitArray[index.get()] = splitScore.floatValue();
            index.incrementAndGet();
        });
        
        SettingItem scoreSettingItem = SettingService.singletonInstance.findItem("grading.questionScore").orElseThrow();
        float singleQuestionScore = scoreSettingItem.getValue(Number.class).floatValue();
        
        levelSplitArray[levelSplitArray.length - 1] = examData.getQuestionAmount() * singleQuestionScore;
        
        final int searchResult = Arrays.binarySearch(levelSplitArray, examResult.getScore());
        int levelIndex;
        if (searchResult == levelSplitArray.length - 1) {//max score
            levelIndex = levelSplitArray.length - 2;//avoid overflow
        } else {
            if (searchResult >= 0) levelIndex = searchResult - 1;//at lower point
            else levelIndex = -searchResult - 2;//at part
        }
        final GradingLevel gradingLevel = gradingLevels.get(levelIndex);
        examResult.setLevel(gradingLevel.getName());
        examResult.setMessage(gradingLevel.getMessage());
        examResult.setColorHex(gradingLevel.getColorHex());
        
        examData.setExamResult(examResult);
        examDataRepository.save(examData);
        return examResult;
    }
}