package indi.etern.checkIn.action.setting;

import java.util.LinkedHashMap;
import indi.etern.checkIn.action.TransactionalAction;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.entities.verification.VerificationRule;
import indi.etern.checkIn.service.dao.VerificationRuleService;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Action("saveVerificationSetting")
public class SaveVerificationSetting extends TransactionalAction {
    List<Map<String, Object>> dataList;
    final VerificationRuleService verificationRuleService;
    
    public SaveVerificationSetting(VerificationRuleService verificationRuleService) {
        this.verificationRuleService = verificationRuleService;
    }
    
    @Override
    public String requiredPermissionName() {
        return "save setting verificationSetting";
    }
    
    @Override
    protected Optional<LinkedHashMap<String,Object>> doAction() throws Exception {
        verificationRuleService.deleteAll();
        for (Map<String, Object> data : dataList) {
            Map<String, Object> property = (Map<String, Object>) data.get("property");
            List<String> trace = (List<String>) property.get("trace");
//            List<String> verificationTypeNames = (List<String>) property.get("verificationTypeNames");
            String verificationTypeName = property.get("verificationTypeName").toString();
            VerificationRule verificationRule = VerificationRule.builder()
                    .id((String) data.get("id"))
                    .objectName((String) data.get("objectName"))
                    .trace(trace)
                    .verificationType(verificationTypeName)
                    .level((String) data.get("level"))
                    .values((List<Object>) data.get("values"))
                    .tipTemplate((String) data.get("tipTemplate"))
                    .index(dataList.indexOf(data))
                    .build();
            verificationRuleService.save(verificationRule);
        }
        return Optional.ofNullable(getSuccessMap());
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void initData(Map<String, Object> dataObj) {
        dataList = (List<Map<String, Object>>) dataObj.get("data");
    }
}
