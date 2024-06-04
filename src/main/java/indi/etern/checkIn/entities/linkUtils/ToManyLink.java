package indi.etern.checkIn.entities.linkUtils;

import indi.etern.checkIn.entities.BaseEntity;

import java.util.Set;

//@Entity
//@Table("partitions_questions_mapping")
public interface ToManyLink<S extends LinkSource & BaseEntity<?>,T extends LinkTarget & BaseEntity<?>> extends Link<S,T> {
    S getSource();
    Set<T> getTargets();
}