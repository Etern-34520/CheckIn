package indi.etern.checkIn.entities.linkUtils;

import indi.etern.checkIn.entities.BaseEntity;

//@Entity
public interface Link<S extends BaseEntity<?>,T extends BaseEntity<?>> {
//    @EmbeddedId
    S getSource();
    void setSource(S source);
}