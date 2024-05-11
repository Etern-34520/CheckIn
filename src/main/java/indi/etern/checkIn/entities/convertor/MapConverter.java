package indi.etern.checkIn.entities.convertor;

import com.google.gson.Gson;
import jakarta.persistence.AttributeConverter;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class MapConverter implements AttributeConverter<Map<?,?>,String> {
    final Gson gson;
    
    public MapConverter(Gson gson) {
        this.gson = gson;
    }
    
    @Override
    public String convertToDatabaseColumn(Map<?,?> attribute) {
        return gson.toJson(attribute);
    }
    
    @Override
    public Map<String,String> convertToEntityAttribute(String dbData) {
        return gson.fromJson(dbData,Map.class);
    }
}
