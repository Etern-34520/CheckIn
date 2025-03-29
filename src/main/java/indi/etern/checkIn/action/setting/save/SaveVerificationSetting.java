package indi.etern.checkIn.action.setting.save;

import indi.etern.checkIn.action.BaseAction1;
import indi.etern.checkIn.action.MessageOutput;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.action.interfaces.ExecuteContext;
import indi.etern.checkIn.action.interfaces.InputData;
import indi.etern.checkIn.entities.setting.verification.VerificationRule;
import indi.etern.checkIn.service.dao.VerificationRuleService;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Action("saveVerificationSetting")
public class SaveVerificationSetting extends BaseAction1<SaveVerificationSetting.Input, MessageOutput> {
    public record Input(List<Map<String, Object>> data) implements InputData {}
    final VerificationRuleService verificationRuleService;
    
    public SaveVerificationSetting(VerificationRuleService verificationRuleService) {
        this.verificationRuleService = verificationRuleService;
    }
    
    @Transactional
    @Override
    public void execute(ExecuteContext<Input, MessageOutput> context) {
        context.requirePermission("save verification setting");
        final Input input = context.getInput();
        verificationRuleService.deleteAll();
        final List<Map<String, Object>> dataList = input.data;
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
                    .targetInputName((String) data.get("targetInputName"))
                    .values((List<Object>) data.get("values"))
                    .tipTemplate((String) data.get("tipTemplate"))
                    .index(dataList.indexOf(data))
                    .build();
            verificationRuleService.save(verificationRule);
        }
        context.resolve(MessageOutput.success("Verification setting saved"));
    }
}
