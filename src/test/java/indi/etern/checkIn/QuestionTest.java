package indi.etern.checkIn;

import indi.etern.checkIn.entities.question.impl.multipleQuestion.MultipleQuestionBuilder;
import indi.etern.checkIn.entities.question.interfaces.MultiPartitionableQuestion;
import indi.etern.checkIn.entities.question.interfaces.multipleChoice.Choice;
import indi.etern.checkIn.service.dao.ChoiceService;
import indi.etern.checkIn.service.dao.MultiPartitionableQuestionService;
import indi.etern.checkIn.service.dao.PartitionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

@SpringBootTest(classes = CheckInApplication.class,webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class QuestionTest {
    @Autowired
    MultiPartitionableQuestionService multiPartitionableQuestionService;
    @Autowired
    PartitionService partitionService;
    @Autowired
    ChoiceService choiceService;
    @Test
    void testInsert() {
        MultipleQuestionBuilder multipleQuestionBuilder = new MultipleQuestionBuilder();
        multipleQuestionBuilder.setQuestionContent("test insert");
        multipleQuestionBuilder.setId("9c451b9b-73c9-4788-a005-ff382368c500");
        multipleQuestionBuilder.addChoice(new Choice("A", true));
        multipleQuestionBuilder.addChoice(new Choice("B", false));
        MultiPartitionableQuestionService.singletonInstance.save(multipleQuestionBuilder.build());
    }
    @Test
    void testFind() {
        Optional<MultiPartitionableQuestion> question = MultiPartitionableQuestionService.singletonInstance.findById("9c451b9b-73c9-4788-a005-ff382368c500");
        if (question.isPresent()) {
            System.out.println(question.get().getContent());
        } else {
            throw new RuntimeException("not found");
        }
    }
/*
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
//    @BeforeAll
    @Transactional
    public void testGetAnswerAndContent() {
        final User etern = new User("etern", 941651914, "114514");
        {
            MultipleQuestionBuilder multipleQuestionFactory = new MultipleQuestionBuilder();
            Question question = multipleQuestionFactory.addChoices(choices1).setQuestionContent("testQuestion1").setAuthor(etern).build();
            assert question instanceof SingleCorrectQuestion;
            SingleCorrectQuestion multipleCorrectQuestion = (SingleCorrectQuestion) question;
            assert multipleCorrectQuestion.getCorrectChoice().getContent().equals("A");
            assert multipleCorrectQuestion.getContent().equals("testQuestion1");
            assert multipleCorrectQuestion.getPartitions().contains(Partition.getInstance("undefined"));
            question1 = question;
        }
        {
            MultipleQuestionBuilder multipleQuestionFactory = new MultipleQuestionBuilder();
            Question question = multipleQuestionFactory.addChoices(choices2).setQuestionContent("testQuestion2").setAuthor(etern).build();
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
//    @Transactional
    public void testExceptions(){
        List<Choice> choices = new ArrayList<>();
        {
            choices.add(new Choice("A2", false));
            choices.add(new Choice("B2", false));
            choices.add(new Choice("C2", false));
            choices.add(new Choice("D2", false));
        }
        MultipleQuestionBuilder multipleQuestionBuilder = new MultipleQuestionBuilder();
        try {
            Question question = multipleQuestionBuilder.addChoices(choices).build();
        } catch (Exception e){
            assert e.getMessage().equals("No correct choice found");
        }
        
        choices.add(new Choice("E2", true));
        multipleQuestionBuilder.addChoices(choices);
        try {
            Question question = multipleQuestionBuilder.build();
        } catch (Exception e){
            assert e.getMessage().equals("Question content not set");
        }
        Question question = multipleQuestionBuilder.setQuestionContent("testQuestion").build();
        try {
            Question question1 = multipleQuestionBuilder.addChoices(choices).build();
        } catch (Exception e){
            assert e.getMessage().equals("MultipleQuestionBuilder has already built");
        }
    }
*/
/*
    @Test
//    @Transactional
    public void testCheckAnswer() {
//        testGetAnswerAndContent();
        assert question1 instanceof SingleCorrectQuestion;
        assert question2 instanceof MultipleCorrectQuestion;
        assert ((SingleCorrectQuestion) question1).checkAnswer(new Choice("A", true));
        assert !((SingleCorrectQuestion) question1).checkAnswer(new Choice("A", false));
        assert !((SingleCorrectQuestion) question1).checkAnswer(new Choice("B", true));
        assert ((MultipleCorrectQuestion) question2).checkAnswers(choices2);
        choices2.remove(0);
        assert !((MultipleCorrectQuestion) question2).checkAnswers(choices2);
    }
*//*

    @Test
//    @Transactional
    public void insertQuestion() {
//        testGetAnswerAndContent();
//        partitionRepository.save(Partition.getToday("undefined"));
//        choiceRepository.saveAll(((MultipleChoice)question1).getChoices());
        multiPartitionableQuestionService.save((MultiPartitionableQuestion) question1);
        multiPartitionableQuestionService.save((MultiPartitionableQuestion) question2);
//        List<MultiPartitionableQuestion> multiPartitionableQuestions = multiPartitionableQuestionService.findAll();
//        assert multiPartitionableQuestions.size() == 2;
//        assert ((MultipleChoice)multiPartitionableQuestions.get(0)).getChoices().size() == 4;
//        assert ((MultipleChoice)multiPartitionableQuestions.get(1)).getChoices().size() == 4;
    }
    @Test
    @Transactional
    public void getQuestion(){
//        insertQuestion();
        List<MultiPartitionableQuestion> multiPartitionableQuestions = multiPartitionableQuestionService.findAll();
//        assert multiPartitionableQuestions.size() == 2;
        assert ((MultipleChoice)multiPartitionableQuestions.get(0)).getChoices().size() == 4;
        assert ((MultipleChoice)multiPartitionableQuestions.get(1)).getChoices().size() == 4;
    }
    @Test
    @Transactional
    public void getPartition(){
        insertQuestion();
        testGetAnswerAndContent();
        Partition partition = partitionService.findByName("undefined").orElseThrow();
        assert partition.getQuestions().contains(question1);
        assert partition.getQuestions().contains(question2);
    }
    @Test
//    @Transactional
//    @AfterAll
    public void deleteQuestion(){
        multiPartitionableQuestionService.deleteAll();
        partitionService.deleteAll();
        choiceService.deleteAll();
    }
*/
}
