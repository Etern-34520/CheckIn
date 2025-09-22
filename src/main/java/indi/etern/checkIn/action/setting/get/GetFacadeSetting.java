package indi.etern.checkIn.action.setting.get;

import indi.etern.checkIn.action.BaseAction;
import indi.etern.checkIn.action.NullInput;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.action.interfaces.ExecuteContext;
import indi.etern.checkIn.action.interfaces.OutputData;
import indi.etern.checkIn.action.setting.save.SaveFacadeSetting;
import indi.etern.checkIn.service.dao.SettingService;
import indi.etern.checkIn.service.exam.StatusService;
import indi.etern.checkIn.utils.GetSettingCommon;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

@Action("getFacadeSetting")
public class GetFacadeSetting extends BaseAction<NullInput, GetFacadeSetting.SuccessOutput> {
    private final StatusService statusService;

    public record SuccessOutput(Map<String, Object> data, Map<String, Object> extraData) implements OutputData {
        @Override
        public Result result() {
            return Result.SUCCESS;
        }
    }
    
    private final SettingService settingService;
    
    public GetFacadeSetting(SettingService settingService, StatusService statusService) {
        this.settingService = settingService;
        this.statusService = statusService;
    }
    
    @Override
    public void execute(ExecuteContext<NullInput, SuccessOutput> context) {
        GetSettingCommon getSettingCommon = new GetSettingCommon(SaveFacadeSetting.KEYS, "facade");
        final LinkedHashMap<String, Object> extraData = new LinkedHashMap<>();
        final StatusService.Status generateStatus = statusService.getStatus().generateAvailability().status();
        final StatusService.Status submitStatus = statusService.getStatus().submitAvailability().status();
        boolean serviceAvailable = generateStatus != StatusService.Status.UNAVAILABLE && submitStatus != StatusService.Status.UNAVAILABLE;
        extraData.put("serviceAvailable", serviceAvailable);
        extraData.put("questionAmount", settingService.getItem("generating", "questionAmount").getValue(Integer.class));
        extraData.put("questionScore", settingService.getItem("generating", "questionScore").getValue(Integer.class));
        extraData.put("partitionRange", settingService.getItem("generating", "partitionRange").getValue(ArrayList.class));
        context.resolve(new SuccessOutput(getSettingCommon.doGet(), extraData));
    }
}
