package indi.etern.checkIn;

import com.google.gson.Gson;
import indi.etern.checkIn.entities.question.impl.question.MultipleChoicesQuestion;
import indi.etern.checkIn.entities.question.impl.Question;
import indi.etern.checkIn.entities.question.impl.Choice;
import indi.etern.checkIn.service.dao.QuestionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@SpringBootTest(classes = CheckInApplication.class,webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class JsonParseTest {
    @Autowired
    Gson gson;// = new Gson();
    @Autowired
    QuestionService multiPartitionableQuestionService;
    @Test
    public void parse() throws IOException {
        Path jsonPath = Paths.get("questions.json");
        String content = Files.readString(jsonPath);
        List<Map<String,Object>> questions = gson.fromJson(content, List.class);
        for (Map<String,Object> questionMap : questions) {
            MultipleChoicesQuestion.Builder multipleQuestionBuilder = new MultipleChoicesQuestion.Builder();
            String questionContent = (String) questionMap.get("question_content");
            questionContent = questionContent.replace(" [单选题]","");
            multipleQuestionBuilder.setQuestionContent(questionContent.split("\\.",2)[1]);
            List<Double> correctIndex = (List<Double>) questionMap.get("correct_options");
            int index = 0;
            for (String choiceStr : (List<String>)questionMap.get("options")) {
//                String pattern = "^[A-Z]\\.\\s.*$";
//                if (choiceStr.startsWith()
                if (choiceStr.getBytes()[1] == '.') {
                    choiceStr = choiceStr.substring(2);
                }
                Choice choice = new Choice(choiceStr,correctIndex.contains((double) index));
                multipleQuestionBuilder.addChoice(choice);
                index++;
            }
            Question question = multipleQuestionBuilder.build();
            multiPartitionableQuestionService.save(question);
        }
    }
}
