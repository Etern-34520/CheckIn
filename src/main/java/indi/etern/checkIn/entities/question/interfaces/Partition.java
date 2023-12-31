package indi.etern.checkIn.entities.question.interfaces;

import indi.etern.checkIn.service.dao.PartitionService;
import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import java.io.Serializable;
import java.util.*;

@Getter
@Entity
@Table(name = "partitions")
public class Partition implements Serializable {
    private static final Map<String, Partition> partitionMap = new HashMap<>();
    private static final Map<Integer, Partition> partitionIdMap = new HashMap<>();
    @Column(name = "name", unique = true, length = 191, nullable = false)//max 767 bytes
    String name;
    
    /*To fix not found question id
     * caused by:
     * before the question save
     * jpa must save the partition
     * but the partition refers to the unsaved question
     * don't worry about the sync
     * jpa will save the question at last*/
    @ManyToMany(mappedBy = "partitions", cascade = {CascadeType.PERSIST, CascadeType.DETACH, CascadeType.REFRESH, CascadeType.REMOVE}, fetch = FetchType.LAZY)
    @Fetch(value = FetchMode.SUBSELECT)
    @NotFound(action = NotFoundAction.IGNORE)
    Set<MultiPartitionableQuestion> questions;
    
    @Id
//    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "partitions_SEQ")
//    @SequenceGenerator(name = "partitions_SEQ", sequenceName = "partitions_SEQ", allocationSize = 1)
    private int id;
    
    protected Partition() {
    }
    
    private Partition(String string) {
        name = string;
        questions = new HashSet<>();
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
            final Optional<Partition> optionalPartition = PartitionService.singletonInstance.tryFindByName(string);
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
    public boolean equals(Object object) {
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
    
}
