package indi.etern.checkIn.action.setting.get;

import indi.etern.checkIn.action.BaseAction1;
import indi.etern.checkIn.action.NullInput;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.action.interfaces.ExecuteContext;
import indi.etern.checkIn.action.interfaces.OutputData;
import indi.etern.checkIn.action.setting.save.SaveAdvanceSetting;
import indi.etern.checkIn.entities.robotToken.RobotTokenItem;
import indi.etern.checkIn.service.dao.RobotTokenService;
import indi.etern.checkIn.utils.GetSettingCommon;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Action("getAdvanceSetting")
public class GetAdvanceSetting extends BaseAction1<NullInput, GetAdvanceSetting.SuccessOutput> {
    public record SuccessOutput(Map<String, Object> data) implements OutputData {
        @Override
        public Result result() {
            return Result.SUCCESS;
        }
    }
    
    private final RobotTokenService robotTokenService;
    
    public GetAdvanceSetting(RobotTokenService robotTokenService) {
        this.robotTokenService = robotTokenService;
    }
    
    @Override
    public void execute(ExecuteContext<NullInput, SuccessOutput> context) {
        context.requirePermission("get advance setting");
        GetSettingCommon getSettingCommon = new GetSettingCommon(SaveAdvanceSetting.KEYS,"advance");
        final LinkedHashMap<String, Object> settings = getSettingCommon.doGet();
        final List<RobotTokenItem> all = robotTokenService.findAll();
        final List<RobotTokenItem> sortedList = all.stream().sorted(Comparator.comparing(RobotTokenItem::getGenerateTime)).toList();
        settings.put("robotTokenItems", sortedList);
        context.resolve(new SuccessOutput(settings));
    }
}
