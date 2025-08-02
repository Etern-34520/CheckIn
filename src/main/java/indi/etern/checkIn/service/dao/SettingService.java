package indi.etern.checkIn.service.dao;

import indi.etern.checkIn.entities.setting.SettingItem;
import indi.etern.checkIn.repositories.SettingRepository;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
@CacheConfig(cacheNames = "setting")
public class SettingService {
    public static SettingService singletonInstance;
    final SettingRepository settingRepository;
    final Cache cache;
    
    protected SettingService(SettingRepository settingRepository, CacheManager cacheManager) {
        singletonInstance = this;
        this.settingRepository = settingRepository;
        cache = cacheManager.getCache("setting");
    }
    
    @Cacheable(key = "#root + '.' + #name")
    public SettingItem getItem(String root, String name) {
        return settingRepository.findById(root + "." + name).orElseThrow();
    }
    
    public List<SettingItem> findAllByKeys(Collection<String> keys) {
        List<SettingItem> settingItems = new ArrayList<>(keys.size());
        List<String> uncachedKeys = new ArrayList<>();
        for (String key : keys) {
            final SettingItem settingItem = cache.get(key, SettingItem.class);
            if (settingItem != null) {
                settingItems.add(settingItem);
            } else {
                uncachedKeys.add(key);
            }
        }
        final List<SettingItem> allById = settingRepository.findAllById(uncachedKeys);
        for (SettingItem settingItem : allById) {
            cache.put(settingItem.getKey(), settingItem);
        }
        settingItems.addAll(allById);
        return settingItems;
    }
    
    @CachePut(key = "#settingItem.key")
    public void setSetting(SettingItem settingItem) {
        settingRepository.save(settingItem);
    }
    
    public void setAll(Iterable<SettingItem> settingItems) {
        for (SettingItem settingItem : settingItems) {
            cache.put(settingItem.getKey(), settingItem);
        }
        settingRepository.saveAll(settingItems);
    }
}
