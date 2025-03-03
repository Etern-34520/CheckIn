package indi.etern.checkIn.repositories;

import indi.etern.checkIn.entities.robotToken.RobotTokenItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RobotTokenRepository extends JpaRepository<RobotTokenItem,String> {
    boolean existsByToken(String token);
}
