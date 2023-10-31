package indi.etern.checkIn.repositories;

import indi.etern.checkIn.entities.traffic.UserTraffic;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserTrafficRepository extends JpaRepository<UserTraffic, Integer> {
}
