package indi.etern.checkIn.repositories;

import indi.etern.checkIn.entities.question.interfaces.MultiPartitionableQuestion;
import indi.etern.checkIn.entities.user.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MultiplePartitionableQuestionRepository extends JpaRepository<MultiPartitionableQuestion, String> {
//    List<MultiPartitionableQuestion> findAllByLastEditTimeBefore(LocalDateTime lastEditTime, Sort sort);
    List<MultiPartitionableQuestion> findAllByLastEditTimeAfter(LocalDateTime lastEditTime, Pageable pageable);

    long countByEnabledIsTrue();

    List<MultiPartitionableQuestion> findAllByAuthor(User author);
}
