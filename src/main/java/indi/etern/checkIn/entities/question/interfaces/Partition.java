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
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "partitions_SEQ")
    @SequenceGenerator(name = "partitions_SEQ", sequenceName = "partitions_SEQ", allocationSize = 1)
    private int id;
    
    @Column(name = "name")
    String name;
    @ManyToMany(mappedBy = "partitions",cascade = CascadeType.ALL,fetch = FetchType.EAGER)
//    @NotFound(action= NotFoundAction.IGNORE)
    /*To fix not found question id
    * caused by:
    * before the question save
    * jpa must save the partition
    * but the partition refers to the unsaved question
    * don't worry about the sync
    * jpa will save the question at last
    */
    Set<MultiPartitionableQuestion> questions;
    protected Partition(){}
    private Partition(String string){
        name = string;
        questions = new HashSet<>();
    }
    
    public static Partition getInstance(String string){
        if (partitionMap.get(string)==null){
            partitionMap.put(string,new Partition(string));
        }
        return partitionMap.get(string);
    }
    
    public static Partition getExample(String name){
        return new Partition(name);
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
    
    public int getId() {
        return id;
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
