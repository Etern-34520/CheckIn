package indi.etern.checkIn.entities.verification;

import indi.etern.checkIn.entities.convertor.ListJsonConvertor;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Entity
@AllArgsConstructor
@Builder
@Table(name = "verification_rules")
@Getter
public class VerificationRule {
    @Id
    String id;
    String objectName;
    @Convert(converter = ListJsonConvertor.class)
    List<String> trace;
    String verificationType;
    String level;
    @Column(name = "order_index")
    int index;
    @Convert(converter = ListJsonConvertor.class)
    @Column(name = "data_values")
    List<Object> values;
    String tipTemplate;
    
    protected VerificationRule() {
    }
}
