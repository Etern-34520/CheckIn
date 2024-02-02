package indi.etern.checkIn.entities.setting;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Table(name = "SERVER_SETTING_ITEMS")
public class SettingItem {
    public static final SettingItem EMPTY = new SettingItem();
    @Id
    @Column(name = "SETTING_KEY")
    private String key;
    
    @Setter
    @Column(name = "SETTING_VALUE")
    private String value;
    
    protected SettingItem() {
        key = "";
        value = "";
    }
    
    public boolean isPresent() {
        return !key.isEmpty();
    }
    
    public SettingItem(String key, String value) {
        this.key = key;
        this.value = value;
    }
    
    public SettingItem(String key) {
        this.key = key;
    }
}
