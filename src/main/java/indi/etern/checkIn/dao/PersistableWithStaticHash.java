package indi.etern.checkIn.dao;

import java.io.Serial;
import java.io.Serializable;

public interface PersistableWithStaticHash extends Serializable {
    String getStaticHash();
    
    @Serial
    default Object readResolve() {
        try {
            Dao dao = ApplicationContextUtils.getApplicationContext().getBean("getDao", Dao.class);
            afterLoad(dao);
            afterLoad();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }
    
    default void afterLoad(Dao dao){}
    default void afterLoad(){}
    default void onDelete(Dao dao){}
    default void onDelete(){}
}
