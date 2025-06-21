package indi.etern.checkIn.service.dao.verify;

import indi.etern.checkIn.entities.setting.verification.VerificationRule;
import lombok.Getter;
import lombok.Setter;

// ===== 在ValidationContext中添加重置方法 =====
public class ValidationContext {
    @Getter
    private double min = Double.MAX_VALUE;
    @Getter
    private double max = Double.MIN_VALUE;
    @Getter
    private double sum = 0;
    private int count = 0;
    private int invokeCount = 0;
    @Getter
    private final ValidationResult result;
    @Setter
    private VerificationRule currentRule;
    private boolean isEmptyCondition = false; // 空检查标志
    
    public ValidationContext(ValidationResult result) {
        this.result = result;
    }
    
    // 重置规则特定状态
    public void resetRuleState() {
        invokeCount = 0;
        isEmptyCondition = false;
    }
    
    // 重置所有聚合值
    public void resetAggregates() {
        min = Double.MAX_VALUE;
        max = Double.MIN_VALUE;
        sum = 0;
        count = 0;
    }
    
    // 标记空状态
    public void markEmpty() {
        isEmptyCondition = true;
    }
    
    // 检查空条件
    public void checkEmptyCondition() {
        if (isEmptyCondition) {
            validateNumber(0, 0);
        }
    }
    
    private String getTip(double num, double data) {
        String unit = "";
        if (currentRule.getValues().size() > 1) {
            unit = String.valueOf(currentRule.getValues().get(1));
        }
        
        String tip = currentRule.getTipTemplate();
        if (data % 1 == 0) {
            tip = tip.replace("${limit}", String.format(" %d %s ", (int) data, unit));
        } else {
            tip = tip.replace("${limit}", String.format(" %.2f %s ", data, unit));
        }
        if (num % 1 == 0) {
            tip = tip.replace("${datum}", String.format(" %d %s ", (int) num, unit));
        } else {
            tip = tip.replace("${datum}", String.format(" %.2f %s ", num, unit));
        }
        tip = tip.replace("${order}", " " + (invokeCount + 1) + " ");
        return tip;
    }
    
    public void validateNumber(double num, double data) {
        String tip = getTip(num, data);
        
        String issueKey = currentRule.getId() + "-" + invokeCount;
        RuleLevel level = RuleLevel.valueOf(currentRule.getLevel().toUpperCase());
        
        boolean shouldAdd = switch (currentRule.getVerificationType()) {
            case "min" -> num < data;
            case "max" -> num > data;
            case "empty" -> true; // 空检查总是触发
            case "count" -> num < data;
            default -> false; // 根据需求调整
        };
        
        if (shouldAdd) {
            if (level == RuleLevel.ERROR) {
                result.addError(issueKey, tip);
            } else {
                result.addWarning(issueKey, tip);
            }
            invokeCount++;
        }
    }
    
    public void incrementCount() {
        count++;
    }
    
    public void updateMin(double value) {
        min = Math.min(min, value);
    }
    
    public void updateMax(double value) {
        max = Math.max(max, value);
    }
    
    public void addSum(double value) {
        sum += value;
    }
    
    public double getCount() {
        return count;
    }
}