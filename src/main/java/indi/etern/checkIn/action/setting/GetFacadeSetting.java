package indi.etern.checkIn.action.setting;

import java.util.LinkedHashMap;
import indi.etern.checkIn.action.MapResultAction;
import indi.etern.checkIn.action.interfaces.Action;

import java.util.Optional;

@Action("getFacadeSetting")
public class GetFacadeSetting extends MapResultAction {
    
    @Override
    public String requiredPermissionName() {
        return "";
    }
    
    @Override
    protected Optional<LinkedHashMap<String,Object>> doAction() throws Exception {
        GetSettingCommon getSettingCommon = new GetSettingCommon(SaveFacadeSetting.KEYS,"facade");
        final LinkedHashMap<String,Object> map = new LinkedHashMap<>();
        map.put("data",getSettingCommon.doGet());
        return Optional.of(map);
    }
}
