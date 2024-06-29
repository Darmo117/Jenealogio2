package net.darmo_creations.jenealogio2.ui.components;

import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import net.darmo_creations.jenealogio2.*;
import net.darmo_creations.jenealogio2.config.*;
import net.darmo_creations.jenealogio2.model.*;
import net.darmo_creations.jenealogio2.model.datetime.*;
import net.darmo_creations.jenealogio2.themes.*;
import net.darmo_creations.jenealogio2.ui.*;
import org.jetbrains.annotations.*;

import java.util.*;
import java.util.stream.*;

/**
 * A JavaFX component representing a single person in the {@link FamilyTreePane}.
 */
public class PersonWidget extends AnchorPane {
  public static final int WIDTH = 120;
  public static final int HEIGHT = 180;

  private static final String EMPTY_LABEL_VALUE = "?";

  @SuppressWarnings("DataFlowIssue")
  public static final Image DEFAULT_IMAGE =
      new Image(PersonWidget.class.getResourceAsStream(App.IMAGES_PATH + "default_person_image.png"));
  @SuppressWarnings("DataFlowIssue")
  public static final Image ADD_IMAGE =
      new Image(PersonWidget.class.getResourceAsStream(App.IMAGES_PATH + "add_person_image.png"));

  private final Config config;
  private final List<ClickListener> clickListeners = new LinkedList<>();

  private final Person person;
  private final List<ChildInfo> childInfo = new ArrayList<>();
  private PersonWidget parentWidget1;
  private PersonWidget parentWidget2;

  private final ImageView imageView = new ImageView();
  private final Label firstNameLabel = new Label();
  private final Label lastNameLabel = new Label();
  private final Label birthDateLabel = new Label();
  private final Label deathDateLabel = new Label();

  private boolean selected;

  /**
   * Create a component for the given person.
   *
   * @param person       A person object.
   * @param childInfo    Information about the displayed children this widget is a parent of.
   * @param showMoreIcon Whether to show the “plus” icon.
   * @param isTarget     Whether the widget is targetted.
   * @param isRoot       Whether the person is the tree’s root.
   * @param config       The app’s config.
   */
  public PersonWidget(
      final Person person,
      final @NotNull List<ChildInfo> childInfo,
      boolean showMoreIcon,
      boolean isTarget,
      boolean isRoot,
      final @NotNull Config config
  ) {
    this.person = person;
    this.config = Objects.requireNonNull(config);
    this.childInfo.addAll(childInfo);
    this.getStyleClass().add("person-widget");
    if (isTarget) {
      this.getStyleClass().add("center");
    }

    this.setPrefWidth(WIDTH);
    this.setPrefHeight(HEIGHT);
    this.setMinWidth(this.getPrefWidth());
    this.setMaxWidth(this.getPrefWidth());

    VBox pane = new VBox(5);
    AnchorPane.setTopAnchor(pane, 0.0);
    AnchorPane.setBottomAnchor(pane, 0.0);
    AnchorPane.setLeftAnchor(pane, 0.0);
    AnchorPane.setRightAnchor(pane, 0.0);
    this.getChildren().add(pane);

    BorderPane iconsBox = new BorderPane();
    if (person != null && person.disambiguationID().isPresent()) {
      int id = person.disambiguationID().get();
      Label idLabel = new Label("#" + id);
      iconsBox.setLeft(idLabel);
    } else {
      iconsBox.setLeft(new Label()); // Empty label for proper alignment
    }
    HBox iconsRightBox = new HBox(5);
    iconsBox.setRight(iconsRightBox);
    if (isRoot) {
      Label rootIcon = new Label(null, config.theme().getIcon(Icon.TREE_ROOT, Icon.Size.SMALL));
      rootIcon.setTooltip(new Tooltip(config.language().translate("person_widget.root.tooltip")));
      iconsRightBox.getChildren().add(rootIcon);
    }
    if (showMoreIcon) {
      Label moreIcon = new Label(null, config.theme().getIcon(Icon.MORE, Icon.Size.SMALL));
      moreIcon.setTooltip(new Tooltip(config.language().translate("person_widget.more_icon.tooltip")));
      iconsRightBox.getChildren().add(moreIcon);
    }
    if (person != null && person.gender().isPresent()) {
      Gender gender = person.gender().get();
      boolean builtin = gender.isBuiltin();
      String label;
      if (builtin)
        label = config.language().translate("genders." + gender.key().name());
      else
        label = Objects.requireNonNull(gender.userDefinedName());
      Label genderIcon = new Label(null, new ImageView(gender.icon()));
      genderIcon.setTooltip(new Tooltip(label));
      iconsRightBox.getChildren().add(genderIcon);
    }
    pane.getChildren().add(iconsBox);

    final int imageSize = 50;
    this.imageView.setPreserveRatio(true);
    this.imageView.setFitHeight(imageSize);
    this.imageView.setFitWidth(imageSize);
    HBox imageBox = new HBox(this.imageView);
    imageBox.setAlignment(Pos.CENTER);
    imageBox.setMinHeight(imageSize);
    imageBox.setMinWidth(imageSize);
    pane.getChildren().add(imageBox);

    VBox infoPane = new VBox(5, this.firstNameLabel, this.lastNameLabel, this.birthDateLabel, this.deathDateLabel);
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
  public List<ChildInfo> childInfo() {
    return new ArrayList<>(this.childInfo);
  }

  /**
   * The widget representing the parent 1 of this wrapped person.
   */
  public Optional<PersonWidget> parentWidget1() {
    return Optional.ofNullable(this.parentWidget1);
  }

  /**
   * Set the widget representing the parent 1 of this wrapped person.
   *
   * @param parentWidget1 The widget.
   */
  public void setParentWidget1(PersonWidget parentWidget1) {
    this.parentWidget1 = parentWidget1;
  }

  /**
   * The widget representing the parent 1 of this wrapped person.
   */
  public Optional<PersonWidget> parentWidget2() {
    return Optional.ofNullable(this.parentWidget2);
  }

  /**
   * Set the widget representing the parent 2 of this wrapped person.
   *
   * @param parentWidget2 The widget.
   */
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

  /**
   * Called when this widget is clicked. Notifies all {@link ClickListener}s.
   *
   * @param mouseEvent The mouse event.
   */
  private void onClick(@NotNull MouseEvent mouseEvent) {
    this.clickListeners.forEach(
        clickListener -> clickListener.onClick(this, mouseEvent.getClickCount(), mouseEvent.getButton()));
    mouseEvent.consume();
  }

  /**
   * Populate labels with data from the wrapped person object.
   */
  private void populateFields() {
    if (this.person == null) {
      this.imageView.setImage(ADD_IMAGE);
      this.getStyleClass().add("add-parent");
      return;
    }

    this.imageView.setImage(this.person.mainPicture().flatMap(Picture::image).orElse(DEFAULT_IMAGE));

    String firstNames = this.person.getFirstNames().orElse(EMPTY_LABEL_VALUE);
    this.firstNameLabel.setText(firstNames);
    this.firstNameLabel.setTooltip(new Tooltip(firstNames));

    String lastName = this.person.getLastName().orElse(EMPTY_LABEL_VALUE);
    this.lastNameLabel.setText(lastName);
    this.lastNameLabel.setTooltip(new Tooltip(lastName));

    String birthYear = this.person.getBirthDate().map(PersonWidget::formatDateYear).orElse(EMPTY_LABEL_VALUE);
    this.birthDateLabel.setText(birthYear);
    this.birthDateLabel.setGraphic(this.config.theme().getIcon(Icon.BIRTH, Icon.Size.SMALL));
    this.birthDateLabel.setTooltip(new Tooltip(birthYear));

    if (this.person.lifeStatus().isConsideredDeceased()) {
      String deathYear = this.person.getDeathDate().map(PersonWidget::formatDateYear).orElse(EMPTY_LABEL_VALUE);
      this.deathDateLabel.setText(deathYear);
      this.deathDateLabel.setGraphic(this.config.theme().getIcon(Icon.DEATH, Icon.Size.SMALL));
      this.deathDateLabel.setTooltip(new Tooltip(deathYear));
    }
  }

  public static String formatDateYear(@NotNull DateTime date) {
    if (date instanceof DateTimeWithPrecision d) {
      int year = d.date().toISO8601Date().getYear();
      return switch (d.precision()) {
        case EXACT -> String.valueOf(year);
        case ABOUT -> "~ " + year;
        case POSSIBLY -> "? " + year;
        case BEFORE -> "< " + year;
        case AFTER -> "> " + year;
      };
    } else if (date instanceof DateTimeRange d) {
      int startYear = d.startDate().toISO8601Date().getYear();
      int endYear = d.endDate().toISO8601Date().getYear();
      return startYear != endYear ? "%s-%s".formatted(startYear, endYear) : String.valueOf(startYear);
    } else if (date instanceof DateTimeAlternative da) {
      List<Integer> years = da.dates().stream()
          .map(d -> d.toISO8601Date().getYear())
          .distinct() // Not using Collectors.toSet() to keep the order
          .toList();
      if (years.size() == 1)
        return "~ " + years.get(0);
      return years.stream().map(String::valueOf).collect(Collectors.joining("/"));
    }
    throw new IllegalArgumentException("unexpected date type: " + date.getClass());
  }

  /**
   * Interface for click listeners.
   */
  public interface ClickListener {
    void onClick(@NotNull PersonWidget personWidget, int clickCount, MouseButton button);
  }
}
