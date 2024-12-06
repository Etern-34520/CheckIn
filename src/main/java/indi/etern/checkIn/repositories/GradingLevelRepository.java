package indi.etern.checkIn.repositories;

import indi.etern.checkIn.entities.setting.grading.GradingLevel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GradingLevelRepository extends JpaRepository<GradingLevel, String> {}