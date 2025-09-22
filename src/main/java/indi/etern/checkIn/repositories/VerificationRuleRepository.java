package indi.etern.checkIn.repositories;

import indi.etern.checkIn.entities.setting.verification.VerificationRule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VerificationRuleRepository extends JpaRepository<VerificationRule,String> {

}
