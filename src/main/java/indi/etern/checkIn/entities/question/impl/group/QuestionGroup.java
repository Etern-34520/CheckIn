package indi.etern.checkIn.entities.question.impl.group;

import indi.etern.checkIn.entities.convertor.MapConverter;
import indi.etern.checkIn.entities.linkUtils.LinkSource;
import indi.etern.checkIn.entities.linkUtils.LinkTarget;
import indi.etern.checkIn.entities.linkUtils.impl.QuestionLinkImpl;
import indi.etern.checkIn.entities.linkUtils.impl.ToPartitionLink;
import indi.etern.checkIn.entities.linkUtils.impl.ToQuestionGroupLink;
import indi.etern.checkIn.entities.question.impl.Partition;
import indi.etern.checkIn.entities.question.impl.Question;
import indi.etern.checkIn.entities.question.impl.question.QuestionException;
import indi.etern.checkIn.entities.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.util.*;

@Getter
@Entity
public class QuestionGroup extends Question implements LinkTarget, LinkSource<QuestionLinkImpl<?>> {
    public Set<ToQuestionGroupLink> getQuestionLinks() {
        return questionLinks;
    }

    public void addQuestion(Question question) {
        ToQuestionGroupLink questionLinkWrapper = (ToQuestionGroupLink) question.getLinkWrapper();
        questionLinkWrapper.setOrderIndex(questionLinks.size());
        questionLinks.add(questionLinkWrapper);
    }

    public void addQuestionLink(ToQuestionGroupLink link) {
        link.setOrderIndex(questionLinks.size());
        questionLinks.add(link);
    }

    @OneToMany(mappedBy = "target",fetch = FetchType.EAGER)
    @Fetch(value = FetchMode.SUBSELECT)
    @OrderBy("orderIndex")
//    @JoinTable(name = "question_group_list",
//            joinColumns = @JoinColumn(name = "question_id", referencedColumnName = "id"),
//            inverseJoinColumns = @JoinColumn(name = "group_id", referencedColumnName = "id"))
    protected Set<ToQuestionGroupLink> questionLinks;

    protected QuestionGroup() {
    }

    public QuestionGroup(String id, String questionContent/*, Set<Partition> partitions*/, User author, Set<Question> questionSet) {
        this.content = questionContent;
//        this.partitions = partitions;
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

    public QuestionGroup(String id, String questionContent/*, Set<Partition> partitions*/, User author) {
        this.content = questionContent;
//        this.partitions = partitions;
        this.author = author;
        this.questionLinks = new HashSet<>();
        this.id = id;
    }

    @Setter
    @Convert(converter = MapConverter.class)
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "image_base64_strings", columnDefinition = "mediumblob")
    Map<String, String> imageBase64Strings;

    @Getter
    public static class Builder {

        private String content;
        private final Set<Partition> partitions = new HashSet<>();
        private User author;
        private String id;
        private final Set<Question> questions = new HashSet<>();
        private Boolean enabled;
        @Getter
        final Map<String, String> imageBase64Strings = new LinkedHashMap<>();

        public Builder() {
        }

        public static Builder from(QuestionGroup previousQuestionGroup) {
            final QuestionGroup.Builder builder = new Builder().setContent(previousQuestionGroup.getContent())
                    .setAuthor(previousQuestionGroup.getAuthor())
                    .setEnable(previousQuestionGroup.isEnabled())
                    .setId(previousQuestionGroup.getId());
            builder.getPartitions().clear();
            builder.getPartitions().addAll(((ToPartitionLink)previousQuestionGroup.getLinkWrapper()).getTargets());
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
            if (id == null) id = UUID.randomUUID().toString();
            QuestionGroup questionGroup;
            ToPartitionLink link;
            if (partitions.isEmpty()) {
                throw new QuestionException("link to partitions is not set");
            } else {
                link = new ToPartitionLink();
                Set<Partition> targets = link.getTargets();
                targets.clear();
                targets.addAll(partitions);
            }
            if (questions.isEmpty()) {
                questionGroup = new QuestionGroup(id, content/*, partitions*/, author);
            } else {
                questionGroup = new QuestionGroup(id, content/*, partitions*/, author, questions);
            }
            if (!imageBase64Strings.isEmpty()) {
                questionGroup.setImageBase64Strings(imageBase64Strings);//FIXME
            }
            if (enabled!=null) {
                questionGroup.setEnabled(enabled);
            }
            questionGroup.setLinkWrapper(link);
            return questionGroup;
        }

        public Builder setEnabled(Boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public QuestionGroup.Builder addBase64Image(String name, String base64String) {
            imageBase64Strings.put(name, base64String);
            return this;
        }
    }
}