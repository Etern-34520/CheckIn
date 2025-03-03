package indi.etern.checkIn.action.setting.get;

import indi.etern.checkIn.action.MapResultAction;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.action.setting.save.SaveFacadeSetting;
import indi.etern.checkIn.service.dao.GradingLevelService;
import indi.etern.checkIn.service.dao.SettingService;
import indi.etern.checkIn.utils.GetSettingCommon;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Optional;

@Action("getFacadeSetting")
public class GetFacadeSetting extends MapResultAction {
    
    private final SettingService settingService;
    
    public GetFacadeSetting(SettingService settingService, GradingLevelService gradingLevelService) {
        super();
        this.settingService = settingService;
    }
    
    @Override
    public String requiredPermissionName() {
        return null;
    }
    
    @Override
    protected Optional<LinkedHashMap<String, Object>> doAction() throws Exception {
        GetSettingCommon getSettingCommon = new GetSettingCommon(SaveFacadeSetting.KEYS, "facade");
        final LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put("data", getSettingCommon.doGet());
        final LinkedHashMap<Object, Object> extraData = new LinkedHashMap<>();
        extraData.put("questionAmount", settingService.getItem("generating", "questionAmount").getValue(Integer.class));
        extraData.put("questionScore", settingService.getItem("generating", "questionScore").getValue(Integer.class));
        extraData.put("partitionRange", settingService.getItem("generating", "partitionRange").getValue(ArrayList.class));
        final Boolean showRequiredPartitions = settingService.getItem("generating", "showRequiredPartitions").getValue(Boolean.class);
        if (showRequiredPartitions)
            extraData.put("requiredPartitions", settingService.getItem("generating", "requiredPartitions").getValue(ArrayList.class));
        map.put("extraData", extraData);
        return Optional.of(map);
    }
}
