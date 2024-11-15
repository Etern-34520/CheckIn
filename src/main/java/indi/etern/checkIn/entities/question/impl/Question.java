package indi.etern.checkIn.entities.question.impl;

import indi.etern.checkIn.entities.BaseEntity;
import indi.etern.checkIn.entities.convertor.MapConverter;
import indi.etern.checkIn.entities.linkUtils.LinkSource;
import indi.etern.checkIn.entities.linkUtils.Link;
import indi.etern.checkIn.entities.linkUtils.impl.QuestionLinkImpl;
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
@Embeddable
public class Question implements LinkSource<QuestionLinkImpl<?>>, BaseEntity<String> {
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss");
    @Column(name = "content")
    protected String content;

//    protected int hashcode;
    
    @JoinColumn(name = "AUTHOR_QQNUMBER")
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.REFRESH}, fetch = FetchType.EAGER)
    @NotFound(action = NotFoundAction.IGNORE)
    protected User author = null;// = User.exampleOfName("unknown");
    
    @Column(name = "LAST_MODIFIED_TIME")
    protected LocalDateTime lastModifiedTime;
    
    @Getter
    @Setter
    protected boolean enabled = false;
    
    @Getter
    @JoinTable(name = "upvoters_questions_mapping",
            joinColumns = @JoinColumn(name = "question_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "user_id", referencedColumnName = "qqNumber"))
    @ManyToMany(cascade = {
            CascadeType.REFRESH
    }, fetch = FetchType.EAGER)
    protected Set<User> upVoters = new HashSet<>();
    
    
    @Getter
    @ManyToMany(cascade = {
            CascadeType.REFRESH
    }, fetch = FetchType.EAGER)
    @JoinTable(name = "downvoters_questions_mapping",
            joinColumns = @JoinColumn(name = "question_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "user_id", referencedColumnName = "qqNumber"))
    protected Set<User> downVoters = new HashSet<>();
    
    @OneToOne(fetch = FetchType.EAGER,cascade = CascadeType.ALL,orphanRemoval = true)
    @JoinColumn(name = "id", referencedColumnName = "id")
    protected QuestionLinkImpl<?> linkWrapper;
    
    @Id
    @Column(name = "id")
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
        ((Link<Question, ?>) linkWrapper).setSource(this);
    }
}
