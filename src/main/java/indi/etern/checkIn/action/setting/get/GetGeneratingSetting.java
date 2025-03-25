package indi.etern.checkIn.action.setting.get;

import indi.etern.checkIn.action.BaseAction1;
import indi.etern.checkIn.action.NullInput;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.action.interfaces.ExecuteContext;
import indi.etern.checkIn.action.interfaces.OutputData;
import indi.etern.checkIn.action.setting.save.SaveGeneratingSetting;
import indi.etern.checkIn.utils.GetSettingCommon;

import java.util.Map;

@Action("getGeneratingSetting")
public class GetGeneratingSetting extends BaseAction1<NullInput, GetGeneratingSetting.SuccessOutput> {
    public record SuccessOutput(Map<String, Object> data) implements OutputData {
        @Override
        public Result result() {
            return Result.SUCCESS;
        }
    }
    @Override
    public void execute(ExecuteContext<NullInput, SuccessOutput> context) {
        context.requirePermission("get generating setting");
        GetSettingCommon getSettingCommon = new GetSettingCommon(SaveGeneratingSetting.KEYS,"generating");
        context.resolve(new SuccessOutput(getSettingCommon.doGet()));
    }
}
