package indi.etern.checkIn.action.setting.save;

import indi.etern.checkIn.action.TransactionalAction;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.entities.robotToken.RobotTokenItem;
import indi.etern.checkIn.service.dao.RobotTokenService;
import indi.etern.checkIn.utils.SaveSettingCommon;
import org.springframework.web.servlet.FrameworkServlet;

import java.util.*;

@Action("saveAdvanceSetting")
public class SaveAdvanceSetting extends TransactionalAction {
    public static final String[] KEYS = {
            "ipSource",
            "useRequestIpIfSourceIsNull",
            "autoCreateUserMode"
    };
    SaveSettingCommon saveSettingCommon;
    private List<Map<String, String>> createdRobotTokenList;
    private List<String> deletedRobotTokenList;
    private final RobotTokenService robotTokenService;
    
    public SaveAdvanceSetting(RobotTokenService robotTokenService, FrameworkServlet frameworkServlet) {
        this.robotTokenService = robotTokenService;
    }
    
    @Override
    public String requiredPermissionName() {
        return "save advance setting";
    }
    
    @Override
    protected Optional<LinkedHashMap<String, Object>> doAction() throws Exception {
        saveSettingCommon.doSave();
        final LinkedHashMap<String, Object> successMap = getSuccessMap();
        if (createdRobotTokenList != null) {
            List<RobotTokenItem> robotTokenItems = new ArrayList<>();
            for (Map<String, String> tokenData : createdRobotTokenList) {
                robotTokenItems.add(RobotTokenItem.generateNewToken(tokenData.get("id"), tokenData.get("description"), getCurrentUser()));
            }
            robotTokenService.saveAll(robotTokenItems);
        }
        if (deletedRobotTokenList != null) {
            robotTokenService.deleteAllById(deletedRobotTokenList);
        }
        if (createdRobotTokenList != null || deletedRobotTokenList != null) {
            final List<RobotTokenItem> all = robotTokenService.findAll();
            final List<RobotTokenItem> sortedList = all.stream().sorted(Comparator.comparing(RobotTokenItem::getGenerateTime)).toList();
            successMap.put("currentTokens",sortedList);
        }
        return Optional.of(successMap);
    }
    
    @Override
    public void initData(Map<String, Object> dataMap) {
        //noinspection unchecked
        final Map<String, Object> data = (Map<String, Object>) dataMap.get("data");
        saveSettingCommon = new SaveSettingCommon(data,
                KEYS, "advance");
        //noinspection unchecked
        createdRobotTokenList = (List<Map<String, String>>) data.get("createdRobotTokens");
        //noinspection unchecked
        deletedRobotTokenList = (List<String>) data.get("deletedRobotTokenIds");
    }
}
