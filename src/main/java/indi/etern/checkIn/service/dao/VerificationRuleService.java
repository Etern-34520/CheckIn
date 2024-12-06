package indi.etern.checkIn.service.dao;

import indi.etern.checkIn.entities.setting.verification.VerificationRule;
import indi.etern.checkIn.repositories.VerificationRuleRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VerificationRuleService {
    final VerificationRuleRepository verificationRuleRepository;
    
    public VerificationRuleService(VerificationRuleRepository verificationRuleRepository) {
        this.verificationRuleRepository = verificationRuleRepository;
    }
    
    public void deleteAll() {
        verificationRuleRepository.deleteAll();
    }
    
    public void saveAll(Iterable<VerificationRule> verificationRules) {
        verificationRuleRepository.saveAll(verificationRules);
    }
    
    public void save(VerificationRule verificationRule) {
        verificationRuleRepository.save(verificationRule);
    }
    
    public List<VerificationRule> findAll() {
        return verificationRuleRepository.findAll(Sort.by("index"));
    }
}
