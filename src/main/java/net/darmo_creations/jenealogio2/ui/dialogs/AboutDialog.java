package net.darmo_creations.jenealogio2.ui.dialogs;

import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import net.darmo_creations.jenealogio2.*;
import net.darmo_creations.jenealogio2.utils.*;

import java.util.*;

/**
 * Dialog that displays information about this app. It is not resizable.
 */
public class AboutDialog extends DialogBase<ButtonType> {
  /**
   * Create an about dialog.
   */
  public AboutDialog() {
    super("about", false, ButtonTypes.CLOSE);
    Label titleLabel = new Label();
    titleLabel.setText(App.NAME);
    titleLabel.setStyle("-fx-font-size: 1.2em; -fx-font-weight: bold");
    VBox.setMargin(titleLabel, new Insets(10));

    TextArea contentView = new TextArea();
    contentView.setText("""
        App version: %s
                
        Developped by Damia Vergnet (@Darmo117 on GitHub).
        This application is available under GPL-3.0 license.
                
        Icons from FatCow (https://github.com/gammasoft/fatcow).
                
        Map view by Gluon Maps (https://github.com/gluonhq/maps).
                
        Map tile data and geocoding service by OpenStreetMap and its contributors (https://www.openstreetmap.org/copyright).
                
        System properties:
        %s
        """.formatted(App.VERSION, App.getSystemProperties()));
    contentView.setEditable(false);
    VBox.setVgrow(contentView, Priority.ALWAYS);

    VBox vBox = new VBox(10, titleLabel, contentView);

    Image appIcon = App.config().theme().getAppIcon();
    ImageView logo = new ImageView(appIcon);
    logo.setFitHeight(100);
    logo.setFitWidth(100);

    HBox content = new HBox(10, logo, vBox);
    content.setPrefWidth(600);
    content.setPrefHeight(300);
    this.getDialogPane().setContent(content);

    this.setIcon(appIcon);
  }

  @Override
  protected List<FormatArg> getTitleFormatArgs() {
    return List.of(new FormatArg("app_name", App.NAME));
  }
}
