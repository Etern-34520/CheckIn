package indi.etern.checkIn.action.setting.save;

import indi.etern.checkIn.action.BaseAction;
import indi.etern.checkIn.action.MessageOutput;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.action.interfaces.ExecuteContext;
import indi.etern.checkIn.action.interfaces.InputData;
import indi.etern.checkIn.utils.SaveSettingCommon;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Action("saveFacadeSetting")
public class SaveFacadeSetting extends BaseAction<SaveFacadeSetting.Input, MessageOutput> {
    public record Input(Map<String, Object> data) implements InputData {}
    public static final String[] KEYS = {"title", "subTitle", "description", "icon"};
    
    @Transactional
    @Override
    public void execute(ExecuteContext<Input, MessageOutput> context) {
        context.requirePermission("save facade setting");
        SaveSettingCommon saveSettingCommon = new SaveSettingCommon(context.getInput().data,
                KEYS,
                "facade");
        saveSettingCommon.doSave();
        context.resolve(MessageOutput.success("Facade setting saved"));
    }
}
