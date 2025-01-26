package indi.etern.checkIn.action.setting;

import indi.etern.checkIn.action.MapResultAction;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.entities.setting.grading.GradingLevel;
import indi.etern.checkIn.service.dao.GradingLevelService;
import jakarta.transaction.Transactional;

import java.util.*;

@Action("saveGradingSetting")
public class SaveGradingSetting extends MapResultAction {
    public static final String[] KEYS = {"splits", "questionScore"};
    SaveSettingCommon saveSettingCommon;
    final GradingLevelService gradingLevelService;
    List<Map<String, Object>> levels;
    
    public SaveGradingSetting(GradingLevelService gradingLevelService) {
        this.gradingLevelService = gradingLevelService;
    }
    
    @Override
    public String requiredPermissionName() {
        return "";
    }
    
    @Override
    @Transactional
    protected Optional<LinkedHashMap<String,Object>> doAction() throws Exception {
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
        return Optional.of(getSuccessMap());
    }
    
    @Override
    public void initData(Map<String, Object> dataMap) {
        //noinspection unchecked
        saveSettingCommon = new SaveSettingCommon((Map<String, Object>) dataMap.get("data"),
                KEYS,"grading");
        //noinspection unchecked
        levels = (List<Map<String,Object>>) ((Map<String, Object>) dataMap.get("data")).get("levels");
    }
}
