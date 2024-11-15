package indi.etern.checkIn.action.setting;

import indi.etern.checkIn.action.MapResultAction;
import indi.etern.checkIn.action.interfaces.Action;

import java.util.LinkedHashMap;
import java.util.Optional;

@Action("getDrawingSetting")
public class GetDrawingSetting extends MapResultAction {
    
    @Override
    public String requiredPermissionName() {
        return "";
    }
    
    @Override
    protected Optional<LinkedHashMap<String,Object>> doAction() throws Exception {
        GetSettingCommon getSettingCommon = new GetSettingCommon(SaveDrawingSetting.KEYS,"drawing");
        final LinkedHashMap<String,Object> map = new LinkedHashMap<>();
        map.put("data",getSettingCommon.doGet());
        return Optional.of(map);
    }
}
