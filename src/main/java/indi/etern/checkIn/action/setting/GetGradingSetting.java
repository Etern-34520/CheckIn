package indi.etern.checkIn.action.setting;

import indi.etern.checkIn.action.MapResultAction;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.entities.setting.grading.GradingLevel;
import indi.etern.checkIn.service.dao.GradingLevelService;

import java.util.LinkedHashMap;
import java.util.Optional;

@Action("getGradingSetting")
public class GetGradingSetting extends MapResultAction {
    final GradingLevelService gradingLevelService;
    
    public GetGradingSetting(GradingLevelService gradingLevelService) {
        this.gradingLevelService = gradingLevelService;
    }
    
    @Override
    public String requiredPermissionName() {
        return "";
    }
    
    @Override
    protected Optional<LinkedHashMap<String,Object>> doAction() throws Exception {
        GetSettingCommon getSettingCommon = new GetSettingCommon(SaveGradingSetting.KEYS,"grading");
        final LinkedHashMap<String,Object> map = new LinkedHashMap<>();
        final LinkedHashMap<String, Object> data = getSettingCommon.doGet();
        final Iterable<GradingLevel> levels = gradingLevelService.findAll();
        data.put("levels", levels);
        map.put("data", data);
        return Optional.of(map);
    }
}
