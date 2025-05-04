
// GridManager.java
package tn.esprit.fx;

import javafx.scene.Node;
import javafx.scene.layout.GridPane;

import java.util.HashSet;
import java.util.Set;

public class GridManager {
    private final GridPane gridPane;
    private final Set<String> usedSlots = new HashSet<>();
    private Node widgetToMove = null;

    public GridManager(GridPane gridPane) {
        this.gridPane = gridPane;
    }

    public boolean addWidget(Node widget) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                String key = row + "-" + col;
                if (!usedSlots.contains(key)) {
                    GridPane.setRowIndex(widget, row);
                    GridPane.setColumnIndex(widget, col);
                    gridPane.getChildren().add(widget);
                    usedSlots.add(key);
                    return true;
                }
            }
        }
        return false;
    }

    public void removeWidget(Node widget) {
        gridPane.getChildren().remove(widget);
        usedSlots.remove(getWidgetKey(widget));
    }

    public void activateMoveMode(Node widget) {
        this.widgetToMove = widget;
    }

    public void onCellClicked(int row, int col) {
        if (widgetToMove != null) {
            String newKey = row + "-" + col;

            Node toMove = widgetToMove;
            gridPane.getChildren().remove(toMove);

            GridPane.setRowIndex(toMove, row);
            GridPane.setColumnIndex(toMove, col);
            gridPane.getChildren().add(toMove);

            widgetToMove = null;
            System.out.println("Moved widget to " + newKey);
        }
    }

    private String getWidgetKey(Node node) {
        Integer row = GridPane.getRowIndex(node);
        Integer col = GridPane.getColumnIndex(node);
        return (row != null ? row : 0) + "-" + (col != null ? col : 0);
    }

    public void clear() {
        gridPane.getChildren().clear();
        usedSlots.clear();
    }
}
