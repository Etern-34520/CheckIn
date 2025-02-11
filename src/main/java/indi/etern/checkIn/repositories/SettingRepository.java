package indi.etern.checkIn.repositories;

import indi.etern.checkIn.entities.setting.SettingItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettingRepository extends JpaRepository<SettingItem,String> {
}
