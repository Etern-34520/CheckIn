package indi.etern.checkIn.entities.question.impl.multipleQuestion;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import indi.etern.checkIn.entities.question.interfaces.MultiPartitionableQuestion;
import indi.etern.checkIn.entities.question.interfaces.multipleChoice.Choice;
import indi.etern.checkIn.entities.serializer.ExamQuestionSerializer;
import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.util.List;

@JsonSerialize(using = ExamQuestionSerializer.class)
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Getter
public class MultipleChoiceQuestion extends MultiPartitionableQuestion {
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Fetch(value = FetchMode.SUBSELECT)
    @JoinColumn(name = "question_id", referencedColumnName = "id")
    List<Choice> choices;
}
