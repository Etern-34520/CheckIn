package indi.etern.checkIn.entities.robotToken;

import com.fasterxml.jackson.annotation.JsonFormat;
import indi.etern.checkIn.auth.JwtTokenProvider;
import indi.etern.checkIn.entities.BaseEntity;
import indi.etern.checkIn.entities.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "robot_tokens")
public class RobotTokenItem implements BaseEntity<String> {
    @Id
    @Column(columnDefinition = "char(36)")
    String id;
    
    @Column(columnDefinition = "varchar(512)")
    String token;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime generateTime;
    
    @Column(columnDefinition = "varchar(1024)")
    String description;
    
    Long generateByUserQQ;
    
    public static RobotTokenItem generateNewToken(String id, String description, User applicant) {
        final String token = JwtTokenProvider.singletonInstance.generateRobotToken(applicant, id);
        return new RobotTokenItem(id,token,LocalDateTime.now(),description,applicant.getQQNumber());
    }
}