package indi.etern.checkIn.action.setting.get;

import indi.etern.checkIn.action.BaseAction;
import indi.etern.checkIn.action.NullInput;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.action.interfaces.ExecuteContext;
import indi.etern.checkIn.action.interfaces.OutputData;
import indi.etern.checkIn.action.setting.save.SaveOAuth2Setting;
import indi.etern.checkIn.entities.setting.oauth2.OAuth2ProviderInfo;
import indi.etern.checkIn.service.dao.SettingService;
import indi.etern.checkIn.service.web.OAuth2Service;

import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

@Action("getOAuth2Setting")
public class GetOAuth2Setting extends BaseAction<NullInput, GetOAuth2Setting.SuccessOutput> {
    private final SettingService settingService;
    private final OAuth2Service oAuth2Service;

    public GetOAuth2Setting(SettingService settingService, OAuth2Service oAuth2Service) {
        this.settingService = settingService;
        this.oAuth2Service = oAuth2Service;
    }

    public record SuccessOutput(SaveOAuth2Setting.OAuth2Data data) implements OutputData {
        @Override
        public Result result() {
            return Result.SUCCESS;
        }
    }
    @Override
    public void execute(ExecuteContext<NullInput, SuccessOutput> context) {
        context.requirePermission("get OAuth2 setting");
        String appDomainURI;
        try {
            appDomainURI = settingService.getItem("oauth2","appDomainURI").getValue(String.class);
        } catch (NoSuchElementException ignored) {
            appDomainURI = null;
        }
        List<OAuth2ProviderInfo> providers = oAuth2Service.getProviderInfos();
        providers.sort(Comparator.comparingInt(OAuth2ProviderInfo::getOrderIndex));
        context.resolve(new SuccessOutput(new SaveOAuth2Setting.OAuth2Data(appDomainURI,providers)));
    }
}
