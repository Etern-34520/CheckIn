package indi.etern.checkIn.entities.question.impl.multipleQuestion;

import indi.etern.checkIn.MVCConfig;
import indi.etern.checkIn.entities.question.interfaces.Partition;
import indi.etern.checkIn.entities.question.interfaces.multipleChoice.Choice;
import indi.etern.checkIn.entities.question.interfaces.multipleChoice.SingleCorrect;
import indi.etern.checkIn.entities.user.User;
import jakarta.persistence.*;

import java.util.*;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class SingleCorrectQuestion extends MultipleChoiceQuestion implements SingleCorrect {
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
        dataMap.put("type",getClass().getSimpleName());
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
        final List<Integer> partitionIds = new ArrayList<>();
        for (Partition partition : partitions) {
            partitionNames.add(partition.getName());
            partitionIds.add(partition.getId());
        }
        dataMap.put("partitions", partitionNames);
        dataMap.put("partitionIds",partitionIds);
        dataMap.put("id", id);
        HashMap<String, Object> value = new HashMap<>();
        if (author!=null) {
            value.put("name",author.getUsername());
            value.put("qq",author.getQQNumber());
        } else {
            value.put("name", "unknown");
            value.put("qq", "");
        }
        dataMap.put("author", value);
        dataMap.put("enabled",enabled);
        return MVCConfig.getGson().toJson(dataMap);
    }
}
