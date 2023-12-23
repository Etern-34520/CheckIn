package indi.etern.checkIn.entities.question.impl.multipleQuestion;

import indi.etern.checkIn.MVCConfig;
import indi.etern.checkIn.entities.question.interfaces.MultiPartitionableQuestion;
import indi.etern.checkIn.entities.question.interfaces.Partition;
import indi.etern.checkIn.entities.question.interfaces.multipleChoice.Choice;
import indi.etern.checkIn.entities.question.interfaces.multipleChoice.MultipleCorrect;
import indi.etern.checkIn.entities.user.User;
import jakarta.persistence.*;

import java.util.*;

@Entity
@Inheritance(strategy= InheritanceType.SINGLE_TABLE)
public class MultipleCorrectQuestion extends MultiPartitionableQuestion implements MultipleCorrect {
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id",referencedColumnName = "id")
    List<Choice> choices;
    @Transient
    List<Choice> correctChoices;
    
    protected MultipleCorrectQuestion() {
    }
    
    public MultipleCorrectQuestion(String questionContent, List<Choice> choices, Set<Partition> partitions, User author) {
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
        this.correctChoices = getCorrectChoices(choices);
        this.partitions = partitions;
        this.author = author;
    }
    
    private static List<Choice> getCorrectChoices(List<Choice> choices) {
        List<Choice> correctChoice = new ArrayList<>();
        for (Choice choice : choices) {
            if (choice.isCorrect()) {
                correctChoice.add(choice);
            }
        }
        if (correctChoice.isEmpty()){
            throw new QuestionException("No correct choice found");
        } else if (correctChoice.size() == 1){
            throw new QuestionException("Only one correct choice found, use SingleCorrectQuestion instead");
        }
        return correctChoice;
    }
    @Override
    public List<Choice> getChoices() {
        return choices;
    }
    @PostLoad
    public void filterCorrectChoices() {
        if (choices == null){
            return;
        }
        List<Choice> newChoices = new ArrayList<>();
        for (Choice choice : choices){
            if (choice.isCorrect()){
                newChoices.add(choice);
            }
        }
        correctChoices = newChoices;
    }
    @Override
    public List<Choice> getCorrectChoices() {
        return correctChoices;
    }
    
    @Override
    public boolean checkAnswers(List<Choice> choices) {
        boolean correct = true;
        List<Choice> correctChoicesCopy = new ArrayList<>(correctChoices);
        for (Choice choice : choices){
            for (Choice choice1 : correctChoicesCopy) {
                if (choice.getContent().equals(choice1.getContent())) {
                    correctChoicesCopy.remove(choice1);
                    correct = true;
                    break;
                } else {
                    correct = false;
                }
            }
        }
        return correct;
    }
    
    @Override
    public String toString() {
        return "MultipleCorrectQuestion{" +
                "choices=" + choices +
                ", correctChoices=" + correctChoices +
                ", content='" + content + '\'' +
                ", partitions='" + (partitions != null?partitions.toString():"[]") + '\'' +
                '}';
    }
    
    @Override
    public String toJsonData() {
        Map<String,Object> dataMap = new HashMap<>();
        dataMap.put("type",getClass().getSimpleName());
        dataMap.put("content",content);
        List<Map<String,String>> choices = new ArrayList<>();
        List<Map<String,String>> correctChoices = new ArrayList<>();
        for (Choice choice : this.choices) {
            Map<String,String> choiceMap = new HashMap<>();
            choiceMap.put("content",choice.getContent());
            choiceMap.put("correct",String.valueOf(choice.isCorrect()));
            choices.add(choiceMap);
            if(choice.isCorrect()){
                correctChoices.add(choiceMap);
            }
        }
        dataMap.put("choices",choices);
        dataMap.put("correctChoices",correctChoices);
        final List<String> partitionNames = new ArrayList<>();
        final List<Integer> partitionIds = new ArrayList<>();
        for (Partition partition : partitions) {
            partitionNames.add(partition.getName());
            partitionIds.add(partition.getId());
        }
        dataMap.put("partitions", partitionNames);
        dataMap.put("partitionIds",partitionIds);
        dataMap.put("md5",md5);
        return MVCConfig.getGson().toJson(dataMap);
    }
}
