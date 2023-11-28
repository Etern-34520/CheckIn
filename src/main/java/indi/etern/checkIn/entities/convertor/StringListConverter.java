package indi.etern.checkIn.entities.convertor;

import com.google.gson.Gson;
import jakarta.persistence.AttributeConverter;

import java.util.List;

public class StringListConverter implements AttributeConverter<List<String>,String> {
    Gson gson = new Gson();
    @Override
    public String convertToDatabaseColumn(List<String> attribute) {
        return gson.toJson(attribute);
    }
    
    @Override
    public List<String> convertToEntityAttribute(String dbData) {
        return gson.fromJson(dbData,List.class);
    }
}
