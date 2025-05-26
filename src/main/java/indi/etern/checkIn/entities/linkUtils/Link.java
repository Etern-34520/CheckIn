package indi.etern.checkIn.entities.linkUtils;

import indi.etern.checkIn.entities.BaseEntity;

public interface Link<S extends BaseEntity<?>,T extends BaseEntity<?>> {
    @SuppressWarnings("EmptyMethod")
    S getSource();
    void setSource(S source);
}