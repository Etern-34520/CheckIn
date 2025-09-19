package indi.etern.checkIn.repositories;

import indi.etern.checkIn.entities.user.OAuth2Binding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OAuth2BindingRepository extends JpaRepository<OAuth2Binding, String> {
    Optional<OAuth2Binding> findByProviderIdAndUserId(String providerId, String userId);

    boolean existsByProviderId(String providerId);
}
