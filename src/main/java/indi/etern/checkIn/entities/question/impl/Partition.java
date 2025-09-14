package indi.etern.checkIn.entities.question.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import indi.etern.checkIn.entities.BaseEntity;
import indi.etern.checkIn.entities.linkUtils.LinkTarget;
import indi.etern.checkIn.entities.linkUtils.impl.ToPartitionsLink;
import indi.etern.checkIn.service.dao.PartitionService;
import indi.etern.checkIn.utils.UUIDv7;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Entity
@Table(name = "partitions")
public class Partition implements Serializable, LinkTarget, BaseEntity<String> {
    @Getter
    @Id
    @Column(columnDefinition = "char(36)")
    private String id;
    
    @Setter
    @Getter
    @Column(name = "name", unique = true, nullable = false)
    String name;
    
    @Getter
    @JsonIgnore
    @ManyToMany(mappedBy = "targets", fetch = FetchType.EAGER)
    @Fetch(value = FetchMode.SUBSELECT)
    Set<ToPartitionsLink> questionLinks;
    
    protected Partition() {
    }
    
    private Partition(String string) {
        name = string;
        questionLinks = new HashSet<>();
        id = UUIDv7.randomUUID().toString();
    }
    
    public static Partition ofName(String string) {
        final Optional<Partition> optionalPartition = PartitionService.singletonInstance.findByName(string);
        return optionalPartition.orElseGet(() -> new Partition(string));
    }
    
    public static Partition ofId(String partitionId) {
        final Optional<Partition> optionalPartition = PartitionService.singletonInstance.findById(partitionId);
        return optionalPartition.orElse(null);
    }
    
    @Override
    public String toString() {
        return name + "(" + id + ")";
    }
    
    @JsonProperty("enabledQuestionCount")
    public int getEnabledQuestionCount() {
        return questionLinks.stream().filter(questionLink -> questionLink.getSource().isEnabled())
                .mapToInt(questionLink -> questionLink.getSource() instanceof QuestionGroup questionGroup ? questionGroup.getQuestionLinks().size() : 1)
                .sum();
    }
    
    @JsonProperty("questionAmount")
    public int getQuestionAmount() {
        return questionLinks.size();
    }
    
    @Transient
    @JsonIgnore
    volatile private Set<Question> questionSet;
    
    @Transient
    @JsonIgnore
    volatile private boolean enabledQuestionMayDirtied = true;
    
    @PostPersist
    @PostUpdate
    void markDirty() {
        enabledQuestionMayDirtied = true;
    }
    
    private void updateEnabledInternal() {
        Set<Question> questionList = new HashSet<>();
        for (ToPartitionsLink questionLink : questionLinks) {
            Question question = questionLink.getSource();
            if (question.isEnabled()) {
                questionList.add(question);
            }
        }
        questionSet = questionList;
    }
    
    @JsonIgnore
    public Set<Question> getEnabledQuestionsSet() {
        if (enabledQuestionMayDirtied) {
            synchronized (this.id + "-enabledQuestions") {
                if (enabledQuestionMayDirtied) {
                    updateEnabledInternal();
                    enabledQuestionMayDirtied = false;
                }
                return questionSet;
            }
        } else {
            return questionSet;
        }
    }
    
    @Override
    public int hashCode() {
        return id.hashCode();
    }
    
    @Override
    public boolean equals(Object object) {
        if (object instanceof Partition partition) {
            return partition.id.equals(this.id);
        } else {
            return false;
        }
    }
}