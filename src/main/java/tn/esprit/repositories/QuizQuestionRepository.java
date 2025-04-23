package tn.esprit.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.entities.QuizQuestion;

/**
 * Repository Spring Data JPA pour gérer les entités QuizQuestion.
 */
@Repository
public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, Long> {

    // Tu peux ajouter des méthodes personnalisées si besoin plus tard :
    // List<QuizQuestion> findByQuestionContaining(String keyword);
}
