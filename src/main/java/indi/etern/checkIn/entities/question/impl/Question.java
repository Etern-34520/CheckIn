package indi.etern.checkIn.entities.question.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import indi.etern.checkIn.entities.BaseEntity;
import indi.etern.checkIn.entities.converter.MapConverter;
import indi.etern.checkIn.entities.linkUtils.LinkSource;
import indi.etern.checkIn.entities.linkUtils.impl.QuestionLinkImpl;
import indi.etern.checkIn.entities.question.statistic.QuestionStatistic;
import indi.etern.checkIn.entities.user.User;
import indi.etern.checkIn.service.dao.verify.ValidationResult;
import indi.etern.checkIn.utils.UUIDv7;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Entity
@Table(name = "questions")
@NamedEntityGraphs({
        @NamedEntityGraph(name = "Question.LinkWrapper", attributeNodes = {@NamedAttributeNode("linkWrapper")})
})
public class Question implements LinkSource<QuestionLinkImpl<?>>, BaseEntity<String> {
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss");
    @Id
    @Getter
    @Column(columnDefinition = "char(36)")
    @JsonIgnore
    String id;

    @Getter
    @Column(name = "content", columnDefinition = "text")
    String content;

    @Getter
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.REFRESH}, fetch = FetchType.EAGER)
    @JoinColumn(name = "author_qqnumber", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    @NotFound(action = NotFoundAction.IGNORE)
    @JsonIgnore
    User author = null;

    @Getter
    @Column(name = "last_modified_time")
    LocalDateTime lastModifiedTime;

    @Getter
    @Setter
    boolean enabled = false;

    @Getter
    @Setter
    boolean unsafeXss = false;

    @Getter
    @JoinTable(name = "upvoters_questions_mapping",
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT),
            inverseForeignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT),
            joinColumns = @JoinColumn(name = "question_id", referencedColumnName = "id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT)),
            inverseJoinColumns = @JoinColumn(name = "user_id", referencedColumnName = "qqnumber", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT)))
    @ManyToMany(cascade = {
            CascadeType.DETACH
    }, fetch = FetchType.EAGER)
    @JsonIgnore
    Set<User> upVoters = new HashSet<>();

    @Getter
    @OneToOne(mappedBy = "question")
    QuestionStatistic questionStatistic;

    @Getter
    @ManyToMany(cascade = {
            CascadeType.DETACH
    }, fetch = FetchType.EAGER)
    @JoinTable(name = "downvoters_questions_mapping",
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT),
            inverseForeignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT),
            joinColumns = @JoinColumn(name = "question_id", referencedColumnName = "id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT)),
            inverseJoinColumns = @JoinColumn(name = "user_id", referencedColumnName = "qqnumber", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT)))
    @JsonIgnore
    Set<User> downVoters = new HashSet<>();

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "id", referencedColumnName = "id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    @JsonIgnore
    QuestionLinkImpl<?> linkWrapper;

    public QuestionLinkImpl<?> getLinkWrapper() {
        return (QuestionLinkImpl<?>) Hibernate.unproxy(linkWrapper);
    }

    /**
     * Notice: data saved as base64
     * Invalid Data Size: Mediumblob:16MB -> Base64:9MB(around)
     */
    @Getter
    @Setter
    @Convert(converter = MapConverter.class)
    @Column(name = "image_base64_strings", columnDefinition = "mediumblob")
    Map<String, String> images;

    @Getter
    @Setter
    @Column(name = "verification_digest", columnDefinition = "char(32)")
    String verificationDigest;

    @Getter
    @Setter
    ValidationResult validationResult;

    @Getter
    @Setter
    @JsonIgnore
    @Column(name = "explanation", columnDefinition = "text")
    String explanation;

    protected Question() {
        lastModifiedTime = LocalDateTime.now();
    }

    public void initId() {
        id = UUIDv7.randomUUID().toString();
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof Question question) {
            return question.id.equals(id);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + id + ")";
    }

    @SuppressWarnings("unused")
    public String getLastModifiedTimeString() {
        return dateTimeFormatter.format(lastModifiedTime);
    }

    @Override
    public void setLinkWrapper(QuestionLinkImpl<?> linkWrapper) {
        this.linkWrapper = linkWrapper;
        linkWrapper.setSource(this);
    }

    /**
     * for Jackson
     */
    public String getType() {
        return getClass().getSimpleName();
    }
}