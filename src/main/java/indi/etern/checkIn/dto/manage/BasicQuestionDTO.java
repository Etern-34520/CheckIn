package indi.etern.checkIn.dto.manage;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import indi.etern.checkIn.deserializer.LocalDateTimeDeserializer;
import indi.etern.checkIn.entities.linkUtils.impl.QuestionLinkImpl;
import indi.etern.checkIn.entities.linkUtils.impl.ToPartitionsLink;
import indi.etern.checkIn.entities.question.impl.Partition;
import indi.etern.checkIn.entities.question.impl.Question;
import indi.etern.checkIn.entities.user.User;
import indi.etern.checkIn.serializer.LocalDateTimeSerializer;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BasicQuestionDTO {
    protected String id;
    protected String content;
    protected Boolean enabled;
    protected String type;
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    protected LocalDateTime lastModifiedTime;
    protected List<String> partitionIds = null;
    @Setter
    protected boolean showError = false;
    @Setter
    protected boolean showWarning = false;
    protected Map<String, IssueDTO> errors = new HashMap<>();
    protected Map<String, IssueDTO> warnings = new HashMap<>();
    protected Long authorQQ;
    
    public BasicQuestionDTO(Question question) {
        id = question.getId();
        content = question.getContent();
        enabled = question.isEnabled();
        type = question.getType();
        lastModifiedTime = question.getLastModifiedTime();
        final QuestionLinkImpl<?> linkWrapper = question.getLinkWrapper();
        if (linkWrapper instanceof ToPartitionsLink toPartitionsLink) {
            partitionIds = toPartitionsLink.getTargets().stream().map(Partition::getId).toList();
        }
        authorQQ = question.getAuthor().getQQNumber();
    }
    
    public void inheritFrom(Question question) {
        if (enabled == null) {
            enabled = question.isEnabled();
        }
        if (content == null) {
            content = question.getContent();
        }
        if (partitionIds == null) {
            final QuestionLinkImpl<?> linkWrapper = question.getLinkWrapper();
            if (linkWrapper instanceof ToPartitionsLink toPartitionsLink) {
                partitionIds = toPartitionsLink.getTargets().stream().map(Partition::getId).toList();
            }
        }
        final User author = question.getAuthor();
        if (authorQQ == null && author != null) {
            authorQQ = author.getQQNumber();
        } else if (authorQQ != null && authorQQ == 0L) {
            authorQQ = null;
        }
    }
}