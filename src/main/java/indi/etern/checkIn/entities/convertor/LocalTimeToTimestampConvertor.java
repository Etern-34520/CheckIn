package indi.etern.checkIn.entities.convertor;

import jakarta.persistence.AttributeConverter;

import java.sql.Time;
import java.time.LocalTime;

public class LocalTimeToTimestampConvertor implements AttributeConverter<LocalTime, Time> {
    @Override
    public Time convertToDatabaseColumn(LocalTime attribute) {
        return Time.valueOf(attribute);
    }
    
    @Override
    public LocalTime convertToEntityAttribute(Time dbData) {
        return dbData.toLocalTime();
    }
}
