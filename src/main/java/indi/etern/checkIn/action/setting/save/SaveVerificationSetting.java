package indi.etern.checkIn.action.setting.save;

import indi.etern.checkIn.action.ActionExecutor;
import indi.etern.checkIn.action.BaseAction;
import indi.etern.checkIn.action.MessageOutput;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.action.interfaces.ExecuteContext;
import indi.etern.checkIn.action.interfaces.InputData;
import indi.etern.checkIn.action.setting.get.GetVerificationSettingAction;
import indi.etern.checkIn.api.webSocket.Message;
import indi.etern.checkIn.entities.setting.verification.VerificationRule;
import indi.etern.checkIn.service.dao.VerificationRuleService;
import indi.etern.checkIn.service.web.WebSocketService;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Action("saveVerificationSetting")
public class SaveVerificationSetting extends BaseAction<SaveVerificationSetting.Input, MessageOutput> {
    private final ActionExecutor actionExecutor;
    private final WebSocketService webSocketService;
    
    public record Input(List<Map<String, Object>> data) implements InputData {}
    final VerificationRuleService verificationRuleService;
    
    public SaveVerificationSetting(VerificationRuleService verificationRuleService, ActionExecutor actionExecutor, WebSocketService webSocketService) {
        this.verificationRuleService = verificationRuleService;
        this.actionExecutor = actionExecutor;
        this.webSocketService = webSocketService;
    }
    
    @SuppressWarnings("unchecked")
    @Transactional
    @Override
    public void execute(ExecuteContext<Input, MessageOutput> context) {
        context.requirePermission("save verification setting");
        final Input input = context.getInput();
        verificationRuleService.deleteAll();
        final List<Map<String, Object>> dataList = input.data;
        List<VerificationRule> verificationRuleList = new ArrayList<>();
        for (Map<String, Object> data : dataList) {
            Map<String, Object> property = (Map<String, Object>) data.get("property");
            List<String> trace = (List<String>) property.get("trace");
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
                    .ignoreMissingField((Boolean) data.get("ignoreMissingField"))
                    .index(dataList.indexOf(data))
                    .build();
            verificationRuleList.add(verificationRule);
        }
        verificationRuleService.saveAll(verificationRuleList);
        final List<Object> ruleList = actionExecutor.execute(GetVerificationSettingAction.class).getOutput().data();
        webSocketService.sendMessageToAll(Message.of("updateVerificationRules", ruleList));
        context.resolve(MessageOutput.success("Verification setting saved"));
    }
}
