package indi.etern.checkIn.repositories;

import indi.etern.checkIn.entities.question.impl.Question;
import indi.etern.checkIn.entities.user.User;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, String> {
    List<Question> findAllByAuthor(User author);
    List<Question> findAllByUpVotersContains(User user);
    List<Question> findAllByDownVotersContains(User user);
    List<Question> findAllByAuthor(User user, Pageable pageable);

    @EntityGraph(value = "Question.LinkWrapper", type = EntityGraph.EntityGraphType.FETCH)
    List<Question> findAllByLastModifiedTimeBeforeAndLinkWrapper_Class(LocalDateTime localDateTime, Class<?> linkClass, Sort sort, Limit limit);
}