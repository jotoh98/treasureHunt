package com.treasure.hunt.view.widget;

import com.treasure.hunt.service.preferences.PreferenceService;
import javafx.beans.InvalidationListener;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.VBox;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * @author axel1200
 */
@Slf4j
public class PreferencesWidgetController {

    public TextField nameTextField;
    public TextField valueTextField;
    public VBox popupPane;
    public Label errorLabel;
    public TableView<Pair<String, Number>> preferencesTable;
    public TableColumn<Pair<String, Number>, String> nameColumn;
    public TableColumn<Pair<String, Number>, String> valueColumn;
    private ObservableList<Pair<String, Number>> items = FXCollections.observableArrayList();

    public void initialize() {
        popupPane.managedProperty().bind(popupPane.visibleProperty());
        errorLabel.managedProperty().bind(errorLabel.visibleProperty());
        popupPane.setVisible(false);

        preferencesTable.setEditable(true);
        valueColumn.setEditable(true);
        valueColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        valueColumn.setOnEditCommit(event -> {
            NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.US);
            try {
                Number number = numberFormat.parse(event.getNewValue());
                PreferenceService.getInstance().putPreference(event.getRowValue().getKey(), number);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        nameColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getKey()));
        valueColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getValue().toString()));

        preferencesTable.setItems(items);
        InvalidationListener invalidationListener = observable -> {
            items.clear();
            PreferenceService.getInstance().getPreferences().
                    forEach((key, value) -> items.add(new Pair<>(key, value)));
        };
        PreferenceService.getInstance().getPreferences().addListener(invalidationListener);
        invalidationListener.invalidated(null);

    }

    public void addItem() {
        errorLabel.setVisible(false);
        nameTextField.setText(null);
        valueTextField.setText(null);
        popupPane.setVisible(true);
    }

    public void deleteItem() {
        Pair<String, Number> selectedItem = preferencesTable.getSelectionModel()
                .getSelectedItem();
        if (selectedItem == null) {
            return;
        }
        PreferenceService.getInstance()
                .deletePreferences(selectedItem.getKey());
    }

    public void addCancel() {
        popupPane.setVisible(false);
    }

    public void addAdd() {
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.US);
        try {
            Number number = numberFormat.parse(valueTextField.getText());
            PreferenceService.getInstance().putPreference(nameTextField.getText(), number);
        } catch (Exception e) {
            errorLabel.setVisible(true);
            return;
        }
        popupPane.setVisible(false);
    }
}

