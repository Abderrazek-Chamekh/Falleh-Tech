package tn.esprit.Controllers.produitfront;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tn.esprit.entities.QuizQuestion;
import tn.esprit.services.QuizService;

import java.util.List;

@RestController
@RequestMapping("/api/quiz")
public class QuizController {

    @Autowired
    private QuizService quizService;

    /**
     * Endpoint pour récupérer les questions du quiz.
     */
    @GetMapping("/start")
    public List<QuizQuestion> startQuiz() {
        return quizService.getQuiz();
    }

    /**
     * Endpoint pour soumettre les réponses.
     * @param answers : Liste des réponses données par l'utilisateur
     * @param clientId : Identifiant du client (pour la remise)
     */
    @PostMapping("/submit")
    public String submitQuiz(@RequestBody List<String> answers, @RequestParam String clientId) {
        boolean passed = quizService.checkAnswers(answers);

        if (passed) {
            // Tu peux appeler ici un service pour appliquer la remise
            return "✅ Bravo ! Vous avez gagné une remise de 15% sur votre panier.";
        } else {
            return "❌ Désolé, une ou plusieurs réponses sont incorrectes.";
        }
    }
}