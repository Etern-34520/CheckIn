package indi.etern.checkIn.entities.question.impl.multipleQuestion;

import com.google.gson.Gson;
import indi.etern.checkIn.entities.question.interfaces.MultiPartitionableQuestion;
import indi.etern.checkIn.entities.question.interfaces.Partition;
import indi.etern.checkIn.entities.question.interfaces.multipleChoice.Choice;
import indi.etern.checkIn.entities.question.interfaces.multipleChoice.SingleCorrect;
import indi.etern.checkIn.entities.user.User;
import jakarta.persistence.*;

import java.util.*;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class SingleCorrectQuestion extends MultiPartitionableQuestion implements SingleCorrect {
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "QUESTION_ID",referencedColumnName = "ID")
    List<Choice> choices;
    @Transient
    Choice correctChoice;
    
    protected SingleCorrectQuestion() {
    }
    
    public SingleCorrectQuestion(String questionContent, List<Choice> choices, Set<Partition> partitions, User author) {
        if (choices.size() < 2) {
            throw new QuestionException("Less than two choices");
        }
        if (questionContent == null) {
            throw new QuestionException("Question content not set");
        }
        content = questionContent;
        for (Choice choice:choices){
            choice.setQuestion(this);
        }
        this.choices = choices;
        this.correctChoice = getCorrectChoice(choices);
        this.partitions = partitions;
        this.author = author;
//        initMD5();
//        super.id = md5;
    }
    
    private static Choice getCorrectChoice(List<Choice> choices) {
        boolean hasCorrect = false;
        Choice correctChoice = null;
        for (Choice choice : choices) {
            if (choice.isCorrect()) {
                if (hasCorrect) {
                    throw new QuestionException("Has more than one correct choice");
                } else {
                    hasCorrect = true;
                    correctChoice = choice;
                }
            }
        }
        if (correctChoice == null) {
            throw new QuestionException("No correct choice found");
        }
        return correctChoice;
    }
    
    @Override
    public List<Choice> getChoices() {
        return choices;
    }
    @PostLoad
    public void filterCorrectChoices() {
        for (Choice choice : choices){
            if (choice.isCorrect()){
                correctChoice = choice;
                break;
            }
        }
    }
    @Override
    public Choice getCorrectChoice() {
        return correctChoice;
    }
    
    @Override
    public boolean checkAnswer(Choice choice) {
        if (choice.isCorrect()) {
            return choice.getContent().equals(correctChoice.getContent());
        } else {
            return false;
        }
    }
    
    @Override
    public boolean checkAnswer(String choiceContent) {
        return choiceContent.equals(correctChoice.getContent());
    }
    
    
    @Override
    public String toString() {
        return "SingleCorrectQuestion{" +
                "choices=" + choices +
                ", correctChoice=" + correctChoice +
                ", content='" + content + '\'' +
                ", partitions='" + (partitions != null ? partitions.toString() : "[]") + '\'' +
                '}';
    }
    @Override
    public String toJsonData() {
        Map<String,Object> dataMap = new HashMap<>();
        dataMap.put("type","singleCorrect");
        dataMap.put("content",content);
        List<Map<String,String>> choices = new ArrayList<>();
        for (Choice choice : this.choices) {
            Map<String,String> choiceMap = new HashMap<>();
            choiceMap.put("content",choice.getContent());
            choiceMap.put("correct",String.valueOf(choice.isCorrect()));
            choices.add(choiceMap);
        }
        dataMap.put("choices",choices);
        dataMap.put("correctChoice","{content:"+correctChoice.getContent()+",correct:"+correctChoice.isCorrect()+"}");
        final List<String> partitionNames = new ArrayList<>();
        for (Partition partition : partitions) {
            partitionNames.add(partition.getName());
        }
        dataMap.put("partitions", partitionNames);
        return new Gson().toJson(dataMap);
    }
}
