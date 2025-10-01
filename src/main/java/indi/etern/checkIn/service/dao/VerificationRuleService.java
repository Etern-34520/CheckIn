package indi.etern.checkIn.service.dao;

import indi.etern.checkIn.dto.manage.question.CommonQuestionDTO;
import indi.etern.checkIn.dto.manage.question.QuestionGroupDTO;
import indi.etern.checkIn.entities.question.impl.Question;
import indi.etern.checkIn.entities.setting.verification.VerificationRule;
import indi.etern.checkIn.repositories.VerificationRuleRepository;
import indi.etern.checkIn.service.dao.verify.DynamicValidator;
import indi.etern.checkIn.service.dao.verify.ValidationContext;
import indi.etern.checkIn.service.dao.verify.ValidationResult;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheConfig;
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
    private final QuestionService questionService;
    List<VerificationRule> rules;
    Logger logger = LoggerFactory.getLogger(VerificationRuleService.class);

    public VerificationRuleService(VerificationRuleRepository verificationRuleRepository, CacheManager cacheManager, QuestionService questionService) {
        this.verificationRuleRepository = verificationRuleRepository;
        cache = cacheManager.getCache("verificationRule");
        singletonInstance = this;
        this.questionService = questionService;
    }

    public void deleteAll() {
        verificationRuleRepository.deleteAll();
        rules = List.of();
    }

    public void saveAll(List<VerificationRule> verificationRules) {
        rules = verificationRules;
        verificationRuleRepository.saveAll(verificationRules);
    }

    public List<VerificationRule> getAll() {
        if (rules == null) {
            rules = verificationRuleRepository.findAll(Sort.by("index"));
        }
        return rules;
    }

    public String digest(CommonQuestionDTO commonQuestionDTO) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(commonQuestionDTO.getLastModifiedTime());
        getAll().forEach(rule -> {
            if (rule.isApplicable(commonQuestionDTO)) {
                stringBuilder.append(rule.hashCode());
            }
        });
        return DigestUtils.md5DigestAsHex(stringBuilder.toString().getBytes());
    }

    public ValidationResult verify(CommonQuestionDTO commonQuestionDTO, VerifyTargetType verifyTargetType) {
        ValidationResult result = new ValidationResult();
        getAll().stream().filter(rule ->
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

    public ValidationResult updateValidation(CommonQuestionDTO commonQuestionDTO, Question question) {
        final String currentDigest = digest(commonQuestionDTO);
        final String previousDigest = question.getVerificationDigest();
        if (!currentDigest.equals(previousDigest)) {
            if (logger.isDebugEnabled()) {
                logger.debug("Verify question[\"{}\"] due to invalid digest, previous:\"{}\", current:\"{}\"", question.getId(), previousDigest, currentDigest);
            }
            final ValidationResult result;
//            synchronized (question.getId()) {
            result = verify(commonQuestionDTO, VerificationRuleService.VerifyTargetType.ANY);
            question.setVerificationDigest(currentDigest);
            question.setValidationResult(result);
            questionService.save(question);
//            }
            if (logger.isDebugEnabled() && !result.getErrors().isEmpty()) {
                logger.debug("Verify result errors:");
                result.getErrors().forEach((key, msg) -> logger.debug("{}: {}", key, msg));
                logger.debug("========");
            }
            if (logger.isDebugEnabled() && !result.getWarnings().isEmpty()) {
                logger.debug("Verify result warnings:");
                result.getWarnings().forEach((key, msg) -> logger.debug("{}: {}", key, msg));
                logger.debug("========");
            }
            return result;
        } else {
            return question.getValidationResult();
        }
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