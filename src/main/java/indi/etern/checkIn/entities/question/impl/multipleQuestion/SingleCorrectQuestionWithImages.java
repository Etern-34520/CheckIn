package indi.etern.checkIn.entities.question.impl.multipleQuestion;

import indi.etern.checkIn.entities.convertor.StringListConverter;
import indi.etern.checkIn.entities.question.interfaces.ImagesWith;
import indi.etern.checkIn.entities.question.interfaces.Partition;
import indi.etern.checkIn.entities.question.interfaces.multipleChoice.Choice;
import indi.etern.checkIn.entities.user.User;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;

import java.util.List;
import java.util.Set;

@Entity
public class SingleCorrectQuestionWithImages extends SingleCorrectQuestion implements ImagesWith {
    @Convert(converter = StringListConverter.class)
    List<String> imagePathStrings;
    
    public SingleCorrectQuestionWithImages(String questionContent, List<Choice> choices, Set<Partition> partitions, User author) {
        super(questionContent, choices, partitions, author);
        this.imagePathStrings = imagePathStrings;
    }
    
    public void setImagePathStrings(List<String> imagePathStrings) {
        this.imagePathStrings = imagePathStrings;
    }
    
    protected SingleCorrectQuestionWithImages() {}
    
    @Override
    public List<String> getImagePathStrings() {
        return imagePathStrings;
    }
}
