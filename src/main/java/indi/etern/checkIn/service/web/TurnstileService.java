package indi.etern.checkIn.service.web;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import indi.etern.checkIn.entities.record.RequestRecord;
import indi.etern.checkIn.entities.setting.SettingItem;
import indi.etern.checkIn.service.dao.SettingService;
import indi.etern.checkIn.throwable.TurnstileException;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.coyote.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

@Service
public class TurnstileService {
    private final SettingService settingService;
    @Value("${turnstile.siteVerifyUrl}")
    private String SITEVERIFY_URL;
    private final Logger logger = LoggerFactory.getLogger(TurnstileService.class.getName());

    @Value("${turnstile.safemode:false}")
    protected boolean safemode;

    public TurnstileService(SettingService settingService) {
        this.settingService = settingService;
    }

    public String getSiteKey() {
        try {
            final SettingItem item = settingService.getItem("advance", "turnstileSiteKey");
            return item.getValue(String.class);
        } catch (Exception ignored) {
            return null;
        }
    }

    public String getSecretKey() {
        try {
            final SettingItem item = settingService.getItem("advance", "turnstileSecret");
            return item.getValue(String.class);
        } catch (Exception ignored) {
            return null;
        }
    }

    public boolean isTurnstileEnabledOnLogin() {
        return isTurnstileEnabledOn("enableTurnstileOnLogin");
    }

    public boolean isTurnstileEnabledOnExam() {
        return isTurnstileEnabledOn("enableTurnstileOnExam");
    }

    private boolean isTurnstileEnabledOn(String environment) {
        if (safemode) {
            logger.warn("Turnstile service is in safemode, skipping verification.");
            return false;
        }
        boolean enableTurnstile;
        try {
            final SettingItem item = settingService.getItem("advance", environment);
            enableTurnstile = item.getValue(Boolean.class);
        } catch (Exception ignored) {
            enableTurnstile = false;
        }
        return enableTurnstile;
    }

    public void check(String turnstileToken, HttpServletRequest httpServletRequest) throws BadRequestException, JsonProcessingException, TurnstileException {
        if (safemode) {
            logger.warn("Turnstile service is in safemode, skipping verification.");
            return;
        }
        if (turnstileToken == null || turnstileToken.isEmpty()) {
            throw new BadRequestException("Turnstile token is required");
        } else {
            try (HttpClient client = HttpClient.newHttpClient()) {
                String remoteIp = RequestRecord.getIpOf(httpServletRequest);

                TurnstileRequest turnstileRequest = new TurnstileRequest(getSecretKey(), turnstileToken, remoteIp);
                ObjectMapper objectMapper = new ObjectMapper();
                String requestBody = objectMapper.writeValueAsString(turnstileRequest);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(SITEVERIFY_URL))
                        .header("Content-Type", "application/json") // 关键：告诉服务器发送的是 JSON
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody)) // 设置方法和请求体
                        .timeout(Duration.ofSeconds(10))
                        .build();

                try {
                    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                    int statusCode = response.statusCode();
                    String responseBody = response.body();

                    if (statusCode == 200) {
                        TurnstileResponse turnstileResponse = objectMapper.readValue(responseBody, TurnstileResponse.class);
                        if (turnstileResponse.success) {
                            return;
                        } else {
                            throw new TurnstileException("Turnstile 验证失败");
                        }
                    } else {
                        throw new TurnstileException("Turnstile 请求失败: " + statusCode);
                    }
                } catch (Exception e) {
                    logger.error("While sending verify request to turnstile", e);
                    if (logger.isDebugEnabled()) {
                        e.printStackTrace();
                    }
                    throw new TurnstileException(e.getMessage());
                }
            }
        }
    }

    public boolean isServiceEnable() {
        String siteKey = getSiteKey();
        String secretKey = getSecretKey();
        return siteKey != null && !siteKey.isEmpty() && secretKey != null && !secretKey.isEmpty();
    }

    private record TurnstileRequest(String secret, String response, String remoteip) {
    }

    /**
     {
     "success": true,
     "challenge_ts": "2022-02-28T15:14:30.096Z",
     "hostname": "example.com",
     "error-codes": [],
     "action": "login",
     "cdata": "sessionid-123456789",
     "metadata": {
     "ephemeral_id": "x:9f78e0ed210960d7693b167e"
     }
     }
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    private record TurnstileResponse(
            boolean success,
            String challenge_ts,
            String hostname,
            List<String> error_codes,
            String action,
            String cdata,
            Metadata metadata
    ) {
        @JsonIgnoreProperties(ignoreUnknown = true)
        public record Metadata (String ephemeral_id) {}
    }

}
