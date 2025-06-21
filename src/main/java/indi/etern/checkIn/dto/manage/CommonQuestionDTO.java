package indi.etern.checkIn.dto.manage;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import indi.etern.checkIn.entities.question.impl.Question;
import indi.etern.checkIn.entities.user.User;
import indi.etern.checkIn.service.dao.QuestionStatisticService;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Getter
@EqualsAndHashCode(callSuper = false)
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(QuestionGroupDTO.class),
        @JsonSubTypes.Type(MultipleChoicesQuestionDTO.class),
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class CommonQuestionDTO extends BasicQuestionDTO {
    protected List<ImageDTO> images;
    protected List<Long> upVoters;
    protected List<Long> downVoters;
//    @JsonDeserialize(using = AuthorQQDeserializer.class)
    protected Long authorQQ;
    protected StatisticDTO statistic = null;
    
    public CommonQuestionDTO(Question question) {
        super(question);
        final Map<String, String> images1 = question.getImages();
        if (images1 != null) {
            images = images1.entrySet().stream().map(
                    entry -> ImageDTO.builder()
                            .name(entry.getKey())
                            .url(entry.getValue())
                            .size((int) (entry.getValue().length() * 0.75))
                            .build()
            ).toList();
        } else {
            images = new ArrayList<>();
        }
        upVoters = question.getUpVoters().stream().map(User::getQQNumber).toList();
        downVoters = question.getDownVoters().stream().map(User::getQQNumber).toList();
        final User author = question.getAuthor();
        if (author != null) {
            authorQQ = author.getQQNumber();
        }
        QuestionStatisticService.singletonInstance.findById(question.getId()).ifPresent(questionStatistic -> {
            statistic = new StatisticDTO(questionStatistic);
        });
    }
    
    // for Jackson
    @SuppressWarnings("unused")
    public void setAuthorQQ(Long authorQQ) {
        this.authorQQ = Objects.requireNonNullElse(authorQQ, 0L);
    }
    
    public void inheritFrom(Question question) {
        super.inheritFrom(question);
        final Map<String, String> images1 = question.getImages();
        if (images == null && images1 != null) {
            images = images1.entrySet().stream().map(entry ->
                    ImageDTO.builder()
                            .name(entry.getKey())
                            .url(entry.getValue())
                            .size((int) (entry.getValue().length() * 0.75))
                            .build()
            ).toList();
        } else if (images == null) {
            images = new ArrayList<>(0);
        }
        
        upVoters = question.getUpVoters().stream().map(User::getQQNumber).toList();
        downVoters = question.getDownVoters().stream().map(User::getQQNumber).toList();
        
        final User author = question.getAuthor();
        if (authorQQ == null && author != null) {
            authorQQ = author.getQQNumber();
        } else if (authorQQ != null && authorQQ == 0L) {
            authorQQ = null;
        }
        
        if (statistic == null) {
            QuestionStatisticService.singletonInstance.findById(question.getId()).ifPresent(questionStatistic -> {
                statistic = new StatisticDTO(questionStatistic);
            });
        }
    }
}