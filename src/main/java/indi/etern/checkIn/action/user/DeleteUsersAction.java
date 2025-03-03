package indi.etern.checkIn.action.user;

import indi.etern.checkIn.action.MapResultAction;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.service.dao.UserService;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Action("delete users")
public class DeleteUsersAction extends MapResultAction {
    
    private final UserService userService;
    private List<Number> qqList;
    
    public DeleteUsersAction(UserService userService) {
        this.userService = userService;
    }
    
    @Override
    public String requiredPermissionName() {
        return "delete user";
    }
    
    @Override
    protected Optional<LinkedHashMap<String, Object>> doAction() throws Exception {
        userService.deleteAllByQQ(qqList.stream().map(Number::longValue).toList());
        return Optional.of(getSuccessMap());
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void initData(Map<String, Object> initData) {
        qqList = (List<Number>) initData.get("qqList");
    }
}
