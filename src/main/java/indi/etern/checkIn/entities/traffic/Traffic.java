package indi.etern.checkIn.entities.traffic;

import indi.etern.checkIn.dao.PersistableWithStaticHash;

public class Traffic implements PersistableWithStaticHash {
    String dateString;
    private Traffic() {
        dateString = java.time.LocalDate.now().toString();
    }
    @Override
    public String getStaticHash() {
        return dateString;
    }
}
