package indi.etern.checkIn;

import indi.etern.checkIn.entities.question.Question;
import indi.etern.checkIn.entities.question.impl.multipleQuestion.MultipleCorrectQuestion;
import indi.etern.checkIn.entities.question.impl.multipleQuestion.MultipleQuestionFactory;
import indi.etern.checkIn.entities.question.impl.multipleQuestion.SingleCorrectQuestion;
import indi.etern.checkIn.entities.question.interfaces.MultiPartitionableQuestion;
import indi.etern.checkIn.entities.question.interfaces.Partition;
import indi.etern.checkIn.entities.question.interfaces.multipleChoice.Choice;
import indi.etern.checkIn.entities.question.interfaces.multipleChoice.MultipleChoice;
import indi.etern.checkIn.repositories.ChoiceRepository;
import indi.etern.checkIn.repositories.PartitionRepository;
import indi.etern.checkIn.service.MultiPartitionableQuestionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest(classes = CheckInApplication.class)
@ExtendWith(SpringExtension.class)
public class QuestionTest {
    @Autowired
    MultiPartitionableQuestionService multiPartitionableQuestionService;
    @Autowired
    PartitionRepository partitionRepository;
    @Autowired
    ChoiceRepository choiceRepository;
//    Dao dao = new Dao("indi.etern.checkIn.beans");
    List<Choice> choices1 = new ArrayList<>();
    {
        choices1.add(new Choice("A", true));
        choices1.add(new Choice("B", false));
        choices1.add(new Choice("C", false));
        choices1.add(new Choice("D", false));
    }
    Question question1;
    List<Choice> choices2 = new ArrayList<>();
    {
        choices2.add(new Choice("A1", true));
        choices2.add(new Choice("B1", true));
        choices2.add(new Choice("C1", false));
        choices2.add(new Choice("D1", false));
    }
    Question question2;
    
    public QuestionTest() throws IOException, ClassNotFoundException {
    }
    
    @Test
    public void testGetAnswerAndContent() {
        {
            MultipleQuestionFactory multipleQuestionFactory = new MultipleQuestionFactory();
            Question question = multipleQuestionFactory.addAllChoices(choices1).setQuestionContent("testQuestion1").build();
            assert question instanceof SingleCorrectQuestion;
            SingleCorrectQuestion multipleCorrectQuestion = (SingleCorrectQuestion) question;
            assert multipleCorrectQuestion.getCorrectChoice().getContent().equals("A");
            assert multipleCorrectQuestion.getContent().equals("testQuestion1");
            assert multipleCorrectQuestion.getPartitions().contains(Partition.getInstance("undefined"));
            question1 = question;
        }
        {
            MultipleQuestionFactory multipleQuestionFactory = new MultipleQuestionFactory();
            Question question = multipleQuestionFactory.addAllChoices(choices2).setQuestionContent("testQuestion2").build();
            assert question instanceof MultipleCorrectQuestion;
            MultipleCorrectQuestion multipleCorrectQuestion = (MultipleCorrectQuestion) question;
            assert multipleCorrectQuestion.getCorrectChoices().get(0).getContent().equals("A1");
            assert multipleCorrectQuestion.getCorrectChoices().get(1).getContent().equals("B1");
            assert multipleCorrectQuestion.getContent().equals("testQuestion2");
            assert multipleCorrectQuestion.getPartitions().contains(Partition.getInstance("undefined"));
            question2 = question;
        }
    }
    @Test
    public void testExceptions(){
        List<Choice> choices = new ArrayList<>();
        {
            choices.add(new Choice("A2", false));
            choices.add(new Choice("B2", false));
            choices.add(new Choice("C2", false));
            choices.add(new Choice("D2", false));
        }
        MultipleQuestionFactory multipleQuestionFactory = new MultipleQuestionFactory();
        try {
            Question question = multipleQuestionFactory.addAllChoices(choices).build();
        } catch (Exception e){
            assert e.getMessage().equals("No correct choice found");
        }
        
        choices.add(new Choice("E2", true));
        multipleQuestionFactory.addAllChoices(choices);
        try {
            Question question = multipleQuestionFactory.build();
        } catch (Exception e){
            assert e.getMessage().equals("Question content not set");
        }
        Question question = multipleQuestionFactory.setQuestionContent("testQuestion").build();
        try {
            Question question1 = multipleQuestionFactory.addAllChoices(choices).build();
        } catch (Exception e){
            assert e.getMessage().equals("MultipleQuestionFactory has already built");
        }
    }
    @Test
    public void testCheckAnswer() {
        testGetAnswerAndContent();
        assert question1 instanceof SingleCorrectQuestion;
        assert question2 instanceof MultipleCorrectQuestion;
        assert ((SingleCorrectQuestion) question1).checkAnswer(new Choice("A", true));
        assert !((SingleCorrectQuestion) question1).checkAnswer(new Choice("A", false));
        assert !((SingleCorrectQuestion) question1).checkAnswer(new Choice("B", true));
        assert ((MultipleCorrectQuestion) question2).checkAnswers(choices2);
        choices2.remove(0);
        assert !((MultipleCorrectQuestion) question2).checkAnswers(choices2);
    }
    @Test
    public void insertQuestion() {
        testGetAnswerAndContent();
        partitionRepository.save(Partition.getInstance("undefined"));
//        choiceRepository.saveAll(((MultipleChoice)question1).getChoices());
//        multiPartitionableQuestionService.save((MultiPartitionableQuestion) question1);
//        multiPartitionableQuestionService.save((MultiPartitionableQuestion) question2);
//        List<MultiPartitionableQuestion> multiPartitionableQuestions = multiPartitionableQuestionService.findAll();
//        assert multiPartitionableQuestions.size() == 2;
//        assert ((MultipleChoice)multiPartitionableQuestions.get(0)).getChoices().size() == 4;
//        assert ((MultipleChoice)multiPartitionableQuestions.get(1)).getChoices().size() == 4;
    }
    @Test
    public void updateQuestion(){
//        partitionRepository.(Partition.getInstance("undefined"));
    }
    @Test
    public void getQuestion(){
        List<MultiPartitionableQuestion> multiPartitionableQuestions = multiPartitionableQuestionService.findAll();
        assert multiPartitionableQuestions.size() == 2;
        assert ((MultipleChoice)multiPartitionableQuestions.get(0)).getChoices().size() == 4;
        assert ((MultipleChoice)multiPartitionableQuestions.get(1)).getChoices().size() == 4;
    }
}
