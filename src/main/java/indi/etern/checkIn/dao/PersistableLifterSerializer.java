package indi.etern.checkIn.dao;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.FieldSerializer;


public class PersistableLifterSerializer extends FieldSerializer<Object> {
    private Dao dao = ApplicationContextUtils.getApplicationContext().getBean("getDao", Dao.class);
    
    public PersistableLifterSerializer(Kryo kryo, Class type) {
        super(kryo, type);
    }
    
    public PersistableLifterSerializer(Kryo kryo, Class type, FieldSerializerConfig config) {
        super(kryo, type, config);
    }
    
    @Override
    public void write(Kryo kryo, Output output, Object object) {
        super.write(kryo, output, object);
        if (object instanceof PersistableWithStaticHash persistableWithStaticHash){
//            persistableWithStaticHash.beforeSave();
//            persistableWithStaticHash.beforeSave(dao);
        }
    }
    
    public void setDao(Dao dao) {
        this.dao = dao;
    }
    
    @Override
    public Object read(Kryo kryo, Input input, Class aClass) {
        Object object = super.read(kryo, input, aClass);
        if (object instanceof PersistableWithStaticHash persistableWithStaticHash){
            persistableWithStaticHash.afterLoad();
            persistableWithStaticHash.afterLoad(dao);
        }
        return object;
    }
}
