package indi.etern.checkIn.entities.question.impl.multipleQuestion;

import indi.etern.checkIn.entities.convertor.MapConverter;
import indi.etern.checkIn.entities.question.interfaces.ImagesWith;
import indi.etern.checkIn.entities.question.interfaces.Partition;
import indi.etern.checkIn.entities.question.interfaces.multipleChoice.Choice;
import indi.etern.checkIn.entities.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Setter
@Getter
@Entity
public class SingleCorrectQuestionWithImages extends SingleCorrectQuestion implements ImagesWith {
    @Convert(converter = MapConverter.class)
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "image_base64_strings",columnDefinition = "mediumblob")
    Map<String,String> imageBase64Strings;
    
    public SingleCorrectQuestionWithImages(String questionContent, List<Choice> choices, Set<Partition> partitions, User author) {
        super(questionContent, choices, partitions, author);
    }
    
    protected SingleCorrectQuestionWithImages() {}
}
