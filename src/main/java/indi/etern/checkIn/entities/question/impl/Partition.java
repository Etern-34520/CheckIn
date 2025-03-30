package indi.etern.checkIn.entities.question.impl;

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
public class Partition implements Serializable, LinkTarget, BaseEntity<Integer> {
    private static final Map<String, Partition> partitionMap = new HashMap<>();
    private static final Map<Integer, Partition> partitionIdMap = new HashMap<>();
    @Column(name = "name", unique = true, nullable = false)
    String name;
    
//    FIXME foreign key
    @ManyToMany(mappedBy = "targets")
//    @JoinColumn(foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    @Fetch(value = FetchMode.SUBSELECT)
    Set<ToPartitionsLink> questionLinks;
    
    @Id
    private int id;
    
    public Integer getId() {
        return id;
    }
    
    protected Partition() {
    }
    
    private Partition(String string) {
        name = string;
        questionLinks = new HashSet<>();
//        id = Math.toIntExact(System.currentTimeMillis()/100000000);
        final Random random = new Random();
        int randomInt = random.nextInt();
        while (PartitionService.singletonInstance.existsById(randomInt)) {
            randomInt = random.nextInt();
        }
        id = randomInt;
    }
    
    public static Partition getInstance(String string) {
        if (partitionMap.get(string) == null) {
            final Optional<Partition> optionalPartition = PartitionService.singletonInstance.findByName(string);
            if (optionalPartition.isPresent()) {
                partitionMap.put(string, optionalPartition.get());
                partitionIdMap.put(optionalPartition.get().getId(), optionalPartition.get());
            } else {
                Partition partition = new Partition(string);
                partitionMap.put(string, partition);
                partitionIdMap.put(partition.getId(), partition);
            }
        }
        return partitionMap.get(string);
    }
    
    public static Partition getInstance(int partitionId) {
        if (partitionIdMap.get(partitionId) == null) {
            final Optional<Partition> optionalPartition = PartitionService.singletonInstance.findById(partitionId);
            if (optionalPartition.isPresent()) {
                partitionMap.put(optionalPartition.get().getName(), optionalPartition.get());
                partitionIdMap.put(optionalPartition.get().getId(), optionalPartition.get());
            } else {
                return null;
            }
        }
        return partitionIdMap.get(partitionId);
    }
    
    public static Partition getExample(String name) {
        return new Partition(name);
    }
    
    public void setName(String newName) {
        name = newName;
        partitionMap.remove(name);
        partitionMap.put(name, this);
    }
    
    @Override
    public String toString() {
        return name + "(" + id + ")";
    }
    //    @Override
    
    /*public void setSort(Comparator<Question> sortedSet) {
        sortedQuestion = new TreeSet<>(sortedSet);
        sortedQuestion.addAll(questions);
    }
    
    @PostLoad
    public void initSort() {
        setSort(Comparator.comparing(Question::getContent));
    }*/
    
    public Set<Question> getEnabledQuestions() {
        Set<Question> questionSet = new HashSet<>();
        for (ToPartitionsLink questionLink : questionLinks) {
            Question question = questionLink.getSource();
            if (question.isEnabled()) {
                questionSet.add(question);
            }
        }
        return questionSet;
    }
    
    public int getEnabledQuestionCount() {
        return questionLinks.stream().filter(questionLink -> questionLink.getSource().isEnabled())
                .mapToInt(questionLink -> questionLink.getSource() instanceof QuestionGroup questionGroup ? questionGroup.getQuestionLinks().size():1)
                .sum();
    }
    
    public List<Question> getEnabledQuestionsList() {
        List<Question> questionList = new ArrayList<>();
        for (ToPartitionsLink questionLink : questionLinks) {
            Question question = questionLink.getSource();
            if (question.isEnabled()) {
                questionList.add(question);
            }
        }
        return questionList;
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
    
    public Map<String,Object> toInfoMap() {
        Map<String,Object> partitionInfo = new LinkedHashMap<>();
        partitionInfo.put("id", id);
        partitionInfo.put("name", name);
        partitionInfo.put("empty", questionLinks.isEmpty());
        partitionInfo.put("enabledQuestionCount", getEnabledQuestionCount());
        partitionInfo.put("questionAmount", questionLinks.size());
        return partitionInfo;
    }
}