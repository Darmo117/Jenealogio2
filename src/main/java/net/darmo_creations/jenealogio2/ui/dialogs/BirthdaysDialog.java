package net.darmo_creations.jenealogio2.ui.dialogs;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import net.darmo_creations.jenealogio2.App;
import net.darmo_creations.jenealogio2.config.Config;
import net.darmo_creations.jenealogio2.config.Language;
import net.darmo_creations.jenealogio2.model.FamilyTree;
import net.darmo_creations.jenealogio2.model.Person;
import net.darmo_creations.jenealogio2.model.datetime.DateTime;
import net.darmo_creations.jenealogio2.model.datetime.DateTimePrecision;
import net.darmo_creations.jenealogio2.model.datetime.DateTimeWithPrecision;
import net.darmo_creations.jenealogio2.themes.Icon;
import net.darmo_creations.jenealogio2.ui.components.NoSelectionModel;
import net.darmo_creations.jenealogio2.ui.events.NewParentClickListener;
import net.darmo_creations.jenealogio2.ui.events.PersonClickListener;
import net.darmo_creations.jenealogio2.ui.events.PersonClickObservable;
import net.darmo_creations.jenealogio2.ui.events.PersonClickedEvent;
import net.darmo_creations.jenealogio2.utils.FormatArg;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Dialog that displays all birthdays from a family tree.
 * <p>
 * User may choose to show/hide birthdays of deceased persons.
 */
public class BirthdaysDialog extends DialogBase<ButtonType> implements PersonClickObservable {
  private final CheckBox showDeceasedCheckBox = new CheckBox();
  private final TabPane tabPane = new TabPane();
  private final ListView<PersonItem> todayList = new ListView<>();
  private final ListView<PersonItem> tomorrowList = new ListView<>();
  private final ListView<PersonItem> afterTomorrowList = new ListView<>();

  private final List<PersonClickListener> personClickListeners = new LinkedList<>();
  private FamilyTree familyTree;

  /**
   * Create a dialog that shows birthdays of a family tree.
   */
  public BirthdaysDialog() {
    super("birthdays", true, false, ButtonTypes.CLOSE);

    Config config = App.config();
    Language language = config.language();

    this.showDeceasedCheckBox.setText(language.translate("dialog.birthdays.show_deceased_birthdays"));
    this.showDeceasedCheckBox.setSelected(config.shouldShowDeceasedPersonsBirthdays());
    this.showDeceasedCheckBox.selectedProperty()
        .addListener((observable, oldValue, newValue) -> this.onCheckBoxSelection(newValue));
    HBox.setMargin(this.showDeceasedCheckBox, new Insets(4));

    HBox hBox = new HBox(this.showDeceasedCheckBox);

    // First tab’s content
    VBox.setVgrow(this.todayList, Priority.ALWAYS);
    VBox.setVgrow(this.tomorrowList, Priority.ALWAYS);
    VBox.setVgrow(this.afterTomorrowList, Priority.ALWAYS);
    VBox vBox = new VBox(4,
        new Label(language.translate("dialog.birthdays.tab.upcoming.today")),
        this.todayList,
        new Label(language.translate("dialog.birthdays.tab.upcoming.tomorrow")),
        this.tomorrowList,
        new Label(language.translate("dialog.birthdays.tab.upcoming.after_tomorrow")),
        this.afterTomorrowList
    );
    vBox.setStyle("-fx-padding: 10px 0 0 0");

    this.tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
    Tab firstTab = new Tab(language.translate("dialog.birthdays.tab.upcoming"));
    firstTab.setContent(vBox);
    this.tabPane.getTabs().add(firstTab);

    // Per-month tabs
    for (int i = 1; i <= 12; i++) {
      this.tabPane.getTabs().add(new BirthdayTab(i));
    }

    VBox content = new VBox(hBox, this.tabPane);
    content.setPrefWidth(1100);
    content.setPrefHeight(700);
    this.getDialogPane().setContent(content);

    Stage stage = this.stage();
    stage.setMinWidth(300);
    stage.setMinHeight(300);
  }

  /**
   * Called when {@link #showDeceasedCheckBox}’s selection changes.
   *
   * @param selected Whether it is selected or not.
   */
  private void onCheckBoxSelection(boolean selected) {
    Config config = App.config();
    config.setShouldShowDeceasedPersonsBirthdays(selected);
    try {
      config.save();
    } catch (IOException e) {
      App.LOGGER.exception(e);
    }
    if (this.familyTree != null) {
      this.refresh(this.familyTree);
    }
  }

  /**
   * Refresh displayed information from the given tree.
   *
   * @param familyTree Tree to get information from.
   */
  public void refresh(final @NotNull FamilyTree familyTree) {
    this.familyTree = Objects.requireNonNull(familyTree);
    Map<Integer, Map<Integer, Set<BirthdayEntry>>> perMonth = new HashMap<>();

    boolean hideDeceased = !App.config().shouldShowDeceasedPersonsBirthdays();
    for (Person person : familyTree.persons()) {
      if (hideDeceased && person.lifeStatus().isConsideredDeceased()) {
        continue;
      }
      Optional<DateTime> birthDate = person.getBirthDate();
      if (birthDate.isPresent()) {
        DateTime date = birthDate.get();
        if (date instanceof DateTimeWithPrecision d) {
          DateTimePrecision precision = d.precision();
          if (precision == DateTimePrecision.BEFORE || precision == DateTimePrecision.AFTER) {
            continue;
          }
          this.addDate(perMonth, d.date().iso8601Date(), precision != DateTimePrecision.EXACT, person);
        }
      }
    }

    for (int i = 1; i <= 12; i++) {
      var monthEntry = perMonth.getOrDefault(i, Map.of());
      ((BirthdayTab) this.tabPane.getTabs().get(i)).setEntries(monthEntry);
    }

    LocalDate today = LocalDate.now();
    this.updateList(this.todayList, perMonth, today);
    LocalDate tomorrow = today.plusDays(1);
    this.updateList(this.tomorrowList, perMonth, tomorrow);
    LocalDate afterTomorrow = tomorrow.plusDays(1);
    this.updateList(this.afterTomorrowList, perMonth, afterTomorrow);
  }

  private void updateList(
      @NotNull ListView<PersonItem> list,
      final Map<Integer, @NotNull Map<Integer, Set<BirthdayEntry>>> birthdays,
      @NotNull LocalDate date
  ) {
    list.getItems().clear();
    birthdays.getOrDefault(date.getMonthValue(), Map.of()).getOrDefault(date.getDayOfMonth(), Set.of()).stream()
        .sorted((e1, e2) -> Person.lastThenFirstNamesComparator().compare(e1.person(), e2.person()))
        .forEach(e -> list.getItems().add(new PersonItem(e.person(), date.getYear())));
  }

  /**
   * Add a {@link BirthdayEntry} to the given map.
   *
   * @param perMonth  Map to add an entry to.
   * @param date      Entry’s date.
   * @param uncertain Whether the date is uncertain.
   * @param person    The person born at that date.
   */
  private void addDate(
      @NotNull Map<Integer, Map<Integer, Set<BirthdayEntry>>> perMonth,
      @NotNull LocalDateTime date,
      boolean uncertain,
      final @NotNull Person person
  ) {
    int month = date.getMonthValue();
    if (!perMonth.containsKey(month)) {
      perMonth.put(month, new HashMap<>());
    }
    int day = date.getDayOfMonth();
    var monthEntry = perMonth.get(month);
    if (!monthEntry.containsKey(day)) {
      monthEntry.put(day, new HashSet<>());
    }
    monthEntry.get(day).add(new BirthdayEntry(person, date, uncertain));
  }

  @Override
  public List<PersonClickListener> personClickListeners() {
    return this.personClickListeners;
  }

  @Override
  public List<NewParentClickListener> newParentClickListeners() {
    throw new UnsupportedOperationException();
  }

  private void firePersonClickEvent(@NotNull Person person) {
    this.firePersonClickEvent(new PersonClickedEvent(person, PersonClickedEvent.Action.SET_AS_TARGET));
  }

  private class PersonItem extends HBox {
    public PersonItem(@NotNull Person person, int year) {
      super(4);
      this.setAlignment(Pos.CENTER_LEFT);
      Button button = new Button(person.toString(), App.config().theme().getIcon(Icon.GO_TO, Icon.Size.SMALL));
      button.setOnAction(event -> BirthdaysDialog.this.firePersonClickEvent(person));
      this.getChildren().addAll(button, new Label(String.valueOf(year)));
    }
  }

  /**
   * Container class that holds data about a person’s birth date.
   *
   * @param person    Person whose birth date is represented by this object.
   * @param date      Person’s birth date.
   * @param uncertain Whether the date is uncertain.
   */
  private record BirthdayEntry(@NotNull Person person, @NotNull LocalDateTime date, boolean uncertain) {
    private BirthdayEntry {
      Objects.requireNonNull(person);
      Objects.requireNonNull(date);
    }
  }

  /**
   * Tab sub-class that displays birthdays for the month it represents.
   */
  private class BirthdayTab extends Tab {
    private final String baseTitle;

    private final ListView<DayItem> entriesList = new ListView<>();

    /**
     * Create a tab for the given month.
     *
     * @param month Month’s value (1 for January, etc.).
     */
    public BirthdayTab(int month) {
      if (month < 1 || month > 12) {
        throw new IllegalArgumentException("invalid month value: " + month);
      }
      this.baseTitle = App.config().language().translate("calendar.gregorian.month." + month);
      this.setText(this.baseTitle);

      this.entriesList.setSelectionModel(new NoSelectionModel<>());
      this.setContent(this.entriesList);
    }

    /**
     * Set entries for this tab.
     *
     * @param entries Birthday entries.
     */
    public void setEntries(final @NotNull Map<Integer, Set<BirthdayEntry>> entries) {
      this.entriesList.getItems().clear();

      var sortedEntries = entries.entrySet().stream()
          .sorted(Map.Entry.comparingByKey())
          .toList();
      int nb = 0;
      for (var entry : sortedEntries) {
        Set<BirthdayEntry> entrySet = entry.getValue();
        this.entriesList.getItems().add(new DayItem(entry.getKey(), entrySet));
        nb += entrySet.size();
      }

      if (nb == 0) {
        this.setText(this.baseTitle);
        return;
      }

      String title = App.config().language().translate(
          "dialog.birthdays.tab.title_amount_format",
          new FormatArg("title", this.baseTitle),
          new FormatArg("number", nb)
      );
      this.setText(title);
    }

    private class DayItem extends VBox {
      public DayItem(int day, final @NotNull Set<BirthdayEntry> entries) {
        super(4);
        Label dayLabel = new Label(String.valueOf(day));
        dayLabel.getStyleClass().add("birth-day");
        this.getChildren().add(dayLabel);
        entries.stream()
            .sorted((e1, e2) -> Person.lastThenFirstNamesComparator().compare(e1.person(), e2.person()))
            .forEach(e -> {
              Person person = e.person();
              Button personButton = new Button(person.toString(), App.config().theme().getIcon(Icon.GO_TO, Icon.Size.SMALL));
              personButton.setOnAction(event -> BirthdaysDialog.this.firePersonClickEvent(person));
              Label yearLabel = new Label(String.valueOf(e.date().getYear()));
              HBox row = new HBox(4, personButton, yearLabel);
              row.setAlignment(Pos.CENTER_LEFT);
              if (e.uncertain()) {
                Label uncertainIcon = new Label(null, App.config().theme().getIcon(Icon.UNCERTAIN, Icon.Size.SMALL));
                uncertainIcon.setTooltip(new Tooltip(App.config().language().translate("dialog.birthdays.uncertain")));
                row.getChildren().add(uncertainIcon);
              }
              this.getChildren().add(row);
            });
      }
    }
  }
}
