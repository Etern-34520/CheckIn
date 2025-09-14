package indi.etern.checkIn;


import indi.etern.checkIn.entities.setting.verification.VerificationRule;
import indi.etern.checkIn.service.dao.verify.DynamicValidator;
import indi.etern.checkIn.service.dao.verify.ValidationContext;
import indi.etern.checkIn.service.dao.verify.ValidationResult;
import indi.etern.checkIn.utils.UUIDv7;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = CheckInApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class VerifierTest {
    @Test
    public void verify() {
        // 创建问题数据
        Map<String, Object> question = new HashMap<>();
        question.put("type", "question");
        
        List<Map<String, Object>> images = new ArrayList<>();
        images.add(Map.of("size", 5_242_880)); // 5MB
        images.add(Map.of("size", 3_145_728)); // 3MB
        question.put("images", images);
        
        // 创建校验规则
        VerificationRule rule = new VerificationRule(
                UUIDv7.randomUUID().toString(),
                "Question",
                Arrays.asList("question","images", "$*", "size"),
                "max",
                "images",
                "error",
                0,
                Arrays.asList(4.0, "MB"),
                "图片大小不能超过${limit} (当前: ${datum})",
                false
        );
        
        // 准备校验结果和上下文
        ValidationResult result = new ValidationResult();
        ValidationContext context = new ValidationContext(result);
        
        // 执行校验
        DynamicValidator.from(question, context).applyRule(rule);
        
        // 检查结果
        if (!result.getErrors().isEmpty()) {
            result.getErrors().forEach((key, msg) -> System.out.println("ERROR: " + msg));
        }
    }
}
