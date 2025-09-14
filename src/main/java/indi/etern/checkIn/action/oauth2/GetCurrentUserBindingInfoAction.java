package indi.etern.checkIn.action.oauth2;

import indi.etern.checkIn.action.BaseAction;
import indi.etern.checkIn.action.NullInput;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.action.interfaces.ExecuteContext;
import indi.etern.checkIn.action.interfaces.OutputData;
import indi.etern.checkIn.entities.user.OAuth2Binding;
import indi.etern.checkIn.entities.user.User;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Action("getCurrentUserBindingInfo")
public class GetCurrentUserBindingInfoAction extends BaseAction<NullInput, GetCurrentUserBindingInfoAction.SuccessOutput> {
    @Override
    public void execute(ExecuteContext<NullInput, SuccessOutput> context) {
        User currentUser = context.getCurrentUser();
        Set<OAuth2Binding> oauth2Bindings = currentUser.getOauth2Bindings();
        Map<String, OAuth2Binding> oauth2BindingsMap = oauth2Bindings.stream()
                .collect(Collectors.toMap(OAuth2Binding::getProviderId, b -> b));
        context.resolve(new SuccessOutput(oauth2BindingsMap));
    }

    public record SuccessOutput(Map<String, OAuth2Binding> oAuth2Bindings) implements OutputData {
        @Override
        public Result result() {
            return Result.SUCCESS;
        }
    }
}
