package net.darmo_creations.jenealogio2.ui.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import net.darmo_creations.jenealogio2.App;
import net.darmo_creations.jenealogio2.model.Gender;
import net.darmo_creations.jenealogio2.model.Person;
import net.darmo_creations.jenealogio2.model.calendar.CalendarDate;
import net.darmo_creations.jenealogio2.model.calendar.DateAlternative;
import net.darmo_creations.jenealogio2.model.calendar.DateRange;
import net.darmo_creations.jenealogio2.model.calendar.DateWithPrecision;
import net.darmo_creations.jenealogio2.ui.ChildInfo;
import net.darmo_creations.jenealogio2.ui.PseudoClasses;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * A JavaFX component representing a single person in a family tree.
 */
// TODO add icon to indicate parents/children if they are not shown
public class PersonWidget extends AnchorPane {
  public static final int WIDTH = 100;
  public static final int HEIGHT = 140;

  private static final String EMPTY_LABEL_VALUE = "-";
  private static final String BIRTH_SYMBOL = "°";
  private static final String DEATH_SYMBOL = "†";

  @SuppressWarnings("DataFlowIssue")
  private static final Image DEFAULT_IMAGE =
      new Image(PersonWidget.class.getResourceAsStream(App.IMAGES_PATH + "default_person_image.png"));
  @SuppressWarnings("DataFlowIssue")
  private static final Image ADD_IMAGE =
      new Image(PersonWidget.class.getResourceAsStream(App.IMAGES_PATH + "add_person_image.png"));

  private final List<ClickListener> clickListeners = new LinkedList<>();

  private final Person person;
  private final ChildInfo childInfo;
  private PersonWidget parentWidget1;
  private PersonWidget parentWidget2;

  private final AnchorPane imagePane = new AnchorPane();
  private final ImageView imageView = new ImageView();
  private final Label firstNameLabel = new Label();
  private final Label lastNameLabel = new Label();
  private final Label birthDateLabel = new Label();
  private final Label deathDateLabel = new Label();

  private boolean selected;

  /**
   * Create a component for the given person.
   *
   * @param person    A person object.
   * @param childInfo Information about the displayed child this widget is a parent of.
   */
  public PersonWidget(final Person person, final ChildInfo childInfo) {
    this.person = person;
    this.childInfo = childInfo;
    this.getStyleClass().add("person-widget");

    this.setPrefWidth(WIDTH);
    this.setPrefHeight(HEIGHT);
    this.setMinWidth(this.getPrefWidth());
    this.setMaxWidth(this.getPrefWidth());

    VBox pane = new VBox();
    AnchorPane.setTopAnchor(pane, 0.0);
    AnchorPane.setBottomAnchor(pane, 0.0);
    AnchorPane.setLeftAnchor(pane, 0.0);
    AnchorPane.setRightAnchor(pane, 0.0);
    this.getChildren().add(pane);

    int size = 54;
    this.imagePane.setPrefSize(size, size);
    this.imagePane.setMinSize(size, size);
    this.imagePane.setMaxSize(size, size);
    this.imagePane.setPadding(new Insets(2));
    HBox imageBox = new HBox(this.imagePane);
    imageBox.setAlignment(Pos.CENTER);
    pane.getChildren().add(imageBox);
    AnchorPane.setTopAnchor(this.imageView, 0.0);
    AnchorPane.setBottomAnchor(this.imageView, 0.0);
    AnchorPane.setLeftAnchor(this.imageView, 0.0);
    AnchorPane.setRightAnchor(this.imageView, 0.0);
    this.imageView.setPreserveRatio(true);
    this.imageView.setFitHeight(50);
    this.imageView.setFitWidth(50);
    this.imagePane.getChildren().add(this.imageView);

    VBox infoPane = new VBox(this.firstNameLabel, this.lastNameLabel, this.birthDateLabel, this.deathDateLabel);
    infoPane.getStyleClass().add("person-data");
    pane.getChildren().add(infoPane);

    this.setOnMouseClicked(this::onClick);

    this.populateFields();
  }

  /**
   * The person object wrapped by this component.
   */
  public Optional<Person> person() {
    return Optional.ofNullable(this.person);
  }

  /**
   * Information about the visible child this widget is a parent of.
   */
  public Optional<ChildInfo> childInfo() {
    return Optional.ofNullable(this.childInfo);
  }

  public Optional<PersonWidget> parentWidget1() {
    return Optional.ofNullable(this.parentWidget1);
  }

  public void setParentWidget1(PersonWidget parentWidget1) {
    this.parentWidget1 = parentWidget1;
  }

  public Optional<PersonWidget> parentWidget2() {
    return Optional.ofNullable(this.parentWidget2);
  }

  public void setParentWidget2(PersonWidget parentWidget2) {
    this.parentWidget2 = parentWidget2;
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
    this.clickListeners.forEach(
        clickListener -> clickListener.onClick(this, mouseEvent.getClickCount(), mouseEvent.getButton()));
    mouseEvent.consume();
  }

  private void populateFields() {
    if (this.person == null) {
      this.imageView.setImage(ADD_IMAGE);
      this.getStyleClass().add("add-parent");
      return;
    }

    this.imageView.setImage(this.person.getImage().orElse(DEFAULT_IMAGE));
    String genderColor = this.person.gender().map(Gender::color).orElse(Gender.MISSING_COLOR);
    this.imagePane.setStyle("-fx-background-color: " + genderColor);

    String firstNames = this.person.getFirstNames().orElse(EMPTY_LABEL_VALUE);
    this.firstNameLabel.setText(firstNames);
    this.firstNameLabel.setTooltip(new Tooltip(firstNames));

    String lastName = this.person.getLastName().orElse(EMPTY_LABEL_VALUE);
    this.lastNameLabel.setText(lastName);
    this.lastNameLabel.setTooltip(new Tooltip(lastName));

    String birthDate = this.person.getBirthDate().map(this::formatDate).orElse("?");
    this.birthDateLabel.setText("%s %s".formatted(BIRTH_SYMBOL, birthDate));
    this.birthDateLabel.setTooltip(new Tooltip(birthDate));

    if (this.person.lifeStatus().isConsideredDeceased()) {
      String deathDate = this.person.getDeathDate().map(this::formatDate).orElse("?");
      this.deathDateLabel.setText("%s %s".formatted(DEATH_SYMBOL, deathDate));
      this.deathDateLabel.setTooltip(new Tooltip(deathDate));
    }
  }

  private String formatDate(@NotNull CalendarDate date) {
    if (date instanceof DateWithPrecision d) {
      int year = d.date().getYear();
      return switch (d.precision()) {
        case EXACT -> String.valueOf(year);
        case ABOUT -> "~ " + year;
        case POSSIBLY -> year + " ?";
        case BEFORE -> "< " + year;
        case AFTER -> "> " + year;
      };
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
    void onClick(@NotNull PersonWidget personWidget, int clickCount, MouseButton button);
  }
}
