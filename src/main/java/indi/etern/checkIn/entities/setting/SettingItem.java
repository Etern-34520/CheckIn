package indi.etern.checkIn.entities.setting;

import com.fasterxml.jackson.core.JsonProcessingException;
import indi.etern.checkIn.CheckInApplication;
import indi.etern.checkIn.entities.converter.ClassConverter;
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
    
    @Column(name = "SETTING_VALUE", columnDefinition = "MEDIUMTEXT")
    private String stringValue;
    
    @Transient
    @Setter
    private Object value;
    
    @Transient
    private boolean loaded = false;
    
    @Column(name = "CLAZZ")
    @Convert(converter = ClassConverter.class)
    @Getter
    private Class<?> clazz;
    
    protected SettingItem() {
        key = "";
        stringValue = "";
    }
    
    @SneakyThrows
    @SuppressWarnings("unchecked")
    public <T> T getValue(Class<T> clazz) {
        if (!loaded) {
            loaded = true;
            postLoad();
        }
        return (T) value;
    }
    
    @SneakyThrows
    public Object getValue() {
        if (!loaded) {
            loaded = true;
            postLoad();
        }
        return value;
    }
    
    public boolean isPresent() {
        return !key.isEmpty();
    }
    
//    @PostLoad
    private void postLoad() throws JsonProcessingException {
        if (clazz != null) {
            value = stringValue == null ? null : CheckInApplication.getObjectMapper().readValue(stringValue, clazz);
        } else {
            value = null;
        }
    }
    
    public SettingItem(String key, Object value, Class<?> clazz) {
        try {
            this.key = key;
            this.value = value;
            stringValue = value == null ? null : CheckInApplication.getObjectMapper().writeValueAsString(value);
            this.clazz = clazz;
            loaded = true;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
    
    public SettingItem(String key, Object value) {
        try {
            this.key = key;
            this.value = value;
            stringValue = value == null ? null : CheckInApplication.getObjectMapper().writeValueAsString(value);
            if (value != null) {
                this.clazz = value.getClass();
            } else {
                this.clazz = null;
            }
            loaded = true;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}