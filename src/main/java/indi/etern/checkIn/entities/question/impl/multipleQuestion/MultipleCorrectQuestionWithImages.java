package indi.etern.checkIn.entities.question.impl.multipleQuestion;

import indi.etern.checkIn.entities.convertor.StringListConverter;
import indi.etern.checkIn.entities.question.interfaces.ImagesWith;
import indi.etern.checkIn.entities.question.interfaces.Partition;
import indi.etern.checkIn.entities.question.interfaces.multipleChoice.Choice;
import indi.etern.checkIn.entities.user.User;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import lombok.Setter;

import java.util.List;
import java.util.Set;

@Setter
@Entity
public class MultipleCorrectQuestionWithImages extends MultipleCorrectQuestion implements ImagesWith {
    @Convert(converter = StringListConverter.class)
    List<String> imagePathStrings;
    
    public MultipleCorrectQuestionWithImages(String questionContent, List<Choice> choices, Set<Partition> partitions, User author) {
        super(questionContent, choices, partitions, author);
    }
    protected MultipleCorrectQuestionWithImages() {}
    @Override
    public List<String> getImagePathStrings() {
        return imagePathStrings;
    }
    
}
