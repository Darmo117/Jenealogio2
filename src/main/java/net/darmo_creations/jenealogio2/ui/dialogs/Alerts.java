package net.darmo_creations.jenealogio2.ui.dialogs;

import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import net.darmo_creations.jenealogio2.App;
import net.darmo_creations.jenealogio2.config.Language;
import net.darmo_creations.jenealogio2.utils.FormatArg;
import net.darmo_creations.jenealogio2.utils.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * This class provides methods to open alert dialogs.
 */
public final class Alerts {
  /**
   * Open an alert dialog to show some information.
   *
   * @param headerKey   Header text key.
   * @param contentKey  Content text key.
   * @param titleKey    Title key.
   * @param contentArgs Format arguments to apply to the header, content and title.
   */
  public static void info(
      @NotNull String headerKey, String contentKey, String titleKey, final FormatArg @NotNull ... contentArgs) {
    alert(Alert.AlertType.INFORMATION, headerKey, contentKey, titleKey, contentArgs);
  }

  /**
   * Open an alert dialog to show a warning message.
   *
   * @param headerKey   Header text key.
   * @param contentKey  Content text key.
   * @param titleKey    Title key.
   * @param contentArgs Format arguments to apply to the header, content and title.
   */
  public static void warning(
      @NotNull String headerKey, String contentKey, String titleKey, final FormatArg @NotNull ... contentArgs) {
    alert(Alert.AlertType.WARNING, headerKey, contentKey, titleKey, contentArgs);
  }

  /**
   * Open an alert dialog to show an error message.
   *
   * @param headerKey   Header text key.
   * @param contentKey  Content text key.
   * @param titleKey    Title key.
   * @param contentArgs Format arguments to apply to the header, content and title.
   */
  public static void error(
      @NotNull String headerKey, String contentKey, String titleKey, final FormatArg @NotNull ... contentArgs) {
    alert(Alert.AlertType.ERROR, headerKey, contentKey, titleKey, contentArgs);
  }

  /**
   * Open an alert dialog to prompt the user for confirmation.
   *
   * @param headerKey   Header text key.
   * @param contentKey  Content text key.
   * @param titleKey    Title key.
   * @param contentArgs Format arguments to apply to the header, content and title.
   * @return True if the user clicked OK, false if they clicked CANCEL or dismissed the dialog.
   */
  public static boolean confirmation(
      @NotNull String headerKey, String contentKey, String titleKey, final FormatArg @NotNull ... contentArgs) {
    Optional<ButtonType> result = alert(Alert.AlertType.CONFIRMATION, headerKey, contentKey, titleKey, contentArgs);
    return result.isPresent() && !result.get().getButtonData().isCancelButton();
  }

  /**
   * Open an alert dialog to ask the user to choose an option from a combobox.
   *
   * @param headerKey   Header text key.
   * @param titleKey    Title key.
   * @param choices     The choices for the combobox.
   * @param contentArgs Format arguments to apply to the header and title.
   * @return The selected item.
   */
  public static <T> Optional<T> chooser(
      @NotNull String headerKey, String titleKey, final Collection<T> choices, final FormatArg @NotNull ... contentArgs) {
    if (choices.isEmpty()) {
      throw new IllegalArgumentException("empty choices");
    }
    Alert alert = getAlert(Alert.AlertType.CONFIRMATION, headerKey, titleKey, contentArgs);
    HBox hBox = new HBox();
    ComboBox<T> choicesCombo = new ComboBox<>();
    choicesCombo.getItems().addAll(choices);
    choicesCombo.getSelectionModel().select(0);
    // TODO add label
    hBox.getChildren().add(choicesCombo);
    hBox.setAlignment(Pos.CENTER);
    alert.getDialogPane().setContent(hBox);
    Optional<ButtonType> buttonType = alert.showAndWait();
    if (buttonType.isPresent() && !buttonType.get().getButtonData().isCancelButton()) {
      return Optional.of(choicesCombo.getSelectionModel().getSelectedItem());
    }
    return Optional.empty();
  }

  /**
   * Open an alert dialog to prompt the use to input some text.
   *
   * @param headerKey   Header text key.
   * @param labelKey    Text field label text key.
   * @param titleKey    Title key.
   * @param defaultText Text to put into the text field.
   * @param contentArgs Format arguments to apply to the header, label and title.
   * @return The selected item.
   */
  public static Optional<String> textInput(
      @NotNull String headerKey,
      @NotNull String labelKey,
      String titleKey,
      String defaultText,
      final FormatArg @NotNull ... contentArgs
  ) {
    Alert alert = getAlert(Alert.AlertType.CONFIRMATION, headerKey, titleKey, contentArgs);
    HBox hBox = new HBox(4);
    TextField textField = new TextField();
    textField.textProperty().addListener((observable, oldValue, newValue) ->
        alert.getDialogPane().lookupButton(ButtonTypes.OK).setDisable(StringUtils.stripNullable(newValue).isEmpty()));
    textField.setText(defaultText);
    Label label = new Label(App.config().language().translate(labelKey, contentArgs));
    hBox.getChildren().addAll(label, textField);
    hBox.setAlignment(Pos.CENTER);
    alert.getDialogPane().setContent(hBox);
    Optional<ButtonType> buttonType = alert.showAndWait();
    if (buttonType.isPresent() && !buttonType.get().getButtonData().isCancelButton()) {
      return StringUtils.stripNullable(textField.getText());
    }
    return Optional.empty();
  }

  private static Optional<ButtonType> alert(
      @NotNull Alert.AlertType type,
      @NotNull String headerKey,
      String contentKey,
      String titleKey,
      final @NotNull FormatArg... contentArgs
  ) {
    Alert alert = getAlert(type, headerKey, titleKey, contentArgs);
    if (contentKey != null) {
      alert.setContentText(App.config().language().translate(contentKey, contentArgs));
    }
    return alert.showAndWait();
  }

  /**
   * Create a basic alert dialog.
   *
   * @param type        Alert type. {@link Alert.AlertType#NONE} is not allowed.
   * @param headerKey   Header text key.
   * @param titleKey    Title key.
   * @param contentArgs Format arguments to apply to the header and title.
   * @return The alert dialog.
   */
  private static Alert getAlert(
      @NotNull Alert.AlertType type, @NotNull String headerKey, String titleKey, final FormatArg @NotNull ... contentArgs) {
    if (type == Alert.AlertType.NONE) {
      throw new IllegalArgumentException(type.name());
    }
    Alert alert = new Alert(type);
    // Replace default buttons to have proper translations
    alert.getDialogPane().getButtonTypes().setAll(switch (type) {
      case INFORMATION, WARNING, ERROR -> List.of(ButtonTypes.OK);
      case CONFIRMATION -> List.of(ButtonTypes.OK, ButtonTypes.CANCEL);
      case NONE -> throw new IllegalArgumentException(type.name()); // Should never happen
    });
    App.config().theme().getStyleSheets()
        .forEach(url -> alert.getDialogPane().getStylesheets().add(url.toExternalForm()));
    if (titleKey == null) {
      titleKey = "alert.%s.title".formatted(type.name().toLowerCase());
    }
    Language language = App.config().language();
    alert.setTitle(language.translate(titleKey));
    alert.setHeaderText(language.translate(headerKey, contentArgs));
    return alert;
  }

  private Alerts() {
  }
}
