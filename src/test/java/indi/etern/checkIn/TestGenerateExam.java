package indi.etern.checkIn;

import com.fasterxml.jackson.databind.ObjectMapper;
import indi.etern.checkIn.entities.exam.ExamData;
import indi.etern.checkIn.entities.question.impl.Partition;
import indi.etern.checkIn.service.dao.QuestionService;
import indi.etern.checkIn.service.exam.ExamGenerator;
import indi.etern.checkIn.utils.QuestionRealCountCounter;
import indi.etern.checkIn.utils.TransactionTemplateUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@SpringBootTest(classes = CheckInApplication.class,webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TestGenerateExam {
    @Autowired
    ExamGenerator examGenerator;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    private QuestionService questionService;
    private Logger logger = LoggerFactory.getLogger(TestGenerateExam.class);
    
    @SneakyThrows
    @Test
    void testGenerate () {
        List<Partition> partitions = new ArrayList<>();
        partitions.add(Partition.ofName("test"));
        TransactionTemplateUtil.getTransactionTemplate().executeWithoutResult((v) -> {
            try {
                for (int i = 0; i < 200; i++) {
                    logger.info("Generating exam {}",i);
                    ExamData data = examGenerator.generateExam(114514,partitions);
                    System.out.println(objectMapper.writeValueAsString(data));
                    final int count = QuestionRealCountCounter.count(questionService.findAllById(data.getQuestionIds()));
                    logger.info("Exam {} has {} questions",i,count);
                    assert count == 20;
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}