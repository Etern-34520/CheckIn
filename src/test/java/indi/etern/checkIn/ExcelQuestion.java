package indi.etern.checkIn;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
public class ExcelQuestion {
    Integer count;//not used
    String content;
    String choiceJsonArray;
    String correctChoiceIndexJsonArray;
    Integer questionScoreType;//not used
    String resolution;//not used
    Integer questionType;//not used because of the auto-detection
    String authorName;//not used currently
}