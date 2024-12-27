package indi.etern.checkIn;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import indi.etern.checkIn.entities.question.impl.Question;
import indi.etern.checkIn.entities.question.impl.Choice;
import indi.etern.checkIn.entities.question.impl.question.MultipleChoicesQuestion;
import indi.etern.checkIn.service.dao.QuestionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;


@SpringBootTest(classes = CheckInApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TestIncludeExcel {
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private QuestionService multiPartitionableQuestionService;

    @Test
    void include() {
        String fileName = ".\\question.xlsx";
        List<Question> multiPartitionableQuestionList = new ArrayList<>(250);
        // 这里 需要指定读用哪个class去读，然后读取第一个sheet 文件流会自动关闭
        EasyExcel.read(fileName, ExcelQuestion.class, new ReadListener<ExcelQuestion>() {
            public static final int BATCH_COUNT = 100;
            private List<ExcelQuestion> cachedDataList = new ArrayList<>(BATCH_COUNT);

            @Override
            public void invoke(ExcelQuestion data, AnalysisContext context) {
                cachedDataList.add(data);
                if (cachedDataList.size() >= BATCH_COUNT) {
                    saveData();
                    // 存储完成清理 list
                    cachedDataList = new ArrayList<>(BATCH_COUNT);
                }
            }

            @Override
            public void doAfterAllAnalysed(AnalysisContext context) {
                saveData();
            }

            /**
             * 加上存储数据库
             */

            private void saveData() {
                for (ExcelQuestion excelQuestion : cachedDataList) {
                    try {
                        MultipleChoicesQuestion.Builder multipleQuestionBuilder = new MultipleChoicesQuestion.Builder();
                        multipleQuestionBuilder.setQuestionContent(excelQuestion.content.replace(" [单选题]","").replace("[单选题]",""));
                        List<String> choicesString = objectMapper.readValue(excelQuestion.choiceJsonArray.replace("，",",").replace("“","\""), List.class);
                        List<Integer> correctIndexes = objectMapper.readValue(excelQuestion.correctChoiceIndexJsonArray, List.class);
                        int index = 0;
                        for (String choiceString : choicesString) {
                            boolean isCorrect = correctIndexes.contains(index);
                            String[] split = choiceString.split("\\.", 2);
                            choiceString = split.length>=2?split[1]:choiceString;
                            choiceString = choiceString.trim();
                            Choice choice = new Choice(choiceString,isCorrect);
                            multipleQuestionBuilder.addChoice(choice);
                            index++;
                        }
                        Question build = multipleQuestionBuilder.build();
//                        multiPartitionableQuestionService.save(build);
                        multiPartitionableQuestionList.add(build);
//                        System.out.println(index);
                    } catch (Exception e) {
                        System.err.println(excelQuestion.count);
//                        System.err.println(excelQuestion);
                        System.err.println(e.getMessage());
                    }
                }
//                log.info("{}条数据，开始存储数据库！", cachedDataList.size());
//                log.info("存储数据库成功！");
            }
        }).sheet().doRead();
        System.out.println(multiPartitionableQuestionList.size());
        multiPartitionableQuestionService.saveAll(multiPartitionableQuestionList);
    }

    @Test
    void TestSize() {
        System.out.println(multiPartitionableQuestionService.count());
    }
}
