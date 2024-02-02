package indi.etern.checkIn.repositories;

import indi.etern.checkIn.entities.setting.SettingItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SettingRepository extends JpaRepository<SettingItem,String> {
    List<SettingItem> findAllByKeyLike(String key);
}
