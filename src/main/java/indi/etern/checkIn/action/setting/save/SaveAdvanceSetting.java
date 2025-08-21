package indi.etern.checkIn.action.setting.save;

import indi.etern.checkIn.action.BaseAction;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.action.interfaces.ExecuteContext;
import indi.etern.checkIn.action.interfaces.InputData;
import indi.etern.checkIn.action.interfaces.OutputData;
import indi.etern.checkIn.entities.robotToken.RobotTokenItem;
import indi.etern.checkIn.service.dao.RobotTokenService;
import indi.etern.checkIn.utils.SaveSettingCommon;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Action("saveAdvanceSetting")
public class SaveAdvanceSetting extends BaseAction<SaveAdvanceSetting.Input, SaveAdvanceSetting.RobotTokenOutput> {
    public record Input(Map<String, Object> data) implements InputData {}
    public record RobotTokenOutput(List<RobotTokenItem> currentTokens) implements OutputData {
        @Override
        public Result result() {
            return Result.SUCCESS;
        }
    }
    
    public static final String[] KEYS = {
            "ipSource",
            "useRequestIpIfSourceIsNull",
            "autoCreateUserMode",
            "enableTurnstileOnLogin",
            "enableTurnstileOnExam",
            "turnstileSiteKey",
            "turnstileSecret"
    };
    SaveSettingCommon saveSettingCommon;
    
    private final RobotTokenService robotTokenService;
    
    public SaveAdvanceSetting(RobotTokenService robotTokenService) {
        this.robotTokenService = robotTokenService;
    }
    
    @Transactional
    @Override
    public void execute(ExecuteContext<Input, RobotTokenOutput> context) {
        final Input input = context.getInput();
        saveSettingCommon = new SaveSettingCommon(input.data,
                KEYS, "advance");
        //noinspection unchecked
        List<Map<String,String>> createdRobotTokenList = (List<Map<String, String>>) input.data.get("createdRobotTokens");
        //noinspection unchecked
        List<String> deletedRobotTokenList = (List<String>) input.data.get("deletedRobotTokenIds");
        context.requirePermission("save advance setting");
        saveSettingCommon = new SaveSettingCommon(input.data,
                KEYS, "advance");
        
        saveSettingCommon.doSave();
        if (createdRobotTokenList != null) {
            List<RobotTokenItem> robotTokenItems = new ArrayList<>();
            for (Map<String, String> tokenData : createdRobotTokenList) {
                robotTokenItems.add(RobotTokenItem.generateNewToken(tokenData.get("id"), tokenData.get("description"), context.getCurrentUser()));
            }
            robotTokenService.saveAll(robotTokenItems);
        }
        if (deletedRobotTokenList != null) {
            robotTokenService.deleteAllById(deletedRobotTokenList);
        }
        RobotTokenOutput robotTokenOutput;
        if (createdRobotTokenList != null || deletedRobotTokenList != null) {
            final List<RobotTokenItem> all = robotTokenService.findAll();
            final List<RobotTokenItem> sortedList = all.stream().sorted(Comparator.comparing(RobotTokenItem::getGenerateTime)).toList();
            robotTokenOutput = new RobotTokenOutput(sortedList);
        } else {
            robotTokenOutput = new RobotTokenOutput(null);
        }
        
        context.resolve(robotTokenOutput);
    }
}
