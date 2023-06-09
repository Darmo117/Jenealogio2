package net.darmo_creations.jenealogio2.ui.dialogs;

import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.converter.IntegerStringConverter;
import net.darmo_creations.jenealogio2.App;
import net.darmo_creations.jenealogio2.config.Language;
import net.darmo_creations.jenealogio2.model.*;
import net.darmo_creations.jenealogio2.model.datetime.DateTimePrecision;
import net.darmo_creations.jenealogio2.model.datetime.DateTimeWithPrecision;
import net.darmo_creations.jenealogio2.model.datetime.calendar.Calendar;
import net.darmo_creations.jenealogio2.model.datetime.calendar.CalendarDateTime;
import net.darmo_creations.jenealogio2.themes.Icon;
import net.darmo_creations.jenealogio2.themes.Theme;
import net.darmo_creations.jenealogio2.ui.ChildInfo;
import net.darmo_creations.jenealogio2.ui.PseudoClasses;
import net.darmo_creations.jenealogio2.ui.components.*;
import net.darmo_creations.jenealogio2.utils.FormatArg;
import net.darmo_creations.jenealogio2.utils.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.Collator;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Dialog to edit a {@link Person} object and its {@link LifeEvent}s.
 */
public class EditPersonDialog extends DialogBase<Person> {
  /**
   * Index of the profile tab.
   */
  public static final int TAB_PROFILE = 0;
  /**
   * Index of the life events tab.
   */
  public static final int TAB_EVENTS = 1;
  /**
   * Index of the parents and relatives tab.
   */
  public static final int TAB_PARENTS = 2;

  private final TabPane tabPane = new TabPane();

  private final ComboBox<NotNullComboBoxItem<LifeStatus>> lifeStatusCombo = new ComboBox<>();
  private final TextField legalLastNameField = new TextField();
  private final TextField legalFirstNamesField = new TextField();
  private final TextField publicLastNameField = new TextField();
  private final TextField publicFirstNamesField = new TextField();
  private final TextField nicknamesField = new TextField();
  private final ComboBox<ComboBoxItem<Gender>> genderCombo = new ComboBox<>();
  private final TextField mainOccupationField = new TextField();
  private final TextField disambiguationIDField = new TextField();
  private final TextArea notesField = new TextArea();
  private final TextArea sourcesField = new TextArea();

  private final Button addEventButton = new Button();
  private final ListView<LifeEventView> lifeEventsList = new ListView<>();

  private final ComboBox<ComboBoxItem<Person>> parent1Combo = new ComboBox<>();
  private final ComboBox<ComboBoxItem<Person>> parent2Combo = new ComboBox<>();

  private final Map<Person.RelativeType, RelativesListView> relativesLists = new HashMap<>();

  /**
   * The person object being edited.
   */
  private Person person;
  /**
   * If not null, indicates the person the one being edited should be a parent of.
   */
  private List<ChildInfo> childInfo;
  /**
   * The family tree the person belongs to..
   */
  private FamilyTree familyTree;
  /**
   * Whether the person is being created.
   */
  private boolean creating;
  /**
   * Stores the life status to be restored if the user selects an event type
   * that indicates death but reverts it later on.
   */
  private LifeStatus lifeStatusCache;
  /**
   * Indicates whether an update event from the {@link #lifeStatusCombo} is internal or from the user.
   */
  private boolean internalLifeStatusChange;
  /**
   * Set of life events to delete.
   */
  private final Set<LifeEventView> eventsToDelete = new HashSet<>();

  /**
   * Create a person edit dialog.
   */
  public EditPersonDialog() {
    super("edit_person", true, ButtonTypes.OK, ButtonTypes.CANCEL);

    this.tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
    this.tabPane.getTabs().add(this.createProfileTab());
    this.tabPane.getTabs().add(this.createEventsTab());
    this.tabPane.getTabs().add(this.createParentsTab());

    this.getDialogPane().setContent(this.tabPane);

    Stage stage = this.stage();
    stage.setMinWidth(850);
    stage.setMinHeight(650);

    this.setResultConverter(buttonType -> {
      if (!buttonType.getButtonData().isCancelButton()) {
        this.updatePerson(this.person);
        if (this.creating) {
          this.familyTree.addPerson(this.person);
          for (ChildInfo info : this.childInfo) {
            info.child().setParent(info.parentIndex(), this.person);
          }
        }
        return this.person;
      }
      return null;
    });
  }

  private Tab createProfileTab() {
    Language language = App.config().language();
    Theme theme = App.config().theme();

    Tab tab = new Tab(language.translate("dialog.edit_person.profile.title"));

    GridPane gridPane = new GridPane();
    gridPane.setHgap(4);
    gridPane.setVgap(4);

    {
      this.lifeStatusCombo.getItems().addAll(Arrays.stream(LifeStatus.values())
          .map(lifeStatus -> {
            String text = language.translate("life_status." + lifeStatus.name().toLowerCase());
            return new NotNullComboBoxItem<>(lifeStatus, text);
          })
          .toList());
      this.lifeStatusCombo.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
        if (!this.internalLifeStatusChange) {
          this.lifeStatusCache = newValue.data();
        }
      });
      this.addRow(gridPane, 0, "dialog.edit_person.profile.life_status", this.lifeStatusCombo);
      RowConstraints rc = new RowConstraints();
      rc.setVgrow(Priority.SOMETIMES);
      gridPane.getRowConstraints().add(rc);
    }

    {
      Label label = new Label(language.translate("dialog.edit_person.profile.legal_last_name"));
      Label helpLabel = new Label(null, theme.getIcon(Icon.HELP, Icon.Size.SMALL));
      helpLabel.setTooltip(new Tooltip(language.translate("dialog.edit_person.profile.legal_last_name.tooltip")));
      HBox hBox = new HBox(5, label, helpLabel);
      gridPane.addRow(1, hBox, this.legalLastNameField);
      RowConstraints rc = new RowConstraints();
      rc.setVgrow(Priority.SOMETIMES);
      gridPane.getRowConstraints().add(rc);
    }

    {
      Label label = new Label(language.translate("dialog.edit_person.profile.legal_first_names"));
      Label helpLable = new Label(null, theme.getIcon(Icon.HELP, Icon.Size.SMALL));
      helpLable.setTooltip(new Tooltip(language.translate("dialog.edit_person.profile.legal_first_names.tooltip")));
      HBox hBox = new HBox(5, label, helpLable);
      gridPane.addRow(2, hBox, this.legalFirstNamesField);
      RowConstraints rc = new RowConstraints();
      rc.setVgrow(Priority.SOMETIMES);
      gridPane.getRowConstraints().add(rc);
    }

    {
      Label label = new Label(language.translate("dialog.edit_person.profile.public_last_name"));
      Label helpLabel = new Label(null, theme.getIcon(Icon.HELP, Icon.Size.SMALL));
      helpLabel.setTooltip(new Tooltip(language.translate("dialog.edit_person.profile.public_last_name.tooltip")));
      HBox hBox = new HBox(5, label, helpLabel);
      gridPane.addRow(3, hBox, this.publicLastNameField);
      RowConstraints rc = new RowConstraints();
      rc.setVgrow(Priority.SOMETIMES);
      gridPane.getRowConstraints().add(rc);
    }

    {
      Label label = new Label(language.translate("dialog.edit_person.profile.public_first_names"));
      Label helpLabel = new Label(null, theme.getIcon(Icon.HELP, Icon.Size.SMALL));
      helpLabel.setTooltip(new Tooltip(language.translate("dialog.edit_person.profile.public_first_names.tooltip")));
      HBox hBox = new HBox(5, label, helpLabel);
      gridPane.addRow(4, hBox, this.publicFirstNamesField);
      RowConstraints rc = new RowConstraints();
      rc.setVgrow(Priority.SOMETIMES);
      gridPane.getRowConstraints().add(rc);
    }

    {
      this.addRow(gridPane, 5, "dialog.edit_person.profile.nicknames", this.nicknamesField);
      RowConstraints rc = new RowConstraints();
      rc.setVgrow(Priority.SOMETIMES);
      gridPane.getRowConstraints().add(rc);
    }

    {
      this.addRow(gridPane, 6, "dialog.edit_person.profile.gender", this.genderCombo);
      RowConstraints rc = new RowConstraints();
      rc.setVgrow(Priority.SOMETIMES);
      gridPane.getRowConstraints().add(rc);
    }

    {
      this.addRow(gridPane, 7, "dialog.edit_person.profile.main_occupation", this.mainOccupationField);
      RowConstraints rc = new RowConstraints();
      rc.setVgrow(Priority.SOMETIMES);
      gridPane.getRowConstraints().add(rc);
    }

    {
      Label label = new Label(language.translate("dialog.edit_person.profile.disambiguation_id"));
      Label helpLabel = new Label(null, theme.getIcon(Icon.HELP, Icon.Size.SMALL));
      helpLabel.setTooltip(new Tooltip(language.translate("dialog.edit_person.profile.disambiguation_id.tooltip")));
      HBox hBox = new HBox(5, label, helpLabel);
      // Only allow digits and empty text
      this.disambiguationIDField.setTextFormatter(new TextFormatter<>(
          new IntegerStringConverter(),
          null,
          change -> change.getControlNewText().matches("^\\d*$") ? change : null
      ));
      this.disambiguationIDField.textProperty().addListener((observable, oldValue, newValue) -> this.updateButtons());
      gridPane.addRow(8, hBox, this.disambiguationIDField);
      RowConstraints rc = new RowConstraints();
      rc.setVgrow(Priority.SOMETIMES);
      gridPane.getRowConstraints().add(rc);
    }

    {
      Label notesLabel = new Label(language.translate("dialog.edit_person.profile.notes"));
      GridPane.setValignment(notesLabel, VPos.TOP);
      gridPane.addRow(9, notesLabel, this.notesField);
      RowConstraints rc = new RowConstraints();
      rc.setVgrow(Priority.ALWAYS);
      gridPane.getRowConstraints().add(rc);
    }

    {
      Label sourcesLabel = new Label(language.translate("dialog.edit_person.profile.sources"));
      GridPane.setValignment(sourcesLabel, VPos.TOP);
      gridPane.addRow(10, sourcesLabel, this.sourcesField);
      RowConstraints rc = new RowConstraints();
      rc.setVgrow(Priority.ALWAYS);
      gridPane.getRowConstraints().add(rc);
    }

    ColumnConstraints cc1 = new ColumnConstraints();
    cc1.setHgrow(Priority.SOMETIMES);
    ColumnConstraints cc2 = new ColumnConstraints();
    cc2.setHgrow(Priority.ALWAYS);
    gridPane.getColumnConstraints().addAll(cc1, cc2);

    tab.setContent(gridPane);
    return tab;
  }

  private Tab createEventsTab() {
    Language language = App.config().language();
    Theme theme = App.config().theme();

    Tab tab = new Tab(language.translate("dialog.edit_person.events.title"));

    Pane spacer = new Pane();
    HBox.setHgrow(spacer, Priority.ALWAYS);
    this.addEventButton.setText(language.translate("dialog.edit_person.add_event"));
    this.addEventButton.setGraphic(theme.getIcon(Icon.ADD_EVENT, Icon.Size.SMALL));
    this.addEventButton.setOnAction(event -> {
      DateTimeWithPrecision date = new DateTimeWithPrecision(
          new CalendarDateTime(LocalDateTime.now(), Calendar.GREGORIAN),
          DateTimePrecision.EXACT
      );
      LifeEventType birth = this.familyTree.lifeEventTypeRegistry()
          .getEntry(new RegistryEntryKey(Registry.BUILTIN_NS, "birth"));
      this.addEvent(new LifeEvent(date, birth), true);
    });
    HBox buttonsBox = new HBox(spacer, this.addEventButton);

    this.lifeEventsList.setSelectionModel(new NoSelectionModel<>());
    VBox.setVgrow(this.lifeEventsList, Priority.ALWAYS);

    tab.setContent(new VBox(10, buttonsBox, this.lifeEventsList));
    return tab;
  }

  private Tab createParentsTab() {
    Language language = App.config().language();

    Tab tab = new Tab(language.translate("dialog.edit_person.parents.title"));

    GridPane gridPane = new GridPane();
    gridPane.setHgap(4);
    gridPane.setVgap(4);

    {
      this.parent1Combo.getSelectionModel().selectedItemProperty()
          .addListener((observable, oldValue, newValue) -> this.updateButtons());
      this.parent2Combo.getSelectionModel().selectedItemProperty()
          .addListener((observable, oldValue, newValue) -> this.updateButtons());
      HBox parentsBox = new HBox(4, this.parent1Combo, this.parent2Combo);
      this.addRow(gridPane, 0, "dialog.edit_person.parents.parents", parentsBox);
      RowConstraints rc = new RowConstraints();
      rc.setValignment(VPos.TOP);
      rc.setVgrow(Priority.SOMETIMES);
      gridPane.getRowConstraints().add(rc);
    }

    int i = 1;
    for (Person.RelativeType type : Person.RelativeType.values()) {
      RelativesListView component = new RelativesListView();
      this.relativesLists.put(type, component);
      Label label = new Label(language.translate(
          "dialog.edit_person.parents.%s_parents".formatted(type.name().toLowerCase())));
      gridPane.addRow(i++, label, component);
      RowConstraints rc = new RowConstraints();
      rc.setValignment(VPos.TOP);
      rc.setVgrow(Priority.ALWAYS);
      gridPane.getRowConstraints().add(rc);
    }

    ColumnConstraints cc1 = new ColumnConstraints();
    cc1.setHgrow(Priority.SOMETIMES);
    ColumnConstraints cc2 = new ColumnConstraints();
    cc2.setHgrow(Priority.ALWAYS);
    gridPane.getColumnConstraints().addAll(cc1, cc2);

    tab.setContent(gridPane);
    return tab;
  }

  private void addRow(@NotNull GridPane gridPane, int index, @NotNull String text, @NotNull Node node) {
    gridPane.addRow(index, new Label(App.config().language().translate(text)), node);
  }

  /**
   * Update the gender entries in the genders combobox.
   */
  public void updateGendersList() {
    Language language = App.config().language();
    Collator collator = Collator.getInstance(language.locale());
    this.genderCombo.getItems().clear();
    this.genderCombo.getItems().add(new ComboBoxItem<>(null, language.translate("genders.unknown")));
    this.genderCombo.getItems().addAll(this.familyTree.genderRegistry().entries().stream()
        .map(gender -> {
          RegistryEntryKey key = gender.key();
          String text = gender.isBuiltin()
              ? language.translate("genders." + key.name())
              : Objects.requireNonNull(gender.userDefinedName());
          return new ComboBoxItem<>(gender, text);
        })
        .sorted((i1, i2) -> collator.compare(i1.text(), i2.text())) // Perform locale-dependent comparison
        .toList());
  }

  /**
   * Set the person to edit.
   *
   * @param person     The person to edit. A null value indicates to create a new person.
   * @param childInfo  If not null, indicates the persons the one being created should be a parent of.
   * @param familyTree The family tree the person belongs or should belong to.
   */
  public void setPerson(Person person, final @NotNull List<ChildInfo> childInfo, @NotNull FamilyTree familyTree) {
    this.childInfo = new ArrayList<>(childInfo);
    this.familyTree = Objects.requireNonNull(familyTree);
    Language language = App.config().language();

    this.creating = person == null;
    if (!this.creating) {
      this.person = person;
      this.setTitle(language.translate("dialog.edit_person.title",
          new FormatArg("person_name", this.person.toString())));
    } else {
      this.person = new Person();
      this.setTitle(language.translate("dialog.edit_person.title.create"));
    }

    this.updateGendersList();

    // Profile
    this.lifeStatusCombo.getSelectionModel().select(new NotNullComboBoxItem<>(this.person.lifeStatus()));
    this.genderCombo.getSelectionModel().select(new ComboBoxItem<>(this.person.gender().orElse(null)));
    this.legalLastNameField.setText(this.person.legalLastName().orElse(""));
    this.publicLastNameField.setText(this.person.publicLastName().orElse(""));
    this.legalFirstNamesField.setText(this.person.getJoinedLegalFirstNames().orElse(""));
    this.publicFirstNamesField.setText(this.person.getJoinedPublicFirstNames().orElse(""));
    this.nicknamesField.setText(this.person.getJoinedNicknames().orElse(""));
    this.mainOccupationField.setText(this.person.mainOccupation().orElse(""));
    this.disambiguationIDField.setText(this.person.disambiguationID().map(String::valueOf).orElse(""));
    this.notesField.setText(this.person.notes().orElse(""));
    this.sourcesField.setText(this.person.sources().orElse(""));

    // Life events
    this.lifeEventsList.getItems().clear();
    this.person.getLifeEventsAsActor().stream().sorted().forEach(lifeEvent -> {
      this.addEvent(lifeEvent, false);
      if (lifeEvent.type().indicatesDeath()) {
        this.lifeStatusCombo.getSelectionModel().select(new NotNullComboBoxItem<>(LifeStatus.DECEASED));
      }
    });
    if (!this.lifeEventsList.getItems().isEmpty()) {
      this.lifeEventsList.getItems().get(0).setExpanded(true);
    }

    // Relatives
    List<Person> potentialRelatives = this.familyTree.persons().stream()
        .filter(p -> p != this.person)
        .sorted(Person.lastThenFirstNamesComparator())
        .toList();
    List<ComboBoxItem<Person>> relatives = potentialRelatives.stream()
        .map(p -> new ComboBoxItem<>(p, p.toString()))
        .toList();
    this.parent1Combo.getItems().clear();
    this.parent1Combo.getItems().add(new ComboBoxItem<>(null,
        language.translate("dialog.edit_person.parents.parents.unknown")));
    this.parent1Combo.getItems().addAll(relatives);
    this.parent2Combo.getItems().clear();
    this.parent2Combo.getItems().add(new ComboBoxItem<>(null,
        language.translate("dialog.edit_person.parents.parents.unknown")));
    this.parent2Combo.getItems().addAll(relatives);
    var parents = this.person.parents();
    Optional<Person> leftParent = parents.left();
    if (leftParent.isPresent()) {
      this.parent1Combo.getSelectionModel().select(new ComboBoxItem<>(leftParent.get()));
    } else {
      this.parent1Combo.getSelectionModel().select(0);
    }
    Optional<Person> rightParent = parents.right();
    if (rightParent.isPresent()) {
      this.parent2Combo.getSelectionModel().select(new ComboBoxItem<>(rightParent.get()));
    } else {
      this.parent2Combo.getSelectionModel().select(0);
    }
    for (Person.RelativeType type : Person.RelativeType.values()) {
      RelativesListView list = this.relativesLists.get(type);
      list.setPersons(this.person.getRelatives(type));
      list.setPotentialRelatives(potentialRelatives);
    }

    this.updateButtons();
  }

  /**
   * Set the value of the parents fields.
   *
   * @param parent1 Person to set as parent 1.
   * @param parent2 Person to set as parent 2.
   */
  public void setParents(Person parent1, Person parent2) {
    if (parent1 != null) {
      this.parent1Combo.getSelectionModel().select(new ComboBoxItem<>(parent1));
    } else {
      this.parent1Combo.getSelectionModel().select(0);
    }
    if (parent2 != null) {
      this.parent2Combo.getSelectionModel().select(new ComboBoxItem<>(parent2));
    } else {
      this.parent2Combo.getSelectionModel().select(0);
    }
  }

  /**
   * Select the tab at the given index.
   *
   * @param index Tab’s index.
   */
  public void selectTab(int index) {
    this.tabPane.getSelectionModel().select(index);
  }

  /**
   * Add a {@link LifeEventView} form for the given {@link LifeEvent} object.
   *
   * @param lifeEvent The life event to create a form for.
   * @param expanded  Whether to expand the created form.
   */
  private void addEvent(@NotNull LifeEvent lifeEvent, boolean expanded) {
    LifeEventView lifeEventView = new LifeEventView(
        this.familyTree, lifeEvent, this.person, this.familyTree.persons(), expanded, this.lifeEventsList);
    lifeEventView.getDeletionListeners().add(this::onEventDelete);
    lifeEventView.getUpdateListeners().add(this::updateButtons);
    lifeEventView.getTypeListeners().add(t -> {
      boolean anyDeath = this.lifeEventsList.getItems().stream()
          .anyMatch(i -> i.selectedLifeEventType().indicatesDeath());
      this.internalLifeStatusChange = true;
      if (anyDeath) {
        this.lifeStatusCombo.getSelectionModel().select(new NotNullComboBoxItem<>(LifeStatus.DECEASED));
      } else {
        this.lifeStatusCombo.getSelectionModel().select(new NotNullComboBoxItem<>(this.lifeStatusCache));
      }
      this.internalLifeStatusChange = false;
      this.lifeStatusCombo.setDisable(anyDeath);
    });
    this.lifeEventsList.getItems().add(lifeEventView);
    this.updateButtons();
  }

  /**
   * Called when a {@link LifeEventView} has been set for deletion.
   * Adds it to the {@link #eventsToDelete} set.
   *
   * @param lifeEventView Component to delete.
   */
  private void onEventDelete(@NotNull LifeEventView lifeEventView) {
    String prefix = "alert.delete_life_event.";
    boolean delete = Alerts.confirmation(prefix + "header", prefix + "content", prefix + "title");
    if (delete) {
      this.eventsToDelete.add(lifeEventView);
      this.lifeEventsList.getItems().remove(lifeEventView);
      this.updateButtons();
    }
  }

  /**
   * Updates this dialog’s buttons.
   */
  private void updateButtons() {
    Integer disambiguationID = this.getDisambiguationID();
    boolean invalid = disambiguationID != null && disambiguationID == 0;
    this.disambiguationIDField.pseudoClassStateChanged(PseudoClasses.INVALID, invalid);

    Map<LifeEventType, List<LifeEventView>> uniqueTypes = new HashMap<>();
    boolean anyDeath = false;
    for (LifeEventView item : this.lifeEventsList.getItems()) {
      item.pseudoClassStateChanged(PseudoClasses.INVALID, false);
      if (item.lifeEvent().type().indicatesDeath()) {
        anyDeath = true;
      }
      if (!item.checkValidity()) {
        invalid = true;
      }
      LifeEventType type = item.selectedLifeEventType();
      if (type.isUnique()) {
        if (!uniqueTypes.containsKey(type)) {
          uniqueTypes.put(type, new LinkedList<>());
        } else {
          item.pseudoClassStateChanged(PseudoClasses.INVALID, true);
          invalid = true;
        }
        uniqueTypes.get(type).add(item);
      }
    }
    this.lifeStatusCombo.setDisable(anyDeath);

    ComboBoxItem<Person> selectedParent1 = this.parent1Combo.getSelectionModel().getSelectedItem();
    ComboBoxItem<Person> selectedParent2 = this.parent2Combo.getSelectionModel().getSelectedItem();
    if (selectedParent1 != null && selectedParent2 != null) {
      Person parent1 = selectedParent1.data();
      Person parent2 = selectedParent2.data();
      boolean sameParents = parent1 != null && parent1 == parent2;
      this.parent1Combo.pseudoClassStateChanged(PseudoClasses.INVALID, sameParents);
      this.parent2Combo.pseudoClassStateChanged(PseudoClasses.INVALID, sameParents);
      invalid |= sameParents;
    }

    this.getDialogPane().lookupButton(ButtonTypes.OK).setDisable(invalid);
  }

  /**
   * Update the given person object with data from this dialog’s fields.
   *
   * @param person The person to update.
   */
  private void updatePerson(@NotNull Person person) {
    // Profile
    person.setGender(this.genderCombo.getSelectionModel().getSelectedItem().data());
    person.setLegalLastName(this.getText(this.legalLastNameField));
    person.setPublicLastName(this.getText(this.publicLastNameField));
    person.setLegalFirstNames(this.splitText(this.legalFirstNamesField));
    person.setPublicFirstNames(this.splitText(this.publicFirstNamesField));
    person.setNicknames(this.splitText(this.nicknamesField));
    person.setMainOccupation(this.getText(this.mainOccupationField));
    person.setDisambiguationID(this.getDisambiguationID());
    person.setNotes(this.getText(this.notesField));
    person.setSources(this.getText(this.sourcesField));

    // Life events
    this.lifeEventsList.getItems().forEach(LifeEventView::applyChanges);
    for (LifeEventView lifeEventView : this.eventsToDelete) {
      LifeEvent event = lifeEventView.lifeEvent();
      this.familyTree.removeActorFromLifeEvent(event, person);
      this.familyTree.removeWitnessFromLifeEvent(event, person);
    }
    // Update life status after events to avoid assertion error
    person.setLifeStatus(this.lifeStatusCombo.getSelectionModel().getSelectedItem().data());

    // Relatives
    person.setParent(0, this.parent1Combo.getSelectionModel().getSelectedItem().data());
    person.setParent(1, this.parent2Combo.getSelectionModel().getSelectedItem().data());
    for (Person.RelativeType type : Person.RelativeType.values()) {
      // Clear relatives of the current type
      for (Person relative : this.person.getRelatives(type)) {
        this.person.removeRelative(relative, type);
      }
      // Add back the selected relatives
      this.relativesLists.get(type).getPersons().forEach(p -> this.person.addRelative(p, type));
    }
  }

  /**
   * Get the disambiguation ID from the corresponding text field.
   */
  private @Nullable Integer getDisambiguationID() {
    String text = this.disambiguationIDField.getText();
    return text.isEmpty() ? null : Integer.parseInt(text);
  }

  /**
   * Strip the text of the given text input.
   *
   * @param textInput Text input to get the text from.
   * @return The stripped text.
   */
  private @Nullable String getText(final @NotNull TextInputControl textInput) {
    return StringUtils.stripNullable(textInput.getText()).orElse(null);
  }

  /**
   * Split the text of the given text input according to whitespace.
   *
   * @param textInput Text input to get the text from.
   * @return A list of the split text’s tokens.
   */
  private List<String> splitText(final @NotNull TextInputControl textInput) {
    return Arrays.stream(textInput.getText().split("\\s+"))
        .map(String::strip)
        .filter(s -> !s.isEmpty())
        .toList();
  }
}
