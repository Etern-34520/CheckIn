package indi.etern.checkIn.entities.question.interfaces;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
@Entity
@Table(name = "partitions")
public class Partition implements Serializable {
    private static final Map<String,Partition> partitionMap = new HashMap<>();
    @Id
    @Column(name = "name")
    String name;
    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(name = "partitions_questions_mapping",
            joinColumns  = @JoinColumn(name = "partition_id",referencedColumnName = "name"),
            inverseJoinColumns = @JoinColumn(name = "question_id",referencedColumnName = "id"))
    Set<MultiPartitionableQuestion> questions = new HashSet<>();
    protected Partition(){}
    private Partition(String string){
        name = string;
    }
    
    public static Partition getInstance(String string){
        if (partitionMap.get(string)==null){
            partitionMap.put(string,new Partition(string));
        }
        return partitionMap.get(string);
    }
    
    public String getName() {
        return name;
    }
    public Set<MultiPartitionableQuestion> getQuestions() {
        return questions;
    }
    public void addQuestion(MultiPartitionableQuestion question){
        questions.add(question);
    }
    @Override
    public boolean equals(Object object){
        if (object instanceof Partition) {
            return ((Partition) object).name.equals(this.name);
        } else {
            return false;
        }
    }
    
    @Override
    public String toString() {
        return name;
    }
    
//    @Override
    public String getStaticHash() {
        return name;
    }
    /*
    @Override
    public void beforeSave(Dao dao){
        questionMd5s.clear();
        for (MultiPartitionableQuestion question : questions) {
            questionMd5s.add(question.getMd5());
        }
    }
    @Override
    public void afterLoad(Dao dao){
        for (String questionMd5 : questionMd5s) {
            questions.add((MultiPartitionableQuestion) dao.get(questionMd5,MultiPartitionableQuestion.class));
        }
    }*/
}
