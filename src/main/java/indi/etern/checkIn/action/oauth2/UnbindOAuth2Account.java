package indi.etern.checkIn.action.oauth2;

import indi.etern.checkIn.action.BaseAction;
import indi.etern.checkIn.action.MessageOutput;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.action.interfaces.ExecuteContext;
import indi.etern.checkIn.action.interfaces.InputData;
import indi.etern.checkIn.action.interfaces.OutputData;
import indi.etern.checkIn.entities.user.OAuth2Binding;
import indi.etern.checkIn.entities.user.User;
import indi.etern.checkIn.repositories.OAuth2BindingRepository;
import indi.etern.checkIn.service.dao.UserService;

import java.util.Set;

@Action("unbindOAuth2Account")
public class UnbindOAuth2Account extends BaseAction<UnbindOAuth2Account.Input, OutputData> {
    private final UserService userService;
    private final OAuth2BindingRepository oAuth2BindingRepository;

    public UnbindOAuth2Account(UserService userService, OAuth2BindingRepository oAuth2BindingRepository) {
        super();
        this.userService = userService;
        this.oAuth2BindingRepository = oAuth2BindingRepository;
    }

    @Override
    public void execute(ExecuteContext<Input, OutputData> context) {
        Input input = context.getInput();
        String providerId = input.providerId();
        User currentUser = context.getCurrentUser();
        Set<OAuth2Binding> oauth2Bindings = currentUser.getOauth2Bindings();
        oauth2Bindings.stream()
                .filter(b -> b.getProviderId().equals(providerId))
                .findFirst().ifPresent(entity -> {
                    entity.setUser(null);
                    oAuth2BindingRepository.deleteById(entity.getId());
                    currentUser.getOauth2Bindings().remove(entity);
                    userService.save(currentUser);
                });
        context.resolve(MessageOutput.success("OAuth2 account unbound"));
    }

    public record Input(String providerId) implements InputData {}
}
