package indi.etern.checkIn.repositories;

import indi.etern.checkIn.entities.setting.oauth2.OAuth2ProviderInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OAuth2ProviderInfoRepository extends JpaRepository<OAuth2ProviderInfo,String> {
}
