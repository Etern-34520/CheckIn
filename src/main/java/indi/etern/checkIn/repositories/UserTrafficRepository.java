package indi.etern.checkIn.repositories;

import indi.etern.checkIn.entities.traffic.UserTraffic;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface UserTrafficRepository extends JpaRepository<UserTraffic, Integer> {
    Integer countByQqNumber(long qqNumber);
    
    List<UserTraffic> findAllByQqNumber(Long qq);

    List<UserTraffic> findAllByLocalDate(LocalDate localDate, Sort sort);
}