package indi.etern.checkIn.action.setting.get;

import indi.etern.checkIn.action.MapResultAction;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.action.setting.save.SaveGeneratingSetting;
import indi.etern.checkIn.utils.GetSettingCommon;

import java.util.LinkedHashMap;
import java.util.Optional;

@Action("getGeneratingSetting")
public class GetGeneratingSetting extends MapResultAction {
    
    @Override
    public String requiredPermissionName() {
        return "get generating setting";
    }
    
    @Override
    protected Optional<LinkedHashMap<String,Object>> doAction() throws Exception {
        GetSettingCommon getSettingCommon = new GetSettingCommon(SaveGeneratingSetting.KEYS,"generating");
        final LinkedHashMap<String,Object> map = new LinkedHashMap<>();
        map.put("data",getSettingCommon.doGet());
        return Optional.of(map);
    }
}
