package indi.etern.checkIn.action.setting;

import indi.etern.checkIn.action.MapResultAction;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.entities.setting.verification.VerificationRule;
import indi.etern.checkIn.service.dao.VerificationRuleService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

@Action("getVerificationSetting")
public class GetVerificationSettingAction extends MapResultAction {
    @Autowired
    VerificationRuleService verificationRuleService;
    
    @Override
    public String requiredPermissionName() {
        return "";
    }
    
    @Override
    protected Optional<LinkedHashMap<String,Object>> doAction() throws Exception {
        List<VerificationRule> ruleList = verificationRuleService.findAll();
        LinkedHashMap<String,Object> result = new LinkedHashMap<>();
        ArrayList<Object> arrayList = new ArrayList<>();
        for (VerificationRule verificationRule : ruleList) {
            LinkedHashMap<String,Object> verificationRuleMap = new LinkedHashMap<>();
            verificationRuleMap.put("id",verificationRule.getId());
            verificationRuleMap.put("objectName",verificationRule.getObjectName());
            final List<String> propertiesLink = verificationRule.getTrace();
            ArrayList<Object> propertiesLinkList = new ArrayList<>(propertiesLink.size());
            propertiesLinkList.addAll(propertiesLink);
            LinkedHashMap<String,Object> propertyMap = new LinkedHashMap<>();
            propertyMap.put("trace", propertiesLinkList);
            propertyMap.put("verificationTypeName",verificationRule.getVerificationType());
            verificationRuleMap.put("property",propertyMap);
            verificationRuleMap.put("level",verificationRule.getLevel());
            verificationRuleMap.put("targetInputName",verificationRule.getTargetInputName());
            verificationRuleMap.put("tipTemplate",verificationRule.getTipTemplate());
            final List<Object> values = verificationRule.getValues();
            ArrayList<Object> valuesList = new ArrayList<>(values.size());
            for (Object value : values) {
                if (value instanceof Number) {
                    valuesList.add((Number) value);
                } else if (value instanceof String) {
                    valuesList.add((String) value);
                } else if (value instanceof Boolean) {
                    valuesList.add((Boolean) value);
                }
            }
            verificationRuleMap.put("values",valuesList);
            arrayList.add(verificationRuleMap);
        }
        result.put("data", arrayList);
        return Optional.of(result);
    }
}