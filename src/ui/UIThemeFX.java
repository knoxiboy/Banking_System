package Projects_java.Banking_System.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.Optional;

/**
 * UIThemeFX — Design system & styling utilities for JavaFX Banking Application.
 * Employs modern styling, typography, and standard dialog helpers.
 */
public class UIThemeFX {

    // ─── Color Palette Hex Constants ──────────────────────────────────────────
    public static final String PRIMARY_HEX       = "#0D47A1"; // Deep Indigo
    public static final String PRIMARY_HOVER_HEX = "#1976D2"; // Bright Blue
    public static final String SUCCESS_HEX       = "#2E7D32"; // Forest Green
    public static final String SUCCESS_HOVER_HEX = "#388E3C";
    public static final String DANGER_HEX        = "#C62828"; // Crimson
    public static final String DANGER_HOVER_HEX  = "#D32F2F";
    public static final String WARNING_HEX       = "#EF6C00"; // Amber
    public static final String BG_LIGHT_HEX      = "#F5F7FA"; // Cloud Gray
    public static final String CARD_BG_HEX       = "#FFFFFF";
    public static final String TEXT_DARK_HEX     = "#212121";
    public static final String TEXT_MUTED_HEX    = "#757575";
    public static final String BORDER_HEX        = "#CFD8DC";

    // ─── Header Banner ────────────────────────────────────────────────────────
    public static VBox createHeader(String titleText, String subtitleText) {
        VBox header = new VBox(4);
        header.setPadding(new Insets(18, 24, 18, 24));
        header.setStyle("-fx-background-color: " + PRIMARY_HEX + ";");

        Label title = new Label(titleText);
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        title.setTextFill(Color.WHITE);

        Label sub = new Label(subtitleText);
        sub.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 13));
        sub.setTextFill(Color.web("#E3F2FD"));

        header.getChildren().addAll(title, sub);
        return header;
    }

    // ─── Button Helpers ───────────────────────────────────────────────────────
    public static Button createButton(String text, String bgHex, String hoverHex) {
        Button btn = new Button(text);
        btn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        btn.setTextFill(Color.WHITE);
        String normalStyle = "-fx-background-color: " + bgHex + "; -fx-background-radius: 6px; -fx-padding: 8 16 8 16; -fx-cursor: hand;";
        String hoverStyle  = "-fx-background-color: " + hoverHex + "; -fx-background-radius: 6px; -fx-padding: 8 16 8 16; -fx-cursor: hand;";
        btn.setStyle(normalStyle);

        btn.setOnMouseEntered(e -> btn.setStyle(hoverStyle));
        btn.setOnMouseExited(e -> btn.setStyle(normalStyle));
        return btn;
    }

    // ─── Form Inputs ──────────────────────────────────────────────────────────
    public static TextField createTextField(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setFont(Font.font("Segoe UI", 13));
        tf.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: " + BORDER_HEX + "; -fx-border-radius: 5px; -fx-background-radius: 5px; -fx-padding: 8 10 8 10;");
        return tf;
    }

    public static PasswordField createPasswordField(String prompt) {
        PasswordField pf = new PasswordField();
        pf.setPromptText(prompt);
        pf.setFont(Font.font("Segoe UI", 13));
        pf.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: " + BORDER_HEX + "; -fx-border-radius: 5px; -fx-background-radius: 5px; -fx-padding: 8 10 8 10;");
        return pf;
    }

    public static VBox createFormField(String labelText, Control inputControl) {
        VBox box = new VBox(4);
        Label lbl = new Label(labelText);
        lbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        lbl.setTextFill(Color.web(TEXT_DARK_HEX));
        box.getChildren().addAll(lbl, inputControl);
        return box;
    }

    // ─── Dialog Helpers ───────────────────────────────────────────────────────
    public static void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static Optional<String> promptPinDialog(String title, String message) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(message);

        ButtonType confirmButtonType = new ButtonType("Confirm", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);

        PasswordField pinField = createPasswordField("Enter 4-6 digit PIN");
        VBox content = new VBox(10);
        content.setPadding(new Insets(15));
        content.getChildren().add(pinField);
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == confirmButtonType) {
                return pinField.getText().trim();
            }
            return null;
        });

        return dialog.showAndWait();
    }
}

