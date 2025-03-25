package indi.etern.checkIn.action.setting.save;

import indi.etern.checkIn.action.BaseAction1;
import indi.etern.checkIn.action.MessageOutput;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.action.interfaces.ExecuteContext;
import indi.etern.checkIn.action.interfaces.InputData;
import indi.etern.checkIn.entities.setting.grading.GradingLevel;
import indi.etern.checkIn.service.dao.GradingLevelService;
import indi.etern.checkIn.utils.SaveSettingCommon;
import jakarta.transaction.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Action("saveGradingSetting")
public class SaveGradingSetting extends BaseAction1<SaveGradingSetting.Input, MessageOutput> {
    public record Input(Map<String, Object> data) implements InputData {}
    public static final String[] KEYS = {"splits", "questionScore"};
    final GradingLevelService gradingLevelService;
    
    public SaveGradingSetting(GradingLevelService gradingLevelService) {
        this.gradingLevelService = gradingLevelService;
    }
    
    @Transactional
    @Override
    public void execute(ExecuteContext<SaveGradingSetting.Input, MessageOutput> context) {
        context.requirePermission("save grading setting");
        final SaveGradingSetting.Input input = context.getInput();
        //noinspection unchecked
        SaveSettingCommon saveSettingCommon = new SaveSettingCommon((Map<String, Object>) input.data.get("data"),
                KEYS, "grading");
        //noinspection unchecked
        List<Map<String, Object>> levels = (List<Map<String, Object>>) ((Map<String, Object>) input.data.get("data")).get("levels");
        
        saveSettingCommon.doSave();
        List<GradingLevel> gradingLevels = new ArrayList<>();
        levels.forEach(level -> {
            GradingLevel.GradingLevelBuilder gradingLevelBuilder = GradingLevel.builder()
                    .id((String) level.get("id"))
                    .name((String) level.get("name"));
            if (level.containsKey("description")) {
                gradingLevelBuilder.description((String) level.get("description"));
            }
            if (level.containsKey("colorHex")) {
                gradingLevelBuilder.colorHex((String) level.get("colorHex"));
            }
            if (level.containsKey("message")) {
                gradingLevelBuilder.message((String) level.get("message"));
            }
            GradingLevel gradingLevel = gradingLevelBuilder.build();
            gradingLevels.add(gradingLevel);
        });
        gradingLevelService.deleteAll();
        gradingLevelService.saveAll(gradingLevels);
        context.resolve(MessageOutput.success("Grading setting saved"));
    }
}