package tn.esprit.controllers.ouvrier;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import tn.esprit.entities.OuvrierCalendrier;
import tn.esprit.services.OuvrierCalendrierService;

import java.net.URL;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class CalendrierOuvrierController implements Initializable {

    @FXML private GridPane calendarGrid;
    @FXML private Label monthLabel;
    @FXML private Button prevMonthBtn, nextMonthBtn;

    private YearMonth currentYearMonth = YearMonth.now();
    private final OuvrierCalendrierService calendrierService = new OuvrierCalendrierService();
    private final int loggedInOuvrierId = 21; // ✅ Replace with session-based user ID

    private List<OuvrierCalendrier> acceptedTasks = new ArrayList<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        acceptedTasks = calendrierService.getCalendrierByUser(loggedInOuvrierId).stream()
                .filter(c -> c.getStatus().equalsIgnoreCase("accepted"))
                .toList();

        populateCalendar(currentYearMonth);
    }

    @FXML
    private void goToPreviousMonth() {
        currentYearMonth = currentYearMonth.minusMonths(1);
        populateCalendar(currentYearMonth);
    }

    @FXML
    private void goToNextMonth() {
        currentYearMonth = currentYearMonth.plusMonths(1);
        populateCalendar(currentYearMonth);
    }

    private void populateCalendar(YearMonth yearMonth) {
        calendarGrid.getChildren().clear();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.FRENCH);
        monthLabel.setText(yearMonth.format(formatter));

        LocalDate firstDay = yearMonth.atDay(1);
        int startDay = (firstDay.getDayOfWeek().getValue() % 7); // Monday = 0
        int daysInMonth = yearMonth.lengthOfMonth();

        int row = 1, col = startDay;


        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate currentDate = yearMonth.atDay(day);
            VBox cell = createDayCell(day);

            // ✅ Highlight today
            if (currentDate.equals(LocalDate.now())) {
                cell.getStyleClass().add("today-cell");
            }

            // ✅ Highlight if current date is within any accepted task range
            for (OuvrierCalendrier task : acceptedTasks) {
                LocalDate start = task.getStartDate().toLocalDate();
                LocalDate end = task.getEndDate().toLocalDate();
                if ((currentDate.isEqual(start) || currentDate.isAfter(start)) &&
                        (currentDate.isEqual(end) || currentDate.isBefore(end))) {
                    cell.getStyleClass().add("accepted-day");
                    break;
                }
            }

            calendarGrid.add(cell, col, row);
            col++;
            if (col > 6) {
                col = 0;
                row++;
            }
        }

    }

    private VBox createDayCell(int day) {
        VBox cell = new VBox();
        cell.setAlignment(Pos.CENTER);
        cell.setSpacing(5);

        Rectangle bg = new Rectangle(80, 60);
        bg.setArcHeight(10);
        bg.setArcWidth(10);
        bg.setFill(Color.web("#f9f9f9"));
        bg.setStroke(Color.web("#ccc"));

        Label label = new Label(String.valueOf(day));
        label.setStyle("-fx-font-size: 14px;");

        cell.getChildren().addAll(bg, label);
        cell.getStyleClass().add("calendar-cell");
        return cell;
    }
}
