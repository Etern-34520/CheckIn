package indi.etern.checkIn.entities.question.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import indi.etern.checkIn.entities.BaseEntity;
import indi.etern.checkIn.entities.converter.MapConverter;
import indi.etern.checkIn.entities.linkUtils.LinkSource;
import indi.etern.checkIn.entities.linkUtils.impl.QuestionLinkImpl;
import indi.etern.checkIn.entities.question.statistic.QuestionStatistic;
import indi.etern.checkIn.entities.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Getter
@Entity
@Table(name = "questions")
public class Question implements LinkSource<QuestionLinkImpl<?>>, BaseEntity<String> {
//    public static final Example<Question> NOT_SUB_QUESTION_EXAMPLE;
//    static {
//        final Question probe = new Question();
//        probe.setLinkWrapper(new ToPartitionsLink());
//        NOT_SUB_QUESTION_EXAMPLE = Example.of(probe);
//    }
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss");
    @Column(name = "content",columnDefinition = "text")
    protected String content;

//    protected int hashcode;
    
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.REFRESH}, fetch = FetchType.EAGER)
    @JoinColumn(name = "author_qqnumber", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    @NotFound(action = NotFoundAction.IGNORE)
    @JsonIgnore
    protected User author = null;// = User.exampleOfName("unknown");
    
    @Column(name = "last_modified_time")
    protected LocalDateTime lastModifiedTime;
    
    @OneToOne(mappedBy = "question", orphanRemoval = true)
    @JsonIgnore
    @NotFound(action = NotFoundAction.IGNORE)
    protected QuestionStatistic questionStatistic;
    
    @Getter
    @Setter
    protected boolean enabled = false;
    
    @Getter
    @JoinTable(name = "upvoters_questions_mapping",
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT),
            inverseForeignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT),
            joinColumns = @JoinColumn(name = "question_id", referencedColumnName = "id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT)),
            inverseJoinColumns = @JoinColumn(name = "user_id", referencedColumnName = "qqNumber", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT)))
    @ManyToMany(cascade = {
            CascadeType.REFRESH
    }, fetch = FetchType.EAGER)
    @JsonIgnore
    protected Set<User> upVoters = new HashSet<>();
    
    @Getter
    @ManyToMany(cascade = {
            CascadeType.REFRESH
    }, fetch = FetchType.EAGER)
    @JoinTable(name = "downvoters_questions_mapping",
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT),
            inverseForeignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT),
            joinColumns = @JoinColumn(name = "question_id", referencedColumnName = "id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT)),
            inverseJoinColumns = @JoinColumn(name = "user_id", referencedColumnName = "qqNumber", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT)))
    @JsonIgnore
    protected Set<User> downVoters = new HashSet<>();
    
    @OneToOne(fetch = FetchType.EAGER,cascade = CascadeType.ALL,orphanRemoval = true)
    @JoinColumn(name = "id", referencedColumnName = "id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    @JsonIgnore
    protected QuestionLinkImpl<?> linkWrapper;
    
    @Id
    @Column(columnDefinition = "char(36)")
    @JsonIgnore
    protected String id;
    @Setter
    @Convert(converter = MapConverter.class)
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "image_base64_strings", columnDefinition = "mediumblob")
    Map<String, String> imageBase64Strings;
    
    protected Question() {
        lastModifiedTime = LocalDateTime.now();
    }
    
    public void initId() {
        id = UUID.randomUUID().toString();
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
    public QuestionLinkImpl<?> getLinkWrapper() {
        return linkWrapper;
    }
    
    @Override
    public void setLinkWrapper(QuestionLinkImpl<?> linkWrapper) {
        this.linkWrapper = linkWrapper;
        linkWrapper.setSource(this);
    }
    
    /**
     * for Jackson
     * */
    public String getType() {return getClass().getSimpleName();}
}