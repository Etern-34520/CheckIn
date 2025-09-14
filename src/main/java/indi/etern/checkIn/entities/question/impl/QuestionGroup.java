package indi.etern.checkIn.entities.question.impl;

import indi.etern.checkIn.entities.linkUtils.LinkSource;
import indi.etern.checkIn.entities.linkUtils.LinkTarget;
import indi.etern.checkIn.entities.linkUtils.impl.QuestionLinkImpl;
import indi.etern.checkIn.entities.linkUtils.impl.ToPartitionsLink;
import indi.etern.checkIn.entities.linkUtils.impl.ToQuestionGroupLink;
import indi.etern.checkIn.entities.question.interfaces.answer.Answerable;
import indi.etern.checkIn.entities.question.interfaces.answer.SingleQuestionAnswer;
import indi.etern.checkIn.entities.user.User;
import indi.etern.checkIn.throwable.entity.QuestionBuilderException;
import indi.etern.checkIn.utils.UUIDv7;
import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.util.*;

@Getter
@Entity
public class QuestionGroup extends Question implements
        LinkTarget, LinkSource<QuestionLinkImpl<?>>, Answerable<List<SingleQuestionAnswer>> {
    
    public void addQuestion(Question question) {
        ToQuestionGroupLink questionLinkWrapper = (ToQuestionGroupLink) question.getLinkWrapper();
        questionLinkWrapper.setOrderIndex(questionLinks.size());
        questionLinks.add(questionLinkWrapper);
    }
    
    public void addQuestionLink(ToQuestionGroupLink link) {
        link.setOrderIndex(questionLinks.size());
        questionLinks.add(link);
    }
    
    @OneToMany(mappedBy = "target", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @Fetch(value = FetchMode.SUBSELECT)
    @OrderBy("orderIndex")
    protected Set<ToQuestionGroupLink> questionLinks;
    
    protected QuestionGroup() {
    }
    
    public QuestionGroup(String id, String questionContent, User author, Set<Question> questionSet, String explanation) {
        this.content = questionContent;
        this.explanation = explanation;
        this.author = author;
        this.questionLinks = new HashSet<>();
        int index = 0;
        for (Question question : questionSet) {
            ToQuestionGroupLink questionLinkWrapper = (ToQuestionGroupLink) question.getLinkWrapper();
            questionLinkWrapper.setOrderIndex(index);
            questionLinks.add(questionLinkWrapper);
            index++;
        }
        this.id = id;
    }
    
    public QuestionGroup(String id, String questionContent, User author, String explanation) {
        this.content = questionContent;
        this.explanation = explanation;
        this.author = author;
        this.questionLinks = new HashSet<>();
        this.id = id;
    }
    
/*
    @Setter
    @Convert(converter = MapConverter.class)
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "image_base64_strings", columnDefinition = "mediumblob")
    Map<String, String> imageBase64Strings;
*/
    
    @SuppressWarnings("UnusedReturnValue")
    @Getter
    public static class Builder {
        
        private String content;
        private final Set<Partition> partitions = new HashSet<>();
        private User author;
        private String id;
        private final Set<Question> questions = new HashSet<>();
        private boolean enabled = false;
        
        @Getter
        final Map<String, String> imageBase64Strings = new LinkedHashMap<>();
        private String explanation;

        public Builder() {
        }
        
        public static Builder from(QuestionGroup previousQuestionGroup) {
            final Builder builder = new Builder().setContent(previousQuestionGroup.getContent())
                    .setAuthor(previousQuestionGroup.getAuthor())
                    .setEnable(previousQuestionGroup.isEnabled())
                    .setId(previousQuestionGroup.getId())
                    .setExplanation(previousQuestionGroup.getExplanation());
            builder.getPartitions().clear();
            builder.getPartitions().addAll(((ToPartitionsLink) previousQuestionGroup.getLinkWrapper()).getTargets());
            builder.getQuestions().clear();
            builder.getQuestions().addAll(previousQuestionGroup.getQuestionLinks().stream().map(ToQuestionGroupLink::getSource).toList());
            return builder;
        }
        
        private Builder setEnable(boolean enabled) {
            this.enabled = enabled;
            return this;
        }
        
        public Builder setContent(String content) {
            this.content = content;
            return this;
        }
        
        public Builder addPartition(Partition partition) {
            partitions.add(partition);
            return this;
        }
        
        public Builder setAuthor(User author) {
            this.author = author;
            return this;
        }
        
        public Builder setId(String id) {
            this.id = id;
            return this;
        }
        
        public QuestionGroup build() {
            if (id == null) id = UUIDv7.randomUUID().toString();
            QuestionGroup questionGroup;
            ToPartitionsLink link;
            if (partitions.isEmpty()) {
                throw new QuestionBuilderException("link to partitions is not set");
            } else {
                link = new ToPartitionsLink();
                Set<Partition> targets = link.getTargets();
                targets.clear();
                targets.addAll(partitions);
            }
            //TODO test
            partitions.forEach(partition -> partition.getQuestionLinks().add(link));
            if (questions.isEmpty()) {
                questionGroup = new QuestionGroup(id, content, author, explanation);
            } else {
                questionGroup = new QuestionGroup(id, content, author, questions, explanation);
            }
            if (!imageBase64Strings.isEmpty()) {
                questionGroup.setImages(imageBase64Strings);
            }
            questionGroup.setEnabled(enabled);
            questionGroup.setLinkWrapper(link);
            return questionGroup;
        }
        
        public Builder setEnabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }
        
        public Builder addBase64Image(String name, String base64String) {
            imageBase64Strings.put(name, base64String);
            return this;
        }

        public Builder setExplanation(String explanation) {
            this.explanation = explanation;
            return this;
        }
    }
    
    @Override
    public QuestionGroupAnswer newAnswerFrom(List<SingleQuestionAnswer> singleQuestionAnswers) {
        final QuestionGroupAnswer questionGroupAnswer = new QuestionGroupAnswer();
        questionGroupAnswer.initFromSource(this,singleQuestionAnswers);
        return questionGroupAnswer;
    }
}