package indi.etern.checkIn.entities.question.statistic;

import com.fasterxml.jackson.annotation.JsonIgnore;
import indi.etern.checkIn.entities.BaseEntity;
import indi.etern.checkIn.entities.exam.ExamData;
import indi.etern.checkIn.entities.question.impl.Question;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.util.List;

@Getter
@Builder
@Entity
@Table(name = "question_statistics")
@AllArgsConstructor
@NoArgsConstructor
public class QuestionStatistic implements BaseEntity<String> {
    @Id
    @JoinTable(
            name = "questions",
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT),
            inverseForeignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT),
            joinColumns = @JoinColumn(name = "id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT)),
            inverseJoinColumns = @JoinColumn(name = "id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT)))
    @Column(columnDefinition = "char(36)")
    @JsonIgnore
    String id;
    
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
    @JoinColumn(name = "id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    @JsonIgnore
    Question question;
    
    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.REFRESH)
    @JoinTable(
            name = "statistic_exam_data_mapping",
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT),
            inverseForeignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT),
            joinColumns = @JoinColumn(name = "exam_id", referencedColumnName = "id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT)),
            inverseJoinColumns = @JoinColumn(name = "statistic_id", referencedColumnName = "id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT)))
    @Fetch(FetchMode.SUBSELECT)
    @JsonIgnore
    List<ExamData> drewExamData;
    int drewCount = 0;
    int submittedCount = 0;
    int correctCount = 0;
    int wrongCount = 0;
    
    public QuestionStatistic increaseDrewCount() {
        drewCount++;
        return this;
    }
    
    public QuestionStatistic increaseSubmittedCount() {
        submittedCount++;
        return this;
    }
    
    public QuestionStatistic increaseCorrectCount() {
        correctCount++;
        return this;
    }
    
    public QuestionStatistic increaseWrongCount() {
        wrongCount++;
        return this;
    }
}