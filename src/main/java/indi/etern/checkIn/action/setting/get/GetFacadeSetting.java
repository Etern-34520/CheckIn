package indi.etern.checkIn.action.setting.get;

import indi.etern.checkIn.action.BaseAction1;
import indi.etern.checkIn.action.NullInput;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.action.interfaces.ExecuteContext;
import indi.etern.checkIn.action.interfaces.OutputData;
import indi.etern.checkIn.action.setting.save.SaveFacadeSetting;
import indi.etern.checkIn.service.dao.GradingLevelService;
import indi.etern.checkIn.service.dao.SettingService;
import indi.etern.checkIn.utils.GetSettingCommon;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

@Action("getFacadeSetting")
public class GetFacadeSetting extends BaseAction1<NullInput, GetFacadeSetting.SuccessOutput> {
    public record SuccessOutput(Map<String, Object> data, Map<String, Object> extraData) implements OutputData {
        @Override
        public Result result() {
            return Result.SUCCESS;
        }
    }
    
    private final SettingService settingService;
    
    public GetFacadeSetting(SettingService settingService, GradingLevelService gradingLevelService) {
        this.settingService = settingService;
    }
    
    @Override
    public void execute(ExecuteContext<NullInput, SuccessOutput> context) {
        GetSettingCommon getSettingCommon = new GetSettingCommon(SaveFacadeSetting.KEYS, "facade");
        final LinkedHashMap<String, Object> extraData = new LinkedHashMap<>();
        extraData.put("questionAmount", settingService.getItem("generating", "questionAmount").getValue(Integer.class));
        extraData.put("questionScore", settingService.getItem("generating", "questionScore").getValue(Integer.class));
        extraData.put("partitionRange", settingService.getItem("generating", "partitionRange").getValue(ArrayList.class));
        final Boolean showRequiredPartitions = settingService.getItem("generating", "showRequiredPartitions").getValue(Boolean.class);
        if (showRequiredPartitions)
            extraData.put("requiredPartitions", settingService.getItem("generating", "requiredPartitions").getValue(ArrayList.class));
        context.resolve(new SuccessOutput(getSettingCommon.doGet(), extraData));
    }
}
