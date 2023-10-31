package indi.etern.checkIn.entities.question.impl.multipleQuestion;

import indi.etern.checkIn.entities.question.interfaces.MultiPartitionableQuestion;
import indi.etern.checkIn.entities.question.interfaces.Partition;
import indi.etern.checkIn.entities.question.interfaces.multipleChoice.Choice;
import indi.etern.checkIn.entities.question.interfaces.multipleChoice.MultipleCorrect;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
@Entity
@Inheritance(strategy= InheritanceType.SINGLE_TABLE)
public class MultipleCorrectQuestion extends MultiPartitionableQuestion implements MultipleCorrect {
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "question_id",referencedColumnName = "id")
    List<Choice> choices;
    @Transient
    List<Choice> correctChoices;
    
    protected MultipleCorrectQuestion() {
    }
    
    public MultipleCorrectQuestion(String questionContent, List<Choice> choices, Set<Partition> partitions) {
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
        initMD5();
//        super.id = md5;
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
    @PostLoad//FIXME
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
}
