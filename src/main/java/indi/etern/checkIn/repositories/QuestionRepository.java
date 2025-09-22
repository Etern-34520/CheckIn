package indi.etern.checkIn.repositories;

import indi.etern.checkIn.entities.linkUtils.impl.QuestionLinkImpl;
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
//    List<Question> findAllByLastEditTimeBefore(LocalDateTime lastEditTime, Sort sort);
    List<Question> findAllByLastModifiedTimeAfter(LocalDateTime lastEditTime, Pageable pageable);

    long countByEnabledIsTrue();

    List<Question> findAllByAuthor(User author);
    List<Question> findAllByUpVotersContains(User user);
    List<Question> findAllByDownVotersContains(User user);
    List<Question> findAllByAuthor(User user, Pageable pageable);
    
//    List<Question> findAllByLastModifiedTimeBeforeAndLinkWrapper();

    @EntityGraph(value = "Question.LinkWrapper", type = EntityGraph.EntityGraphType.FETCH)
    List<Question> findAllByLastModifiedTimeBeforeAndLinkWrapper_LinkType(LocalDateTime localDateTime, QuestionLinkImpl.LinkType linkType, Sort sort, Limit limit);
//    List<Question> findAll(Example<Question> notSubQuestionExample, LocalDateTime now, Sort lastModifiedTime, Limit of);
}