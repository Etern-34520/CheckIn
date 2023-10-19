package indi.etern.checkIn.entities.question.impl.multipleQuestion;

import indi.etern.checkIn.entities.question.interfaces.MultiPartitionableQuestion;
import indi.etern.checkIn.entities.question.interfaces.Partition;
import indi.etern.checkIn.entities.question.interfaces.multipleChoice.Choice;
import indi.etern.checkIn.entities.question.interfaces.multipleChoice.SingleCorrect;

import java.util.List;
import java.util.Set;

public class SingleCorrectQuestion extends MultiPartitionableQuestion implements SingleCorrect {
    List<Choice> choices;
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
        this.choices = choices;
        this.correctChoice = getCorrectChoice(choices);
        this.partitions = partitions;
        initMD5();
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
        if (correctChoice==null){
            throw new QuestionException("No correct choice found");
        }
        return correctChoice;
    }
    
    @Override
    public List<Choice> getChoices() {
        return choices;
    }
    
    @Override
    public Choice getCorrectChoice() {
        return correctChoice;
    }
    
    @Override
    public boolean checkAnswer(Choice choice) {
        if (choice.isCorrect()){
            return choice.getContent().equals(correctChoice.getContent());
        } else {
            return false;
        }
    }
    @Override
    public boolean checkAnswer(String choiceContent){
        return choiceContent.equals(correctChoice.getContent());
    }
    
    
    @Override
    public String toString() {
        return "SingleCorrectQuestion{" +
                "choices=" + choices +
                ", correctChoice=" + correctChoice +
                ", content='" + content + '\'' +
                ", partitions='" + (partitions != null?partitions.toString():"[]") + '\'' +
                '}';
    }
}
