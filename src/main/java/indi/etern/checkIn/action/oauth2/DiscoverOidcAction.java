package indi.etern.checkIn.action.oauth2;

import indi.etern.checkIn.action.BaseAction;
import indi.etern.checkIn.action.MessageOutput;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.action.interfaces.ExecuteContext;
import indi.etern.checkIn.action.interfaces.InputData;
import indi.etern.checkIn.action.interfaces.OutputData;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.List;
import java.util.Map;

@Action("discoverOidc")
public class DiscoverOidcAction extends BaseAction<DiscoverOidcAction.Input, OutputData> {
    public record Input(String issuerUrl) implements InputData {}
    public record OidcData(String authorizationUri, String userInfoUri,
                           String jwksUri, List<String> supportedScopes) {}
    public record SuccessOutput(OidcData data) implements OutputData {
        @Override
        public Result result() {
            return Result.SUCCESS;
        }
    }

    @Override
    public void execute(ExecuteContext<Input, OutputData> context) {
        context.requirePermission("save OAuth2 setting");
        Input input = context.getInput();
        String issuerUrl = input.issuerUrl();
        if (isValidUrl(issuerUrl)) {
            RestTemplate restTemplate = new RestTemplate();
            //noinspection rawtypes
            ResponseEntity<Map> discoveryResponse = restTemplate.getForEntity(issuerUrl, Map.class);
            //noinspection unchecked
            Map<String, Object> discoveryMap = discoveryResponse.getBody();

            if (discoveryMap == null) {
                context.resolve(MessageOutput.error("Failed to fetch discovery document"));
                return;
            }

            //noinspection unchecked
            context.resolve(new SuccessOutput(new OidcData(
                    (String) discoveryMap.get("authorization_endpoint"),
                    (String) discoveryMap.get("userinfo_endpoint"),
                    (String) discoveryMap.get("jwks_uri"),
                    (List<String>) discoveryMap.get("scopes_supported")
            )));
        } else {
            context.resolve(MessageOutput.error("Invalid URL format"));
        }
    }

    private boolean isValidUrl(String urlString) {
        try {
            //noinspection ResultOfMethodCallIgnored
            new URI(urlString).toURL();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
