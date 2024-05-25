package indi.etern.checkIn.entities.question.impl.group;

import indi.etern.checkIn.entities.convertor.MapConverter;
import indi.etern.checkIn.entities.question.interfaces.ImagesWith;
import indi.etern.checkIn.entities.question.interfaces.MultiPartitionableQuestion;
import indi.etern.checkIn.entities.question.interfaces.Partition;
import indi.etern.checkIn.entities.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.util.*;

@Getter
@Entity
public class QuestionGroup extends MultiPartitionableQuestion implements ImagesWith {
    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE, CascadeType.DETACH}, fetch = FetchType.EAGER)
    @Fetch(value = FetchMode.SUBSELECT)
    @JoinTable(name = "question_group_list",
            joinColumns = @JoinColumn(name = "question_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "group_id", referencedColumnName = "id"))
    protected Set<MultiPartitionableQuestion> questionList;

    protected QuestionGroup() {
    }

    public QuestionGroup(String id, String questionContent, Set<Partition> partitions, User author, Set<MultiPartitionableQuestion> questionSet) {
        this.content = questionContent;
        this.partitions = partitions;
        this.author = author;
        this.questionList = questionSet;
        this.id = id;
    }

    public QuestionGroup(String id, String questionContent, Set<Partition> partitions, User author) {
        this.content = questionContent;
        this.partitions = partitions;
        this.author = author;
        this.questionList = new HashSet<>();
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
        private Set<MultiPartitionableQuestion> questions;

        public Builder() {
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

        public Builder addQuestion(MultiPartitionableQuestion question) {
            questions.add(question);
            return this;
        }

        public Builder setId(String id) {
            this.id = id;
            return this;
        }

        public QuestionGroup build() {
            if (id == null) id = UUID.randomUUID().toString();
            if (questions.isEmpty())
                return new QuestionGroup(id, content, partitions, author, questions);
            else
                return new QuestionGroup(id, content, partitions, author);
        }

    }
}