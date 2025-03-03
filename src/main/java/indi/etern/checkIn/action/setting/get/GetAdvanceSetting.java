package indi.etern.checkIn.action.setting.get;

import indi.etern.checkIn.action.MapResultAction;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.action.setting.save.SaveAdvanceSetting;
import indi.etern.checkIn.entities.robotToken.RobotTokenItem;
import indi.etern.checkIn.service.dao.RobotTokenService;
import indi.etern.checkIn.utils.GetSettingCommon;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

@Action("getAdvanceSetting")
public class GetAdvanceSetting extends MapResultAction {
    
    private final RobotTokenService robotTokenService;
    
    public GetAdvanceSetting(RobotTokenService robotTokenService) {
        super();
        this.robotTokenService = robotTokenService;
    }
    
    @Override
    public String requiredPermissionName() {
        return "get advance setting";
    }
    
    @Override
    protected Optional<LinkedHashMap<String,Object>> doAction() throws Exception {
        GetSettingCommon getSettingCommon = new GetSettingCommon(SaveAdvanceSetting.KEYS,"advance");
        final LinkedHashMap<String,Object> map = new LinkedHashMap<>();
        final LinkedHashMap<String, Object> settings = getSettingCommon.doGet();
        map.put("data", settings);
        final List<RobotTokenItem> all = robotTokenService.findAll();
        final List<RobotTokenItem> sortedList = all.stream().sorted(Comparator.comparing(RobotTokenItem::getGenerateTime)).toList();
        settings.put("robotTokenItems", sortedList);
        return Optional.of(map);
    }
}
