package net.darmo_creations.jenealogio2.ui.components;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import net.darmo_creations.jenealogio2.App;
import net.darmo_creations.jenealogio2.model.Gender;
import net.darmo_creations.jenealogio2.model.Person;
import net.darmo_creations.jenealogio2.model.calendar.*;
import net.darmo_creations.jenealogio2.ui.PseudoClasses;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

/**
 * A JavaFX component representing a single person in a family tree.
 */
// TODO add icon to indicate parents/children if they are not shown
public class PersonWidget extends AnchorPane {
  private static final String EMPTY_LABEL_VALUE = "-";
  @SuppressWarnings("DataFlowIssue")
  public static final Image DEFAULT_IMAGE =
      new Image(PersonWidget.class.getResourceAsStream(App.IMAGES_PATH + "default_person_image.png"));

  private final List<ClickListener> clickListeners = new LinkedList<>();

  private final Person person;

  private final AnchorPane imagePane = new AnchorPane();
  private final ImageView imageView = new ImageView();
  private final Label firstNameLabel = new Label();
  private final Label lastNameLabel = new Label();
  private final Label lifeSpanLabel = new Label();

  private boolean selected;

  /**
   * Create a component for the given person.
   *
   * @param person A person object.
   */
  public PersonWidget(final @NotNull Person person) {
    this.person = person;
    this.getStyleClass().add("person-widget");

    this.setPrefWidth(200);
    this.setMinWidth(this.getPrefWidth());
    this.setMaxWidth(this.getPrefWidth());

    HBox hBox = new HBox();
    AnchorPane.setTopAnchor(hBox, 0.0);
    AnchorPane.setBottomAnchor(hBox, 0.0);
    AnchorPane.setLeftAnchor(hBox, 0.0);
    AnchorPane.setRightAnchor(hBox, 0.0);
    this.getChildren().add(hBox);

    hBox.getChildren().add(this.imagePane);
    int size = 54;
    this.imagePane.setPrefSize(size, size);
    this.imagePane.setMinSize(size, size);
    this.imagePane.setMaxSize(size, size);
    this.imagePane.setPadding(new Insets(2));
    AnchorPane.setTopAnchor(this.imageView, 0.0);
    AnchorPane.setBottomAnchor(this.imageView, 0.0);
    AnchorPane.setLeftAnchor(this.imageView, 0.0);
    AnchorPane.setRightAnchor(this.imageView, 0.0);
    this.imageView.setPreserveRatio(true);
    this.imageView.setFitHeight(50);
    this.imageView.setFitWidth(50);
    this.imagePane.getChildren().add(this.imageView);

    VBox vBox = new VBox();
    vBox.getStyleClass().add("person-data");
    hBox.getChildren().add(vBox);
    HBox.setHgrow(vBox, Priority.ALWAYS);

    vBox.getChildren().addAll(this.firstNameLabel, this.lastNameLabel, this.lifeSpanLabel);

    this.setOnMouseClicked(this::onClick);

    this.refresh();
  }

  /**
   * The person object wrapped by this component.
   */
  public Person person() {
    return this.person;
  }

  /**
   * Indicate whether this component is selected.
   */
  public boolean isSelected() {
    return this.selected;
  }

  /**
   * Set the selection of this component.
   */
  public void setSelected(boolean selected) {
    this.selected = selected;
    this.pseudoClassStateChanged(PseudoClasses.SELECTED, selected);
  }

  /**
   * List of click listeners.
   */
  public List<ClickListener> clickListeners() {
    return this.clickListeners;
  }

  private void onClick(@NotNull MouseEvent mouseEvent) {
    this.clickListeners.forEach(clickListener -> clickListener.onClick(this, mouseEvent.getClickCount()));
    mouseEvent.consume();
  }

  public void refresh() {
    this.imageView.setImage(this.person.getImage().orElse(DEFAULT_IMAGE));
    String genderColor = this.person.gender().map(Gender::color).orElse(Gender.MISSING_COLOR);
    this.imagePane.setStyle("-fx-background-color: " + genderColor);

    String firstNames = this.person.getFirstNames().orElse(EMPTY_LABEL_VALUE);
    this.firstNameLabel.setText(firstNames);
    this.firstNameLabel.setTooltip(new Tooltip(firstNames));

    String lastName = this.person.getLastName().orElse(EMPTY_LABEL_VALUE);
    this.lastNameLabel.setText(lastName);
    this.lastNameLabel.setTooltip(new Tooltip(lastName));

    String lifeSpan = this.person.getBirthDate().map(this::formatDate).orElse("?");
    if (this.person.lifeStatus().isConsideredDeceased()) {
      lifeSpan += " - " + this.person.getDeathDate().map(this::formatDate).orElse("?");
    }
    this.lifeSpanLabel.setText(lifeSpan);
    this.lifeSpanLabel.setTooltip(new Tooltip(lifeSpan));
  }

  private String formatDate(@NotNull CalendarDate date) {
    if (date instanceof DateWithPrecision d) {
      return (d.precision() == DatePrecision.EXACT ? "" : "~") + d.date().getYear();
    } else if (date instanceof DateRange d) {
      int startYear = d.startDate().getYear();
      int endYear = d.endDate().getYear();
      return startYear != endYear ? "%s~%s".formatted(startYear, endYear) : String.valueOf(startYear);
    } else if (date instanceof DateAlternative d) {
      int earliestYear = d.earliestDate().getYear();
      int latestYear = d.latestDate().getYear();
      return earliestYear != latestYear ? "%s/%s".formatted(earliestYear, latestYear) : String.valueOf(earliestYear);
    }
    throw new IllegalArgumentException("unexpected date type: " + date.getClass());
  }

  public interface ClickListener {
    void onClick(@NotNull PersonWidget personWidget, int clickCount);
  }
}