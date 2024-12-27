package indi.etern.checkIn.entities.setting;

import com.fasterxml.jackson.core.JsonProcessingException;
import indi.etern.checkIn.MVCConfig;
import indi.etern.checkIn.entities.convertor.ClassConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

@Entity
@Table(name = "SERVER_SETTING_ITEMS")
public class SettingItem {
    @Transient
    public static final SettingItem EMPTY = new SettingItem();
    
    @Id
    @Column(name = "SETTING_KEY")
    @Getter
    private String key;
    
    @Column(name = "SETTING_VALUE")
    private String stringValue;
    
    @Transient
    @Setter
    private Object value;
    
    @Column(name = "CLAZZ")
    @Convert(converter = ClassConverter.class)
    @Getter
    private Class<?> clazz;
    
    protected SettingItem() {
        key = "";
        stringValue = "";
    }
    
    @SuppressWarnings("unchecked")
    public <T> T getValue(Class<T> clazz) {
        return (T) value;
    }
    
    public boolean isPresent() {
        return !key.isEmpty();
    }
    
    @SneakyThrows
    public <T> T readAs(Class<T> clazz) {
        return MVCConfig.getObjectMapper().readValue(stringValue, clazz);
    }
    
    @PostLoad
    private void postLoad() throws JsonProcessingException {
        value = stringValue == null ? null : MVCConfig.getObjectMapper().readValue(stringValue, clazz);
    }
    
    public SettingItem(String key, Object value, Class<?> clazz) {
        try {
            this.key = key;
            this.value = value;
            stringValue = value == null ? null : MVCConfig.getObjectMapper().writeValueAsString(value);
            this.clazz = clazz;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}