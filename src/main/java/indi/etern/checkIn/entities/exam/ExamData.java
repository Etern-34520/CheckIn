package indi.etern.checkIn.entities.exam;

import com.fasterxml.jackson.annotation.JsonIgnore;
import indi.etern.checkIn.entities.BaseEntity;
import indi.etern.checkIn.entities.convertor.ListJsonConvertor;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PUBLIC)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "exam_data")
public class ExamData implements BaseEntity<String> {
    public enum Status {
        DURING,ENDED,LAPSED
    }
    
    @Id
    String id;
    long qqNumber;
    
    @JsonIgnore
    Status status;
    
    @Convert(converter = ListJsonConvertor.class)
    List<Integer> requiredPartitionIds;
    
    @Convert(converter = ListJsonConvertor.class)
    List<Integer> selectedPartitionIds;
    
    @Convert(converter = ListJsonConvertor.class)
    List<String> questionIds;
}