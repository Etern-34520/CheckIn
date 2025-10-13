package indi.etern.checkIn.action.setting.save;

import indi.etern.checkIn.action.BaseAction;
import indi.etern.checkIn.action.MessageOutput;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.action.interfaces.ExecuteContext;
import indi.etern.checkIn.action.interfaces.InputData;
import indi.etern.checkIn.entities.setting.grading.GradingLevel;
import indi.etern.checkIn.entities.user.Role;
import indi.etern.checkIn.service.dao.GradingLevelService;
import indi.etern.checkIn.service.dao.RoleService;
import indi.etern.checkIn.service.exam.StatusService;
import indi.etern.checkIn.utils.SaveSettingCommon;
import jakarta.transaction.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Action("saveGradingSetting")
public class SaveGradingSetting extends BaseAction<SaveGradingSetting.Input, MessageOutput> {
    private final RoleService roleService;
    
    public record Input(Map<String, Object> data) implements InputData {}
    public static final String[] KEYS = {"splits", "questionScore", "multipleChoicesQuestionsCheckingStrategy", "enableLosePoints"};
    final GradingLevelService gradingLevelService;
    
    public SaveGradingSetting(GradingLevelService gradingLevelService, RoleService roleService) {
        this.gradingLevelService = gradingLevelService;
        this.roleService = roleService;
    }
    
    @Transactional
    @Override
    public void execute(ExecuteContext<SaveGradingSetting.Input, MessageOutput> context) {
        context.requirePermission("save grading setting");
        final SaveGradingSetting.Input input = context.getInput();
        SaveSettingCommon saveSettingCommon = new SaveSettingCommon(input.data,
                KEYS, "grading");
        //noinspection unchecked
        List<Map<String, Object>> levels = (List<Map<String, Object>>) input.data.get("levels");
        
        saveSettingCommon.doSave();
        List<GradingLevel> gradingLevels = new ArrayList<>();
        levels.forEach(level -> {
            GradingLevel.GradingLevelBuilder gradingLevelBuilder = GradingLevel.builder()
                    .id((String) level.get("id"))
                    .levelIndex(levels.indexOf(level))
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
            var creatingUserStrategy = GradingLevel.CreatingUserStrategy.ofOrElse(level.get("creatingUserStrategy").toString(),
                    GradingLevel.CreatingUserStrategy.NOT_CREATE);
            gradingLevelBuilder.creatingUserStrategy(creatingUserStrategy);
            if (creatingUserStrategy != GradingLevel.CreatingUserStrategy.NOT_CREATE) {
                final Optional<Role> creatingUserRole = roleService.findByType((String) level.get("creatingUserRole"));
                gradingLevelBuilder.creatingUserRole(creatingUserRole.orElse(null));
            }
            
            GradingLevel gradingLevel = gradingLevelBuilder.build();
            gradingLevels.add(gradingLevel);
        });
        gradingLevelService.deleteAll();
        gradingLevelService.saveAll(gradingLevels);
        StatusService.singletonInstance.flush();
        context.resolve(MessageOutput.success("Grading setting saved"));
    }
}