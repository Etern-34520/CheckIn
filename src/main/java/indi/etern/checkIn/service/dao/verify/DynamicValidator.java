package indi.etern.checkIn.service.dao.verify;

import indi.etern.checkIn.dto.manage.question.BasicQuestionDTO;
import indi.etern.checkIn.dto.manage.question.QuestionGroupDTO;
import indi.etern.checkIn.entities.setting.verification.VerificationRule;
import indi.etern.checkIn.throwable.entity.VerifyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class DynamicValidator {
    private final Object root;
    private final List<Operation> operations = new ArrayList<>();
    private final ValidationContext context;
    Logger logger = LoggerFactory.getLogger(DynamicValidator.class);
    private String aggregateType;
    private boolean isAggregateMode = false;
    private boolean isEmptyCheck = false; // 添加空检查标志
    
    private DynamicValidator(Object root, ValidationContext context) {
        this.root = root;
        this.context = context;
    }
    
    public static DynamicValidator from(Object object, ValidationContext context) {
        return new DynamicValidator(object, context);
    }
    
    public void applyRule(VerificationRule rule) {
        // 重置规则特定状态
        context.resetRuleState();
        context.resetAggregates();
        
        final String objectType = getObjectType(root);
        if (!rule.getObjectName().equals(objectType)) {
            logger.debug("The rule (for {}) is not suitable for {}", rule.getObjectName(), objectType);
            throw new IllegalArgumentException("The rule (for "+ rule.getObjectName() +") is not suitable for " + objectType);
        }
        
        context.setCurrentRule(rule);
        isEmptyCheck = "empty".equals(rule.getVerificationType()); // 设置空检查标志
        
        final List<String> trace = new ArrayList<>(rule.getTrace());
        List<String> tokens = trace.stream().flatMap(s -> Arrays.stream(s.split("\\."))).skip(1).toList();
        processTokens(tokens, rule.isIgnoreMissingField());
        executeChain(rule, tokens);
    }
    
    private String getObjectType(Object obj) {
        if (obj instanceof QuestionGroupDTO) {
            return "QuestionGroup";
        } else if (obj instanceof BasicQuestionDTO) {
            return "MultipleChoicesQuestion";
        }
        return obj.getClass().getSimpleName();
    }
    
    private void processTokens(List<String> tokens, boolean ignoreMissingField) {
        for (int i = 0; i < tokens.size(); i++) {
            String token = tokens.get(i);
            
            // 处理聚合操作
            if (token.equals("&count")) {
                count();
            } else if (token.equals("&min")) {
                min();
            } else if (token.equals("&max")) {
                max();
            } else if (token.equals("&sum")) {
                sum();
            }
            // 处理遍历操作（带或不带过滤）
            else if (token.startsWith("$")) {
                // 检查是否在同一token中包含过滤条件
                if (token.contains("=")) {
                    String[] parts = token.split("=");
                    if (parts.length < 2) {
                        throw new VerifyException("Invalid filter expression: " + token);
                    }
                    
                    String fieldPart = parts[0].replace("$", "").replace("*", "");
                    String filterField = fieldPart.isEmpty() ? null : fieldPart;
                    Object filterValue = parseValue(parts[1]);
                    
                    // 添加遍历操作
                    each();
                    
                    // 添加过滤操作
                    if (filterField != null) {
                        filter(obj -> {
                            Object fieldValue = getFieldValue(obj, filterField);
                            return filterValue.equals(fieldValue);
                        });
                    } else {
                        filter(obj -> filterValue.equals(obj));
                    }
                    
                    if (token.contains("*")) {
                        isAggregateMode = true;
                    }
                } else {
                    boolean isAggregate = token.contains("*");
                    String field = token.replace("$", "").replace("*", "");
                    
                    each();
                    
                    // 检查下一个token是否是过滤条件
                    if (i + 1 < tokens.size() && tokens.get(i + 1).contains("=")) {
                        String filterToken = tokens.get(i + 1);
                        i++;
                        
                        String[] parts = filterToken.split("=");
                        String filterField = parts[0];
                        Object filterValue = parseValue(parts[1]);
                        
                        filter(obj -> {
                            Object fieldValue = getFieldValue(obj, filterField);
                            return filterValue.equals(fieldValue);
                        });
                    }
                    
                    if (!field.isEmpty()) {
                        field(field, ignoreMissingField);
                    }
                    
                    if (isAggregate) {
                        isAggregateMode = true;
                    }
                }
            }
            // 处理独立的过滤条件
            else if (token.contains("=")) {
                String[] parts = token.split("=");
                String field = parts[0];
                Object value = parseValue(parts[1]);
                filter(obj -> {
                    Object fieldValue = getFieldValue(obj, field);
                    return value.equals(fieldValue);
                });
            }
            // 处理普通字段访问
            else {
                field(token, ignoreMissingField);
            }
        }
    }
    
    private Object parseValue(String str) {
        if ("true".equalsIgnoreCase(str)) return true;
        if ("false".equalsIgnoreCase(str)) return false;
        try {
            return Double.parseDouble(str);
        } catch (NumberFormatException e) {
            return str;
        }
    }
    
    private Object getFieldValue(Object obj, String fieldName) {
        return ReflectionUtil.getFieldValue(obj, fieldName);
    }
    
    // ===== 链式操作API =====
    public DynamicValidator field(String fieldName, boolean ignoreMissingField) {
        operations.add((data, next) -> {
            Object value = getFieldValue(data, fieldName);
            
            // 空检查：字段缺失或值为null
            if (isEmptyCheck && value == null) {
                if (!ignoreMissingField) {
                    context.markEmpty();
                }
                return;
            }
            
            // 空检查：空字符串
            if (isEmptyCheck && value instanceof String && ((String) value).isEmpty()) {
                return;
            }
            
            if (value != null) {
                next.accept(value);
            } else if (ignoreMissingField) {
                logger.debug("Ignoring missing value for field {}", fieldName);
            } else {
                throw new VerifyException("Missing field: " + fieldName);
            }
        });
        return this;
    }
    
    public DynamicValidator each() {
        operations.add((data, next) -> {
            if (data == null) {
                // 空检查：空集合
                if (isEmptyCheck) {
                    context.markEmpty();
                }
                return;
            }
            
            int itemCount = 0;
            
            if (data instanceof Iterable) {
                Iterable<?> iterable = (Iterable<?>) data;
                for (Object item : iterable) {
                    itemCount++;
                    next.accept(item);
                }
            } else if (data instanceof Object[]) {
                Object[] array = (Object[]) data;
                for (Object item : array) {
                    itemCount++;
                    next.accept(item);
                }
            } else {
                throw new VerifyException("Not iterable: " + data.getClass().getSimpleName());
            }
            
            // 空检查：空集合
            if (isEmptyCheck && itemCount == 0) {
                context.markEmpty();
            }
        });
        return this;
    }
    
    public DynamicValidator filter(Predicate<Object> predicate) {
        operations.add((data, next) -> {
            if (predicate.test(data)) {
                next.accept(data);
            }
        });
        return this;
    }
    
    public DynamicValidator count() {
        operations.add((data, next) -> {
            context.incrementCount();
        });
        aggregateType = "count";
        return this;
    }
    
    public DynamicValidator min() {
        operations.add((data, next) -> {
            if (data instanceof Number) {
                double value = ((Number) data).doubleValue();
                context.updateMin(value);
                next.accept(value);
            }
        });
        aggregateType = "min";
        return this;
    }
    
    public DynamicValidator max() {
        operations.add((data, next) -> {
            if (data instanceof Number) {
                double value = ((Number) data).doubleValue();
                context.updateMax(value);
                next.accept(value);
            }
        });
        aggregateType = "max";
        return this;
    }
    
    public DynamicValidator sum() {
        operations.add((data, next) -> {
            if (data instanceof Number) {
                double value = ((Number) data).doubleValue();
                context.addSum(value);
                next.accept(value);
            }
        });
        aggregateType = "sum";
        return this;
    }
    
    // ===== 执行校验链 =====
    private void executeChain(VerificationRule rule, List<String> tokens) {
        try {
            Consumer<Object> chain = data1 -> {
                if (!isEmptyCheck) {
                    // 非空检查的常规校验逻辑
                    double resultValue = 0;

                    if (aggregateType != null) {
                        resultValue = switch (aggregateType) {
                            case "count" -> context.getCount();
                            case "min" -> context.getMin();
                            case "max" -> context.getMax();
                            case "sum" -> context.getSum();
                            default -> 0;
                        };
                    } else if (isAggregateMode) {
                        resultValue = switch (rule.getVerificationType()) {
                            case "min" -> context.getMin();
                            case "max" -> context.getMax();
                            case "sum" -> context.getSum();
                            case null, default -> 0;
                        };
                    } else {
                        if (data1 instanceof String) {
                            resultValue = ((String) data1).length();
                        } else if (data1 instanceof Number) {
                            resultValue = ((Number) data1).doubleValue();
                        }
                    }

                    // 特殊处理：图片大小转换
                    if (tokens.contains("images") && (tokens.contains("$size") || tokens.contains("size"))) {
                        resultValue = resultValue / 1048576.0;
                        resultValue = Math.round(resultValue * 100.0) / 100.0;
                    }

                    // 获取比较值
                    double limitValue1 = 0;
                    if (!rule.getValues().isEmpty()) {
                        Object val1 = rule.getValues().getFirst();
                        if (val1 instanceof Number) {
                            limitValue1 = ((Number) val1).doubleValue();
                        } else if (val1 instanceof String) {
                            try {
                                limitValue1 = Double.parseDouble((String) val1);
                            } catch (NumberFormatException e1) {
                                // 使用默认值
                            }
                        }
                    }

                    context.validateNumber(resultValue, limitValue1);
                }
            };
            for (int i = operations.size() - 1; i >= 0; i--) {
                Operation op = operations.get(i);
                Consumer<Object> next = chain;
                chain = data -> op.execute(data, next);
            }
            
            // 执行校验链
            chain.accept(root);
            
            // 特殊处理空检查 - 在所有操作完成后执行
            if (isEmptyCheck) {
                context.checkEmptyCondition();
            }
            
            // 特殊处理计数操作
            if ("count".equals(aggregateType)) {
                double countValue = context.getCount();
                double limitValue = 0;
                if (!rule.getValues().isEmpty()) {
                    Object val = rule.getValues().getFirst();
                    if (val instanceof Number) {
                        limitValue = ((Number) val).doubleValue();
                    } else if (val instanceof String) {
                        try {
                            limitValue = Double.parseDouble((String) val);
                        } catch (NumberFormatException e) {
                            // 使用默认值
                        }
                    }
                }
                context.validateNumber(countValue, limitValue);
            }
        } catch (VerifyException e) {
            RuleLevel level = RuleLevel.valueOf(rule.getLevel().toUpperCase());
            String issueKey = rule.getId() + "-error";
            if (level == RuleLevel.ERROR) {
                context.getResult().addError(issueKey, e.getMessage());
            } else {
                context.getResult().addWarning(issueKey, e.getMessage());
            }
        } finally {
            operations.clear();
            aggregateType = null;
            isAggregateMode = false;
            isEmptyCheck = false;
        }
    }
    
    @FunctionalInterface
    interface Operation {
        void execute(Object data, Consumer<Object> next);
    }
}