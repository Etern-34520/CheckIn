package indi.etern.checkIn.repositories;

import indi.etern.checkIn.entities.user.Role;
import indi.etern.checkIn.entities.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findAllByRole(Role role);
}
