package indi.etern.checkIn.repositories;

import indi.etern.checkIn.entities.question.impl.Choice;
import org.springframework.data.jpa.repository.JpaRepository;


public interface ChoiceRepository extends JpaRepository<Choice,String> {
}
