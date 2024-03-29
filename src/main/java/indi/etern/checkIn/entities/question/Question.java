package indi.etern.checkIn.entities.question;

import indi.etern.checkIn.entities.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Getter
@MappedSuperclass
@Table(name = "MULTI_PARTITIONABLE_QUESTIONS")
public abstract class Question implements Serializable {
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss");
    @Column(name = "content")
    protected String content;
    
//    protected int hashcode;
    
    @JoinColumn(name = "AUTHOR_QQNUMBER")
    @ManyToOne(cascade = {CascadeType.PERSIST,CascadeType.DETACH,CascadeType.REFRESH}, fetch = FetchType.EAGER)
    @NotFound(action = NotFoundAction.IGNORE)
    protected User author = null;// = User.exampleOfName("unknown");
    
    @Getter
    @Column(name = "LAST_EDIT_TIME")
    protected LocalDateTime lastEditTime;

    protected Question() {
        lastEditTime = LocalDateTime.now();
    }
    
    @Id
    @Column(name = "id")
    protected String id;
    
    public void initId(){
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
    
    abstract public String toJsonData();

    @SuppressWarnings("unused")
    public String getLastEditTimeString() {
        return dateTimeFormatter.format(lastEditTime);
    }
}
