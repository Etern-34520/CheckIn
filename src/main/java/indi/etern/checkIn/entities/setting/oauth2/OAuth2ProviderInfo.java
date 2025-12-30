package indi.etern.checkIn.entities.setting.oauth2;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonValue;
import indi.etern.checkIn.entities.converter.ListJsonConverter;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@Table(name = "oauth2_providers")
public class OAuth2ProviderInfo {
    @Embeddable
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Attribute(
            String name,
            @Column(name = "attr_value")
            String value,
            boolean enabled
    ) {
    }


    public enum ExamLoginMode {
        DISABLED, REQUIRED, OPTIONAL;

        @JsonValue
        public String lowerCaseName() {
            return this.name().toLowerCase();
        }

        public static ExamLoginMode of(String s) {
            return Enum.valueOf(ExamLoginMode.class, s.toUpperCase());
        }
    }

    @Id
    private String id;
    private String name;
    private String iconDomain;
    private String issuerUri;
    private String authorizationUri;
    private String userInfoUri;
    private String jwksUri;
    private String clientId;
    private String clientSecret;
    @Convert(converter = ListJsonConverter.class)
    private List<String> scope;
    private String userIdAttributeName;
    private boolean enabledInLogin;
    @Enumerated
    private ExamLoginMode examLoginMode;
    private int orderIndex;

    @ElementCollection(targetClass = Attribute.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "oauth2_providers_info_attributes", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private List<Attribute> otherAttributes;
}