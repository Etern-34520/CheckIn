package indi.etern.checkIn.action.setting.save;

import indi.etern.checkIn.action.BaseAction;
import indi.etern.checkIn.action.MessageOutput;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.action.interfaces.ExecuteContext;
import indi.etern.checkIn.action.interfaces.InputData;
import indi.etern.checkIn.utils.SaveSettingCommon;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Action("saveGeneratingSetting")
public class SaveGeneratingSetting extends BaseAction<SaveGeneratingSetting.Input,MessageOutput> {
    public record Input(Map<String, Object> data) implements InputData {}
    public static final String[] KEYS = {
            "questionAmount",
            "questionScore",
            "partitionRange",
            "specialPartitionLimits",
            "requiredPartitions",
            "showRequiredPartitions",
            "completingPartitions",
            "drawingStrategy",
            "completingStrategy"
    };
    
    @Transactional
    @Override
    public void execute(ExecuteContext<SaveGeneratingSetting.Input, MessageOutput> context) {
        context.requirePermission("save generating setting");
        SaveSettingCommon saveSettingCommon = new SaveSettingCommon(context.getInput().data,
                KEYS, "generating");
        saveSettingCommon.doSave();
        context.resolve(MessageOutput.success("Generating setting saved"));
    }
}
