package indi.etern.checkIn.entities.question.impl.multipleQuestion;

import indi.etern.checkIn.entities.question.Question;
import indi.etern.checkIn.entities.question.interfaces.MultiPartitionableQuestion;
import indi.etern.checkIn.entities.question.interfaces.Partition;
import indi.etern.checkIn.entities.question.interfaces.multipleChoice.Choice;

import java.util.*;

public class MultipleQuestionFactory {
    MultiPartitionableQuestion multipleQuestion;
    List<Choice> choices = new ArrayList<>();
    boolean haveBuilt = false;
    String questionContent;
    Set<Partition> partitions = new HashSet<>();
    public MultipleQuestionFactory addChoice(Choice choice){
        choices.add(choice);
        return this;
    }
    public MultipleQuestionFactory addAllChoices(List<Choice> choices){
        this.choices.addAll(choices);
        return this;
    }
    public MultipleQuestionFactory addChoices(Choice... choices){
        Collections.addAll(this.choices, choices);
        return this;
    }
    public MultipleQuestionFactory setQuestionContent(String content){
        questionContent = content;
        return this;
    }
    public MultipleQuestionFactory addPartition(String partitionString){
        partitions.add(Partition.getInstance(partitionString));
        return this;
    }
    public MultipleQuestionFactory addPartition(Partition partition){
        partitions.add(partition);
        return this;
    }
    public Question build(){
        if (haveBuilt){
            throw new MultipleQuestionFactoryException("MultipleQuestionFactory has already built");
        }
        boolean singleCorrect = false;
        boolean multipleCorrect = false;
        for (Choice choice:choices) {
            if (choice.isCorrect()&&!multipleCorrect&&!singleCorrect){
                singleCorrect = true;
            } else if (choice.isCorrect()&&singleCorrect){
                multipleCorrect = true;
                singleCorrect = false;
            }
        }
        if (!singleCorrect&&!multipleCorrect){
            throw new MultipleQuestionFactoryException("No correct choice found");
        }
        if (questionContent==null){
            throw new MultipleQuestionFactoryException("Question content not set");
        }
        if (choices.size() < 2) {
            throw new QuestionException("Less than two choices");
        }
        if (partitions.isEmpty()){
            partitions.add(Partition.getInstance("undefined"));
        }
        if (singleCorrect){
            multipleQuestion = new SingleCorrectQuestion(questionContent,choices,partitions);
        } else {
            multipleQuestion = new MultipleCorrectQuestion(questionContent,choices,partitions);
        }
        for (Partition partition : partitions) {
            partition.addQuestion(multipleQuestion);
        }
        haveBuilt = true;
        return multipleQuestion;
    }
}
