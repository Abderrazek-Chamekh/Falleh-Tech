package tn.esprit.services;

import tn.esprit.entities.QuizQuestion;
import tn.esprit.tools.Database;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class QuizService {

    private final Connection cnx;

    public QuizService() {
        cnx = Database.getInstance().getConnection();
    }

    /**
     * Retourne une liste fixe de 3 questions de quiz agricole.
     */
    public List<QuizQuestion> getQuiz() {
        List<QuizQuestion> quiz = new ArrayList<>();

        quiz.add(new QuizQuestion(
                "Quelle plante aime beaucoup le soleil ?",
                Arrays.asList("Tomate", "Champignon", "Menthe"),
                "Tomate"
        ));

        quiz.add(new QuizQuestion(
                "Quel outil utilise-t-on pour retourner la terre ?",
                Arrays.asList("Tracteur", "Arrosoir", "Râteau"),
                "Tracteur"
        ));

        quiz.add(new QuizQuestion(
                "Quelle est la bonne saison pour semer les carottes ?",
                Arrays.asList("Hiver", "Printemps", "Été"),
                "Printemps"
        ));

        return quiz;
    }

    /**
     * Vérifie les réponses fournies par l'utilisateur.
     * @param userAnswers liste des réponses (dans le même ordre que getQuiz)
     * @return true si toutes les réponses sont correctes, false sinon
     */
    public boolean checkAnswers(List<String> userAnswers) {
        List<QuizQuestion> quiz = getQuiz();

        if (userAnswers.size() != quiz.size()) {
            return false;
        }

        for (int i = 0; i < quiz.size(); i++) {
            String expected = quiz.get(i).getAnswer().trim().toLowerCase();
            String actual = userAnswers.get(i).trim().toLowerCase();

            if (!expected.equals(actual)) {
                return false;
            }
        }

        return true;
    }
}
