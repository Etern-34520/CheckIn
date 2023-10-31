package indi.etern.checkIn.entities.question.impl.multipleQuestion;

import indi.etern.checkIn.entities.question.interfaces.MultiPartitionableQuestion;
import indi.etern.checkIn.entities.question.interfaces.Partition;
import indi.etern.checkIn.entities.question.interfaces.multipleChoice.Choice;
import indi.etern.checkIn.entities.question.interfaces.multipleChoice.SingleCorrect;
import jakarta.persistence.*;

import java.util.List;
import java.util.Set;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class SingleCorrectQuestion extends MultiPartitionableQuestion implements SingleCorrect {
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "QUESTION_ID",referencedColumnName = "ID")
    List<Choice> choices;
    @Transient
    Choice correctChoice;
    
    protected SingleCorrectQuestion() {
    }
    
    public SingleCorrectQuestion(String questionContent, List<Choice> choices, Set<Partition> partitions) {
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
        initMD5();
        
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
}
