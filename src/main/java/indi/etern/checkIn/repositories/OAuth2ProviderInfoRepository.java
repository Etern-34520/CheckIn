package indi.etern.checkIn.repositories;

import indi.etern.checkIn.entities.setting.oauth2.OAuth2ProviderInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.Set;

public interface OAuth2ProviderInfoRepository extends JpaRepository<OAuth2ProviderInfo,String> {
    Optional<OAuth2ProviderInfo> findByName(String name);

    Set<OAuth2ProviderInfo> findAllByEnabledIsTrue();
}
