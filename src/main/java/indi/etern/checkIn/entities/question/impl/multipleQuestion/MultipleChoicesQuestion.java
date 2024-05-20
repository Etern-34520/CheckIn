package indi.etern.checkIn.entities.question.impl.multipleQuestion;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import indi.etern.checkIn.entities.convertor.MapConverter;
import indi.etern.checkIn.entities.question.interfaces.ImagesWith;
import indi.etern.checkIn.entities.question.interfaces.MultiPartitionableQuestion;
import indi.etern.checkIn.entities.question.interfaces.Partition;
import indi.etern.checkIn.entities.question.interfaces.multipleChoice.Choice;
import indi.etern.checkIn.entities.serializer.ExamQuestionSerializer;
import indi.etern.checkIn.entities.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@JsonSerialize(using = ExamQuestionSerializer.class)
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Getter
public class MultipleChoicesQuestion extends MultiPartitionableQuestion implements ImagesWith {
    protected MultipleChoicesQuestion() {
    }

    public MultipleChoicesQuestion(String questionContent, List<Choice> choices, Set<Partition> partitions, User author) {
        content = questionContent;
        this.choices = choices;
        this.partitions = partitions;
        this.author = author;
        boolean singleCorrect = false;
        boolean multipleCorrect = false;
        for (Choice choice : choices) {
            if (!singleCorrect && choice.isCorrect()) {
                singleCorrect = true;
            } else if (choice.isCorrect()) {
                multipleCorrect = true;
            }
        }
        if (singleCorrect) type = Type.SINGLE_CORRECT;
        else if (multipleCorrect) type = Type.MULTIPLE_CORRECT;
        else throw new QuestionException("No correct choice found");
    }

    @Getter
    public enum Type {
        SINGLE_CORRECT("single_correct") {
            private Choice correctChoice;

            public Choice getCorrectChoice(List<Choice> choices) {
                if (correctChoice != null) {
                    return correctChoice;
                }
                for (Choice choice : choices) {
                    if (choice.isCorrect()) {
                        correctChoice = choice;
                        return choice;
                    }
                }
                throw new QuestionException("No correct choice found");
            }
        }, MULTIPLE_CORRECT("multiple_correct") {
            private List<Choice> correctChoices;

            public List<Choice> getCorrectChoices(List<Choice> choices) {
                if (correctChoices != null) {
                    return correctChoices;
                }
                List<Choice> correctChoices = new ArrayList<>();
                for (Choice choice : choices) {
                    if (choice.isCorrect()) {
                        correctChoices.add(choice);
                    }
                }
                if (correctChoices.isEmpty()) {
                    throw new QuestionException("No correct choice found");
                }
                this.correctChoices = correctChoices;
                return correctChoices;
            }
        };

        private final String name;
        Type(String name) {
            this.name = name;
        }
    }

    @Column(name = "sub_type", nullable = false)
//    @Enumerated(EnumType.STRING)
    MultipleChoicesQuestion.Type type;
    
    @OrderBy("orderIndex")
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Fetch(value = FetchMode.SUBSELECT)
    @JoinColumn(name = "question_id", referencedColumnName = "id")
    List<Choice> choices;

    @Setter
    @Getter
    @Convert(converter = MapConverter.class)
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "image_base64_strings", columnDefinition = "mediumblob")
    Map<String, String> imageBase64Strings;
}
