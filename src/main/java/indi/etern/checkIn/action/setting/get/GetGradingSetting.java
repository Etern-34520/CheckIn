package indi.etern.checkIn.action.setting.get;

import indi.etern.checkIn.action.MapResultAction;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.action.setting.save.SaveGradingSetting;
import indi.etern.checkIn.entities.setting.grading.GradingLevel;
import indi.etern.checkIn.service.dao.GradingLevelService;
import indi.etern.checkIn.service.dao.SettingService;
import indi.etern.checkIn.utils.GetSettingCommon;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Action("getGradingSetting")
public class GetGradingSetting extends MapResultAction {
    final GradingLevelService gradingLevelService;
    private final SettingService settingService;
    
    public GetGradingSetting(GradingLevelService gradingLevelService, SettingService settingService) {
        this.gradingLevelService = gradingLevelService;
        this.settingService = settingService;
    }
    
    @Override
    public String requiredPermissionName() {
        return null;
    }
    
    @Override
    protected Optional<LinkedHashMap<String,Object>> doAction() throws Exception {
        GetSettingCommon getSettingCommon = new GetSettingCommon(SaveGradingSetting.KEYS,"grading");
        final LinkedHashMap<String,Object> map = new LinkedHashMap<>();
        final LinkedHashMap<String, Object> data = getSettingCommon.doGet();
        final Iterable<GradingLevel> levels = gradingLevelService.findAll();
        data.put("levels", levels);
        map.put("data", data);
        Map<String,Object> extraData = new LinkedHashMap<>();
        extraData.put("questionAmount", settingService.getItem("generating","questionAmount").getValue(Integer.class));
        extraData.put("questionScore", settingService.getItem("generating","questionScore").getValue(Integer.class));
        map.put("extraData", extraData);
        return Optional.of(map);
    }
}
