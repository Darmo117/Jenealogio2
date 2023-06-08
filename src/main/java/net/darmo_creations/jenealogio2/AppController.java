package net.darmo_creations.jenealogio2;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import net.darmo_creations.jenealogio2.io.TreeFileReader;
import net.darmo_creations.jenealogio2.io.TreeFileWriter;
import net.darmo_creations.jenealogio2.model.FamilyTree;
import net.darmo_creations.jenealogio2.model.LifeEvent;
import net.darmo_creations.jenealogio2.model.Person;
import net.darmo_creations.jenealogio2.themes.Icon;
import net.darmo_creations.jenealogio2.themes.Theme;
import net.darmo_creations.jenealogio2.ui.ChildInfo;
import net.darmo_creations.jenealogio2.ui.FamilyTreeComponent;
import net.darmo_creations.jenealogio2.ui.FamilyTreePane;
import net.darmo_creations.jenealogio2.ui.FamilyTreeView;
import net.darmo_creations.jenealogio2.ui.dialogs.*;
import net.darmo_creations.jenealogio2.utils.FormatArg;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;

// TODO add right panel to display summary of selected person’s data
public class AppController {
  public static final MouseButton TARGET_UPDATE_BUTTON = MouseButton.SECONDARY;

  private Stage stage;

  @FXML
  private MenuItem newFileMenuItem;
  @FXML
  private MenuItem openFileMenuItem;
  @FXML
  private MenuItem saveMenuItem;
  @FXML
  private MenuItem saveAsMenuItem;
  @FXML
  private MenuItem settingsMenuItem;
  @FXML
  private MenuItem quitMenuItem;
  @FXML
  private MenuItem undoMenuItem;
  @FXML
  private MenuItem redoMenuItem;
  @FXML
  private MenuItem renameTreeMenuItem;
  @FXML
  private MenuItem setAsRootMenuItem;
  @FXML
  private MenuItem addPersonMenuItem;
  @FXML
  private MenuItem editPersonMenuItem;
  @FXML
  private MenuItem removePersonMenuItem;
  @FXML
  private MenuItem addChildMenuItem;
  @FXML
  private MenuItem addSiblingMenuItem;
  @FXML
  private MenuItem editParentsMenuItem;
  @FXML
  private MenuItem editLifeEventsMenuItem;
  @FXML
  private MenuItem setPictureMenuItem;
  @FXML
  private MenuItem calculateRelationshipsMenuItem;
  @FXML
  private MenuItem birthdaysMenuItem;
  @FXML
  private MenuItem mapMenuItem;
  @FXML
  private MenuItem checkInconsistenciesMenuItem;
  @FXML
  private MenuItem aboutMenuItem;

  @FXML
  private Button newToolbarButton;
  @FXML
  private Button openToolbarButton;
  @FXML
  private Button saveToolbarButton;
  @FXML
  private Button saveAsToolbarButton;
  @FXML
  private Button undoToolbarButton;
  @FXML
  private Button redoToolbarButton;
  @FXML
  private Button setAsRootToolbarButton;
  @FXML
  private Button addPersonToolbarButton;
  @FXML
  private Button addChildToolbarButton;
  @FXML
  private Button addSiblingToolbarButton;
  @FXML
  private Button editParentsToolbarButton;
  @FXML
  private Button editLifeEventsToolbarButton;
  @FXML
  private Button setPictureToolbarButton;
  @FXML
  private Button calculateRelationshipsToolbarButton;
  @FXML
  private Button birthdaysToolbarButton;
  @FXML
  private Button mapToolbarButton;
  @FXML
  private Button checkInconsistenciesToolbarButton;

  @FXML
  private AnchorPane sideTreeView;
  @FXML
  private AnchorPane mainPane;

  private final TreeFileReader treeFileReader = new TreeFileReader();
  private final TreeFileWriter treeFileWriter = new TreeFileWriter();

  private final FamilyTreeView familyTreeView = new FamilyTreeView();
  private final FamilyTreePane familyTreePane = new FamilyTreePane();
  private FamilyTreeComponent focusedComponent;

  private final EditPersonDialog editPersonDialog = new EditPersonDialog();
  private final SettingsDialog settingsDialog = new SettingsDialog();
  private final AboutDialog aboutDialog = new AboutDialog();

  private FamilyTree familyTree;
  private File loadedFile;
  private boolean unsavedChanges;
  private boolean defaultEmptyTree;

  public void initialize() {
    Theme theme = App.config().theme();

    // Menu items
    this.newFileMenuItem.setGraphic(theme.getIcon(Icon.NEW_FILE, Icon.Size.SMALL));
    this.newFileMenuItem.setOnAction(event -> this.onNewFileAction());
    this.openFileMenuItem.setGraphic(theme.getIcon(Icon.OPEN_FILE, Icon.Size.SMALL));
    this.openFileMenuItem.setOnAction(event -> this.onOpenFileAction());
    this.saveMenuItem.setGraphic(theme.getIcon(Icon.SAVE, Icon.Size.SMALL));
    this.saveMenuItem.setOnAction(event -> this.onSaveAction());
    this.saveAsMenuItem.setGraphic(theme.getIcon(Icon.SAVE_AS, Icon.Size.SMALL));
    this.saveAsMenuItem.setOnAction(event -> this.onSaveAsAction());
    this.settingsMenuItem.setGraphic(theme.getIcon(Icon.SETTINGS, Icon.Size.SMALL));
    this.settingsMenuItem.setOnAction(event -> this.onSettingsAction());
    this.quitMenuItem.setGraphic(theme.getIcon(Icon.QUIT, Icon.Size.SMALL));
    this.quitMenuItem.setOnAction(event -> this.onQuitAction());

    this.undoMenuItem.setGraphic(theme.getIcon(Icon.UNDO, Icon.Size.SMALL));
    this.redoMenuItem.setGraphic(theme.getIcon(Icon.REDO, Icon.Size.SMALL));
    this.renameTreeMenuItem.setGraphic(theme.getIcon(Icon.RENAME_TREE, Icon.Size.SMALL));
    this.renameTreeMenuItem.setOnAction(event -> this.onRenameTreeAction());
    this.setAsRootMenuItem.setGraphic(theme.getIcon(Icon.SET_AS_ROOT, Icon.Size.SMALL));
    this.setAsRootMenuItem.setOnAction(event -> this.onSetAsRootAction());
    this.addPersonMenuItem.setGraphic(theme.getIcon(Icon.ADD_PERSON, Icon.Size.SMALL));
    this.addPersonMenuItem.setOnAction(event -> this.onAddPersonAction());
    this.editPersonMenuItem.setGraphic(theme.getIcon(Icon.EDIT_PERSON, Icon.Size.SMALL));
    this.editPersonMenuItem.setOnAction(event -> this.onEditPersonAction());
    this.removePersonMenuItem.setGraphic(theme.getIcon(Icon.REMOVE_PERSON, Icon.Size.SMALL));
    this.removePersonMenuItem.setOnAction(event -> this.onRemovePersonAction());
    this.addChildMenuItem.setGraphic(theme.getIcon(Icon.ADD_CHILD, Icon.Size.SMALL));
    this.addSiblingMenuItem.setGraphic(theme.getIcon(Icon.ADD_SIBLING, Icon.Size.SMALL));
    this.editParentsMenuItem.setGraphic(theme.getIcon(Icon.EDIT_PARENTS, Icon.Size.SMALL));
    this.editParentsMenuItem.setOnAction(event -> this.onEditParentsAction());
    this.editLifeEventsMenuItem.setGraphic(theme.getIcon(Icon.EDIT_LIFE_EVENTS, Icon.Size.SMALL));
    this.editLifeEventsMenuItem.setOnAction(event -> this.onEditLifeEventsAction());
    this.setPictureMenuItem.setGraphic(theme.getIcon(Icon.SET_PICTURE, Icon.Size.SMALL));

    this.calculateRelationshipsMenuItem.setGraphic(theme.getIcon(Icon.CALCULATE_RELATIONSHIPS, Icon.Size.SMALL));
    this.birthdaysMenuItem.setGraphic(theme.getIcon(Icon.BIRTHDAYS, Icon.Size.SMALL));
    this.mapMenuItem.setGraphic(theme.getIcon(Icon.MAP, Icon.Size.SMALL));
    this.checkInconsistenciesMenuItem.setGraphic(theme.getIcon(Icon.CHECK_INCONSISTENCIES, Icon.Size.SMALL));

    this.aboutMenuItem.setGraphic(theme.getIcon(Icon.ABOUT, Icon.Size.SMALL));
    this.aboutMenuItem.setOnAction(event -> this.onAboutAction());

    // Toolbar buttons
    this.newToolbarButton.setGraphic(theme.getIcon(Icon.NEW_FILE, Icon.Size.BIG));
    this.newToolbarButton.setOnAction(event -> this.onNewFileAction());
    this.openToolbarButton.setGraphic(theme.getIcon(Icon.OPEN_FILE, Icon.Size.BIG));
    this.openToolbarButton.setOnAction(event -> this.onOpenFileAction());
    this.saveToolbarButton.setGraphic(theme.getIcon(Icon.SAVE, Icon.Size.BIG));
    this.saveToolbarButton.setOnAction(event -> this.onSaveAction());
    this.saveAsToolbarButton.setGraphic(theme.getIcon(Icon.SAVE_AS, Icon.Size.BIG));
    this.saveAsToolbarButton.setOnAction(event -> this.onSaveAsAction());

    this.undoToolbarButton.setGraphic(theme.getIcon(Icon.UNDO, Icon.Size.BIG));
    this.redoToolbarButton.setGraphic(theme.getIcon(Icon.REDO, Icon.Size.BIG));
    this.setAsRootToolbarButton.setGraphic(theme.getIcon(Icon.SET_AS_ROOT, Icon.Size.BIG));
    this.setAsRootToolbarButton.setOnAction(event -> this.onSetAsRootAction());
    this.addPersonToolbarButton.setGraphic(theme.getIcon(Icon.ADD_PERSON, Icon.Size.BIG));
    this.addPersonToolbarButton.setOnAction(event -> this.onAddPersonAction());
    this.addChildToolbarButton.setGraphic(theme.getIcon(Icon.ADD_CHILD, Icon.Size.BIG));
    this.addSiblingToolbarButton.setGraphic(theme.getIcon(Icon.ADD_SIBLING, Icon.Size.BIG));
    this.editParentsToolbarButton.setGraphic(theme.getIcon(Icon.EDIT_PARENTS, Icon.Size.BIG));
    this.editParentsToolbarButton.setOnAction(event -> this.onEditParentsAction());
    this.editLifeEventsToolbarButton.setGraphic(theme.getIcon(Icon.EDIT_LIFE_EVENTS, Icon.Size.BIG));
    this.editLifeEventsToolbarButton.setOnAction(event -> this.onEditLifeEventsAction());
    this.setPictureToolbarButton.setGraphic(theme.getIcon(Icon.SET_PICTURE, Icon.Size.BIG));

    this.calculateRelationshipsToolbarButton.setGraphic(theme.getIcon(Icon.CALCULATE_RELATIONSHIPS, Icon.Size.BIG));
    this.birthdaysToolbarButton.setGraphic(theme.getIcon(Icon.BIRTHDAYS, Icon.Size.BIG));
    this.mapToolbarButton.setGraphic(theme.getIcon(Icon.MAP, Icon.Size.BIG));
    this.checkInconsistenciesToolbarButton.setGraphic(theme.getIcon(Icon.CHECK_INCONSISTENCIES, Icon.Size.BIG));

    AnchorPane.setTopAnchor(this.familyTreeView, 0.0);
    AnchorPane.setBottomAnchor(this.familyTreeView, 0.0);
    AnchorPane.setLeftAnchor(this.familyTreeView, 0.0);
    AnchorPane.setRightAnchor(this.familyTreeView, 0.0);
    this.sideTreeView.getChildren().add(this.familyTreeView);
    this.familyTreeView.personClickListeners()
        .add((person, clickCount, button) -> this.onPersonClick(person, clickCount, button, true));

    AnchorPane.setTopAnchor(this.familyTreePane, 0.0);
    AnchorPane.setBottomAnchor(this.familyTreePane, 0.0);
    AnchorPane.setLeftAnchor(this.familyTreePane, 0.0);
    AnchorPane.setRightAnchor(this.familyTreePane, 0.0);
    this.mainPane.getChildren().add(this.familyTreePane);
    this.familyTreePane.personClickListeners()
        .add((person, clickCount, button) -> this.onPersonClick(person, clickCount, button, false));
    this.familyTreePane.newParentClickListeners().add(this::onNewParentClick);
  }

  public void onShown(@NotNull Stage stage, File file) {
    this.stage = Objects.requireNonNull(stage);
    stage.setOnCloseRequest(event -> {
      event.consume();
      this.onQuitAction();
    });
    if (file != null && this.loadFile(file)) {
      return;
    }
    this.defaultEmptyTree = true;
    String defaultName = App.config().language().translate("app_title.undefined_tree_name");
    this.setFamilyTree(new FamilyTree(defaultName), null);
  }

  private void setFamilyTree(@NotNull FamilyTree tree, File file) {
    this.familyTree = tree;
    this.familyTreeView.setFamilyTree(this.familyTree);
    this.familyTreePane.setFamilyTree(this.familyTree);
    this.familyTreeView.refresh();
    this.familyTreePane.refresh();
    this.loadedFile = file;
    this.unsavedChanges = file == null;
    this.updateUI();
  }

  private void onRenameTreeAction() {
    Optional<String> name = Alerts.textInput(
        "alert.tree_name.header", "alert.tree_name.label", null, null);
    if (name.isEmpty()) {
      return;
    }
    this.familyTree.setName(name.get());
    this.defaultEmptyTree = false;
    this.unsavedChanges = true;
    this.updateUI();
  }

  private void onNewFileAction() {
    if (!this.defaultEmptyTree && this.unsavedChanges) {
      boolean open = Alerts.confirmation(
          "alert.unsaved_changes.header", "alert.unsaved_changes.content", null);
      if (!open) {
        return;
      }
    }
    Optional<String> name = Alerts.textInput(
        "alert.tree_name.header", "alert.tree_name.label", null, null);
    if (name.isEmpty()) {
      return;
    }
    this.defaultEmptyTree = false;
    this.setFamilyTree(new FamilyTree(name.get()), null);
  }

  private void onOpenFileAction() {
    if (!this.defaultEmptyTree && this.unsavedChanges) {
      boolean open = Alerts.confirmation(
          "alert.unsaved_changes.header", "alert.unsaved_changes.content", null);
      if (!open) {
        return;
      }
    }
    Optional<File> f = FileChoosers.showTreeFileChooser(this.stage);
    if (f.isEmpty()) {
      return;
    }
    this.loadFile(f.get());
  }

  private boolean loadFile(@NotNull File file) {
    App.LOGGER.info("Loading tree from %s…".formatted(file));
    FamilyTree familyTree;
    try {
      familyTree = this.treeFileReader.loadFile(file);
    } catch (IOException e) {
      App.LOGGER.exception(e);
      Alerts.error(
          "alert.load_error.header",
          "alert.load_error.content",
          "alert.load_error.title",
          new FormatArg("trace", e.getMessage())
      );
      return false;
    }
    App.LOGGER.info("Done");

    this.defaultEmptyTree = false;
    this.setFamilyTree(familyTree, file);
    return true;
  }

  private void onSaveAction() {
    File file;
    if (this.loadedFile == null) {
      Optional<File> f = FileChoosers.showTreeFileSaver(this.stage);
      if (f.isEmpty()) {
        return;
      }
      file = f.get();
      // File existence is already checked by file chooser
    } else {
      file = this.loadedFile;
    }
    if (!this.saveFile(file)) {
      return;
    }
    if (this.loadedFile == null) {
      this.loadedFile = file;
    }
    this.unsavedChanges = false;
    this.updateUI();
  }

  private void onSaveAsAction() {
    Optional<File> f = FileChoosers.showTreeFileSaver(this.stage);
    if (f.isEmpty()) {
      return;
    }
    File file = f.get();
    if (!this.saveFile(file)) {
      return;
    }
    this.loadedFile = file;
    this.unsavedChanges = false;
    this.updateUI();
  }

  private boolean saveFile(@NotNull File file) {
    App.LOGGER.info("Saving tree to %s…".formatted(file));
    try {
      this.treeFileWriter.saveToFile(this.familyTree, file);
    } catch (IOException e) {
      App.LOGGER.exception(e);
      Alerts.error(
          "alert.save_error.header",
          "alert.save_error.content",
          "alert.save_error.title",
          new FormatArg("trace", e.getMessage())
      );
      return false;
    }
    App.LOGGER.info("Done");
    return true;
  }

  private Optional<Person> getSelectedPerson() {
    if (this.focusedComponent != null) {
      return this.focusedComponent.getSelectedPerson();
    }
    return Optional.empty();
  }

  private void onSetAsRootAction() {
    this.getSelectedPerson().ifPresent(root -> {
      this.familyTree.setRoot(root);
      this.defaultEmptyTree = false;
      this.unsavedChanges = true;
      this.updateUI();
    });
  }

  private void onAddPersonAction() {
    this.openEditPersonDialog(null, null, EditPersonDialog.TAB_PROFILE);
  }

  private void onEditPersonAction() {
    this.getSelectedPerson().ifPresent(
        person -> this.openEditPersonDialog(person, null, EditPersonDialog.TAB_PROFILE));
  }

  private void onEditParentsAction() {
    this.getSelectedPerson().ifPresent(
        person -> this.openEditPersonDialog(person, null, EditPersonDialog.TAB_PARENTS));
  }

  private void onEditLifeEventsAction() {
    this.getSelectedPerson().ifPresent(
        person -> this.openEditPersonDialog(person, null, EditPersonDialog.TAB_EVENTS));
  }

  private void onRemovePersonAction() {
    this.getSelectedPerson().ifPresent(person -> {
      if (this.familyTree.isRoot(person)) {
        Alerts.warning(
            "alert.cannot_delete_root.header",
            "alert.cannot_delete_root.content",
            null,
            new FormatArg("person", person)
        );
        return;
      }
      boolean delete = Alerts.confirmation(
          "alert.delete_person.header", null, "alert.delete_person.title");
      if (delete) {
        for (LifeEvent lifeEvent : person.getLifeEventsAsActor()) {
          if (lifeEvent.actors().size() <= lifeEvent.type().minActors()) {
            lifeEvent.actors().forEach(a -> a.removeLifeEvent(lifeEvent));
            lifeEvent.witnesses().forEach(w -> w.removeLifeEvent(lifeEvent));
          } else {
            person.removeLifeEvent(lifeEvent);
          }
        }
        for (LifeEvent lifeEvent : person.getLifeEventsAsWitness()) {
          lifeEvent.removeWitness(person);
        }
        for (Person child : new HashSet<>(person.children())) {
          for (Person.RelativeType type : Person.RelativeType.values()) {
            child.removeRelative(person, type);
          }
          child.removeParent(person);
        }
        this.familyTree.removePerson(person);
        this.familyTreePane.refresh();
        this.familyTreeView.refresh();
        this.defaultEmptyTree = false;
        this.unsavedChanges = true;
        this.updateUI();
      }
    });
  }

  private void onNewParentClick(@NotNull ChildInfo childInfo) {
    this.openEditPersonDialog(null, childInfo, EditPersonDialog.TAB_PROFILE);
  }

  private void onPersonClick(Person person, int clickCount, MouseButton button, boolean inTree) {
    this.focusedComponent = inTree ? this.familyTreeView : this.familyTreePane;
    if (App.config().shouldSyncTreeWithMainPane()) {
      if (inTree) {
        this.familyTreePane.selectPerson(person, button == TARGET_UPDATE_BUTTON);
      } else {
        this.familyTreeView.selectPerson(person, button == TARGET_UPDATE_BUTTON);
      }
    }
    if (clickCount == 2 && button == MouseButton.PRIMARY) {
      this.openEditPersonDialog(person, null, EditPersonDialog.TAB_PROFILE);
    }
    this.updateUI();
  }

  private void openEditPersonDialog(Person person, ChildInfo childInfo, int tabIndex) {
    this.editPersonDialog.setPerson(person, childInfo, this.familyTree);
    this.editPersonDialog.selectTab(tabIndex);
    Optional<Person> person_ = this.editPersonDialog.showAndWait();
    if (person_.isPresent()) {
      this.familyTreeView.refresh();
      this.familyTreePane.refresh();
      if (person == null && childInfo == null) {
        this.familyTreePane.selectPerson(person_.get(), false);
      }
      this.defaultEmptyTree = false;
      this.unsavedChanges = true;
      this.updateUI();
    }
  }

  private void updateUI() {
    this.stage.setTitle("%s – %s%s".formatted(App.NAME, this.familyTree.name(), this.unsavedChanges ? "*" : ""));

    boolean emptyTree = this.familyTree.persons().isEmpty();
    Optional<Person> selectedPerson = this.getSelectedPerson();
    boolean selection = selectedPerson.isPresent();
    boolean hasBothParents = selection && selectedPerson.get().hasBothParents();
    boolean selectedIsRoot = selection && selectedPerson.map(this.familyTree::isRoot).orElse(false);

    this.saveMenuItem.setDisable(!this.unsavedChanges || emptyTree);
    this.saveAsMenuItem.setDisable(emptyTree);
    this.setAsRootMenuItem.setDisable(!selection || selectedIsRoot);
    this.editPersonMenuItem.setDisable(!selection);
    this.removePersonMenuItem.setDisable(!selection || selectedIsRoot);
    this.addChildMenuItem.setDisable(!selection);
    this.addSiblingMenuItem.setDisable(!hasBothParents);
    this.editParentsMenuItem.setDisable(!selection);
    this.editLifeEventsMenuItem.setDisable(!selection);
    this.setPictureMenuItem.setDisable(!selection);

    this.saveToolbarButton.setDisable(!this.unsavedChanges || emptyTree);
    this.saveAsToolbarButton.setDisable(emptyTree);
    this.setAsRootToolbarButton.setDisable(!selection || selectedIsRoot);
    this.addChildToolbarButton.setDisable(!selection);
    this.addSiblingToolbarButton.setDisable(!hasBothParents);
    this.editParentsToolbarButton.setDisable(!selection);
    this.editLifeEventsToolbarButton.setDisable(!selection);
    this.setPictureToolbarButton.setDisable(!selection);
  }

  private void onSettingsAction() {
    this.settingsDialog.resetLocalConfig();
    this.settingsDialog.showAndWait();
  }

  private void onAboutAction() {
    this.aboutDialog.showAndWait();
  }

  private void onQuitAction() {
    if (!this.defaultEmptyTree && this.unsavedChanges) {
      boolean close = Alerts.confirmation(
          "alert.unsaved_changes.header", "alert.unsaved_changes.content", null);
      if (!close) {
        return;
      }
    }
    Platform.exit();
  }
}
