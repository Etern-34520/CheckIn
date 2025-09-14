package indi.etern.checkIn.entities.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import indi.etern.checkIn.utils.UUIDv7;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "oauth2_bindings", uniqueConstraints = @UniqueConstraint(columnNames = {"provider_id", "user_id"}))
@Getter
@EqualsAndHashCode(of = {"providerId", "userId"})
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OAuth2Binding {
    @Id
    @Column(columnDefinition = "char(36)")
    private String id;

    @Setter
    @Column(name = "provider_id", columnDefinition = "varchar(255)")
    private String providerId;

    @Setter
    @Column(name = "user_id", columnDefinition = "varchar(255)")
    private String userId;

    @JsonIgnore
    @Setter
    @ManyToOne(fetch = FetchType.EAGER, cascade = {CascadeType.REFRESH, CascadeType.DETACH})
    private User user;

    public static OAuth2Binding of(String providerId, String sub) {
        return new OAuth2Binding(UUIDv7.randomUUID().toString(), providerId, sub, null);
    }
}