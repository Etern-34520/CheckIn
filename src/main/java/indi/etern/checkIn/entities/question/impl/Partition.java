package indi.etern.checkIn.entities.question.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import indi.etern.checkIn.entities.BaseEntity;
import indi.etern.checkIn.entities.linkUtils.LinkTarget;
import indi.etern.checkIn.entities.linkUtils.impl.ToPartitionsLink;
import indi.etern.checkIn.entities.question.impl.group.QuestionGroup;
import indi.etern.checkIn.service.dao.PartitionService;
import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.io.Serializable;
import java.util.*;

@Getter
@Entity
@Table(name = "partitions")
public class Partition implements Serializable, LinkTarget, BaseEntity<String> {
    private static final Map<String, Partition> partitionNameMap = new HashMap<>();
    private static final Map<String, Partition> partitionIdMap = new HashMap<>();
    
    @Getter
    @Id
    @Column(columnDefinition = "char(36)")
    private String id;
    
    @Column(name = "name", unique = true, nullable = false)
    String name;
    
    @JsonIgnore
    @ManyToMany(mappedBy = "targets")
    @Fetch(value = FetchMode.SUBSELECT)
    Set<ToPartitionsLink> questionLinks;
    
    
    protected Partition() {
    }
    
    private Partition(String string) {
        name = string;
        questionLinks = new HashSet<>();
//        id = Math.toIntExact(System.currentTimeMillis()/100000000);
        id = UUID.randomUUID().toString();
    }
    
    public static Partition ofName(String string) {
        if (partitionNameMap.get(string) == null) {
            final Optional<Partition> optionalPartition = PartitionService.singletonInstance.findByName(string);
            if (optionalPartition.isPresent()) {
                partitionNameMap.put(string, optionalPartition.get());
                partitionIdMap.put(optionalPartition.get().getId(), optionalPartition.get());
            } else {
                Partition partition = new Partition(string);
                partitionNameMap.put(string, partition);
                partitionIdMap.put(partition.getId(), partition);
            }
        }
        return partitionNameMap.get(string);
    }
    
    public static Partition ofId(String partitionId) {
        if (partitionIdMap.get(partitionId) == null) {
            final Optional<Partition> optionalPartition = PartitionService.singletonInstance.findById(partitionId);
            if (optionalPartition.isPresent()) {
                partitionNameMap.put(optionalPartition.get().getName(), optionalPartition.get());
                partitionIdMap.put(optionalPartition.get().getId(), optionalPartition.get());
            } else {
                return null;
            }
        }
        return partitionIdMap.get(partitionId);
    }
    
    public void setName(String newName) {
        name = newName;
        partitionNameMap.remove(name);
        partitionNameMap.put(name, this);
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
        return name.hashCode();
    }
    
    @Override
    public boolean equals(Object object) {
        if (object instanceof Partition partition) {
            return partition.name.equals(this.name);
        } else {
            return false;
        }
    }
}