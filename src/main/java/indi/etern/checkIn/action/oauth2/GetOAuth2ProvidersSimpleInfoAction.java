package indi.etern.checkIn.action.oauth2;

import com.fasterxml.jackson.annotation.JsonIgnore;
import indi.etern.checkIn.action.BaseAction;
import indi.etern.checkIn.action.NullInput;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.action.interfaces.ExecuteContext;
import indi.etern.checkIn.action.interfaces.OutputData;
import indi.etern.checkIn.entities.setting.oauth2.OAuth2ProviderInfo;
import indi.etern.checkIn.service.web.OAuth2Service;

import java.util.Comparator;
import java.util.List;

@Action("getOAuth2ProvidersSimpleInfo")
public class GetOAuth2ProvidersSimpleInfoAction extends BaseAction<NullInput, GetOAuth2ProvidersSimpleInfoAction.SuccessOutput> {
    private final OAuth2Service oAuth2Service;

    public GetOAuth2ProvidersSimpleInfoAction(OAuth2Service oAuth2Service) {
        this.oAuth2Service = oAuth2Service;
    }

    @Override
    public void execute(ExecuteContext<NullInput, SuccessOutput> context) {
        List<OAuth2ProviderInfo> providerInfos = oAuth2Service.getProviderInfos();
        providerInfos.sort(Comparator.comparingInt(OAuth2ProviderInfo::getOrderIndex));
        List<ProviderItem> providers = providerInfos.stream().map(ProviderItem::from).toList();
        context.resolve(new SuccessOutput(providers, providerInfos));
    }

    public record SuccessOutput(List<ProviderItem> providers, @JsonIgnore List<OAuth2ProviderInfo> providerInfos) implements OutputData {
        @Override
        public Result result() {
            return Result.SUCCESS;
        }
    }

    public record ProviderItem(String id, String name, String iconDomain) {
        public static ProviderItem from(OAuth2ProviderInfo info) {
            return new ProviderItem(info.getId(), info.getName(), info.getIconDomain());
        }
    }
}
