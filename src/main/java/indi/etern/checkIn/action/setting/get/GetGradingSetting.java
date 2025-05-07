package indi.etern.checkIn.action.setting.get;

import indi.etern.checkIn.action.BaseAction;
import indi.etern.checkIn.action.NullInput;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.action.interfaces.ExecuteContext;
import indi.etern.checkIn.action.interfaces.OutputData;
import indi.etern.checkIn.action.setting.save.SaveGradingSetting;
import indi.etern.checkIn.entities.setting.grading.GradingLevel;
import indi.etern.checkIn.service.dao.GradingLevelService;
import indi.etern.checkIn.service.dao.SettingService;
import indi.etern.checkIn.utils.GetSettingCommon;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

@Action("getGradingSetting")
public class GetGradingSetting extends BaseAction<NullInput, GetGradingSetting.SuccessOutput> {
    public record SuccessOutput(Map<String, Object> data, Map<String, Object> extraData) implements OutputData {
        @Override
        public Result result() {
            return Result.SUCCESS;
        }
    }
    
    final GradingLevelService gradingLevelService;
    private final SettingService settingService;
    
    public GetGradingSetting(GradingLevelService gradingLevelService, SettingService settingService) {
        this.gradingLevelService = gradingLevelService;
        this.settingService = settingService;
    }
    
    @Override
    @Transactional(readOnly = true)
    public void execute(ExecuteContext<NullInput, SuccessOutput> context) {
        GetSettingCommon getSettingCommon = new GetSettingCommon(SaveGradingSetting.KEYS,"grading");
        final LinkedHashMap<String, Object> data = getSettingCommon.doGet();
        final Iterable<GradingLevel> levels = gradingLevelService.findAll();
        data.put("levels", levels);
        
        Map<String,Object> extraData = new LinkedHashMap<>();
        extraData.put("questionAmount", settingService.getItem("generating","questionAmount").getValue(Integer.class));
        extraData.put("questionScore", settingService.getItem("generating","questionScore").getValue(Integer.class));
        
        context.resolve(new SuccessOutput(data,extraData));
    }
}
