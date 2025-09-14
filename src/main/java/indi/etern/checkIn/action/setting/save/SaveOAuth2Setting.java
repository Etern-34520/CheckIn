package indi.etern.checkIn.action.setting.save;

import indi.etern.checkIn.action.BaseAction;
import indi.etern.checkIn.action.MessageOutput;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.action.interfaces.ExecuteContext;
import indi.etern.checkIn.action.interfaces.InputData;
import indi.etern.checkIn.entities.setting.SettingItem;
import indi.etern.checkIn.entities.setting.oauth2.OAuth2ProviderInfo;
import indi.etern.checkIn.service.dao.SettingService;
import indi.etern.checkIn.service.web.OAuth2Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Action("saveOAuth2Setting")
public class SaveOAuth2Setting extends BaseAction<SaveOAuth2Setting.Input,MessageOutput> {

    private final SettingService settingService;
    private final OAuth2Service oAuth2Service;

    public SaveOAuth2Setting(SettingService settingService, OAuth2Service oAuth2Service) {
        this.settingService = settingService;
        this.oAuth2Service = oAuth2Service;
    }

    public record OAuth2Data(String appDomainURI, List<OAuth2ProviderInfo> providers) {}

    public record Input(OAuth2Data data) implements InputData {}

    @Transactional
    @Override
    public void execute(ExecuteContext<SaveOAuth2Setting.Input, MessageOutput> context) {
        context.requirePermission("save OAuth2 setting");
        Input input = context.getInput();
        SettingItem settingItem = new SettingItem("oauth2.appDomainURI",
                input.data.appDomainURI, String.class);
        settingService.setSetting(settingItem);
        oAuth2Service.saveProviderInfos(input.data.providers);
        oAuth2Service.reloadRegistrations();
        context.resolve(MessageOutput.success("OAuth2 setting saved"));
    }
}
