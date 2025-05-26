package indi.etern.checkIn.entities.linkUtils;

import indi.etern.checkIn.entities.BaseEntity;

//@Entity
//@Table("partitions_questions_mapping")
public interface ToOneLink<S extends LinkSource<?> & BaseEntity<?>,T extends LinkTarget & BaseEntity<?>> extends Link<S,T> {
    S getSource();
    T getTarget();
}