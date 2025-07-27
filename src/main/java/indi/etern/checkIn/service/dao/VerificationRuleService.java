package indi.etern.checkIn.service.dao;

import indi.etern.checkIn.dto.manage.CommonQuestionDTO;
import indi.etern.checkIn.dto.manage.QuestionGroupDTO;
import indi.etern.checkIn.entities.setting.verification.VerificationRule;
import indi.etern.checkIn.repositories.VerificationRuleRepository;
import indi.etern.checkIn.service.dao.verify.DynamicValidator;
import indi.etern.checkIn.service.dao.verify.ValidationContext;
import indi.etern.checkIn.service.dao.verify.ValidationResult;
import lombok.Getter;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.List;

@Service
@CacheConfig(cacheNames = "verificationRule")
public class VerificationRuleService {
    public static VerificationRuleService singletonInstance;
    final VerificationRuleRepository verificationRuleRepository;
    final Cache cache;
    
    public VerificationRuleService(VerificationRuleRepository verificationRuleRepository, CacheManager cacheManager) {
        this.verificationRuleRepository = verificationRuleRepository;
        cache = cacheManager.getCache("verificationRule");
        singletonInstance = this;
    }
    
    @CacheEvict(key = "'all'")
    public void deleteAll() {
        verificationRuleRepository.deleteAll();
    }
    
    public void saveAll(Iterable<VerificationRule> verificationRules) {
        
        cache.put("all", verificationRules);
        verificationRuleRepository.saveAll(verificationRules);
    }
    
    @Cacheable(key = "'all'")
    public List<VerificationRule> findAll() {
        return verificationRuleRepository.findAll(Sort.by("index"));
    }
    
    public String digest(CommonQuestionDTO commonQuestionDTO) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(commonQuestionDTO.getLastModifiedTime());
        singletonInstance.findAll().forEach(rule -> {
            if (rule.isApplicable(commonQuestionDTO)) {
                stringBuilder.append(rule.hashCode());
            }
        });
        return DigestUtils.md5DigestAsHex(stringBuilder.toString().getBytes());
    }
    
    public ValidationResult verify(CommonQuestionDTO commonQuestionDTO, VerifyTargetType verifyTargetType) {
        ValidationResult result = new ValidationResult();
        singletonInstance.findAll().stream().filter(rule ->
                verifyTargetType == VerifyTargetType.ANY ||
                        rule.getObjectName().equals(verifyTargetType.getTypeName())
        ).forEach(verificationRule -> {
            ValidationContext context1 = new ValidationContext(result);
            // 执行校验
            try {
                DynamicValidator.from(commonQuestionDTO, context1).applyRule(verificationRule);
            } catch (IllegalArgumentException e) {
                if (verifyTargetType != VerifyTargetType.ANY) {
                    throw e;
                }
            }
        });
        if (commonQuestionDTO instanceof QuestionGroupDTO questionGroupDTO) {
            for (CommonQuestionDTO subQuestionDTO : questionGroupDTO.getQuestions()) {
                final ValidationResult result1 = verify(subQuestionDTO, VerifyTargetType.MULTIPLE_CHOICES_QUESTION);
                if (!result1.isShowError()) {
                    result.setShowError(true);
                    commonQuestionDTO.setShowError(true);
                }
                if (!result1.isShowWarning()) {
                    result.setShowWarning(true);
                    commonQuestionDTO.setShowWarning(true);
                }
            }
        }
        if (!result.getErrors().isEmpty()) {
            commonQuestionDTO.getErrors().putAll(result.getErrors());
            result.setShowError(true);
        }
        if (!result.getWarnings().isEmpty()) {
            commonQuestionDTO.getWarnings().putAll(result.getWarnings());
            result.setShowWarning(true);
        }
        return result;
    }
    
    public enum VerifyTargetType {
        QUESTION_GROUP("QuestionGroup"),
        MULTIPLE_CHOICES_QUESTION("MultipleChoicesQuestion"),
        ANY("");
        
        @Getter
        private final String typeName;
        
        VerifyTargetType(String typeName) {
            this.typeName = typeName;
        }
    }
}