package indi.etern.checkIn.entities.setting.verification;

import indi.etern.checkIn.dto.manage.question.CommonQuestionDTO;
import indi.etern.checkIn.dto.manage.question.MultipleChoicesQuestionDTO;
import indi.etern.checkIn.dto.manage.question.QuestionGroupDTO;
import indi.etern.checkIn.entities.converter.ListJsonConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.List;

@Entity
@AllArgsConstructor
@Builder
@Table(name = "verification_rules")
@Getter
@EqualsAndHashCode(callSuper = false)
public class VerificationRule {
    @Id
    @Column(columnDefinition = "char(36)")
    String id;
    String objectName;
    @Convert(converter = ListJsonConverter.class)
    List<String> trace;
    String verificationType;
    String targetInputName;
    String level;
    @Column(name = "order_index")
    int index;
    @Convert(converter = ListJsonConverter.class)
    @Column(name = "data_values")
    List<Object> values;
    String tipTemplate;
    @Column(name = "ignore_missing_field")
    boolean ignoreMissingField;
    
    protected VerificationRule() {
    }
    
    public boolean isApplicable(CommonQuestionDTO commonQuestionDTO) {
        return (objectName.equals("MultipleChoicesQuestion")) && commonQuestionDTO instanceof MultipleChoicesQuestionDTO ||
                objectName.equals("QuestionGroup") && commonQuestionDTO instanceof QuestionGroupDTO;
    }
}