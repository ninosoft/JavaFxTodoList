package com.ninosoft.example.todolist;

import com.ninosoft.example.todolist.datamodel.TodoData;
import com.ninosoft.example.todolist.datamodel.TodoItem;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.util.Callback;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.Predicate;

public class Controller {

    @FXML
    private ListView<TodoItem> mTodoListView; //need to specify generics, TodoItem type
    @FXML
    private TextArea mDetailTextArea;
    @FXML
    private Label mDueDateLabel;
    @FXML
    private BorderPane mainBorderPane;
    @FXML
    private ContextMenu listContextMenu; //for right click menu
    @FXML
    private ToggleButton filterToggleButton;

    SortedList<TodoItem> sortedList;

    // To store the filtered list as per the defined Predicate method.
    private FilteredList<TodoItem> filteredList;

    // To define the filtering criteria
    // A java predicate is a function that takes in a value or object and the predicate will evaluate
    // if it meets the condition returning true or false.
    private Predicate<TodoItem> filterAllItems;
    private Predicate<TodoItem> filterTodaysItems;


    public void initialize() {
        //set the color for the un-toggle button.
        filterToggleButton.setStyle("-fx-background-color: lightsteelblue;");

        // Get the unsorted data array and saved in an  object implementing the ObservableList interface (JavaFX)
        // A list that allows listeners to track changes when they occur.
        ObservableList<TodoItem> sourceList = TodoData.getInstance().getTodoItemsList();

        // Create a filtered List to choose items to be displayed.
        // Filtered List Constructor
        // The predicate test method will define the criteria to filter the items.

        filterAllItems = new Predicate<TodoItem>() {
            @Override
            public boolean test(TodoItem todoItem) {
                //display all items for initial scene/display.
                return true;
            }
        };

        filterTodaysItems = new Predicate<TodoItem>() {
            @Override
            public boolean test(TodoItem todoItem) {
                //display todoItems with due date same as today.
                return todoItem.getDeadline().equals(LocalDate.now());
            }
        };


        //set filtered list.
        filteredList = new FilteredList<>(sourceList, filterAllItems);


        // Sort the filtered list.
        // From JavaFX.Collections, SortedList class. There is no SortedList in Java.
        sortedList = new SortedList<>(filteredList, new Comparator<TodoItem>() {
            @Override
            public int compare(TodoItem o1, TodoItem o2) {
                return o1.getDeadline().compareTo(o2.getDeadline());
            }
        });

        // Add the data to the listView Control
        mTodoListView.setItems(sortedList);


        /*
         * Add cellFactory to the ListView to be able to change the colors of the due date.
         *  A cell factory is used to  generate ListCell instances, which are used to represent
         *  an item in the ListView.
         * It can be used for changing graphics or properties based on conditions.
         * we pass an anonymous class that implements the Callback interface.
         * first argument is the control passed to the cellFactory, second argument is the type cell that will be return.
         * An instance of the listCell  will be return
         */
        mTodoListView.setCellFactory(new Callback<ListView<TodoItem>, ListCell<TodoItem>>() {
            @Override
            public ListCell<TodoItem> call(ListView<TodoItem> param) {
                //define a cell with an anonymous class, overriding the updateItem method.
                ListCell<TodoItem> cell = new ListCell<TodoItem>() {

                    //method to override to customise the visuals of the cell.
                    @Override
                    protected void updateItem(TodoItem item, boolean empty) {
                        super.updateItem(item, empty);//  must be done, for default appearance to be properly set.
                        //test for the empty condition, and if true, set the text and graphic properties to null.
                        if (empty) {
                            setText(null);
                        } else {
                            //Set cell text
                            setText(item.getShortDescription());
                            //Set Color
                            if (item.getDeadline().compareTo(LocalDate.now()) <= 0) {
                                setTextFill(Color.RED);
                            } else if (item.getDeadline().compareTo(LocalDate.now().plusDays(3)) <= 0) {
                                setTextFill(Color.ORANGE);
                            }
                        }
                    }
                };

                /*
                 * Associate the cell to the contextMenu ( for right-click menu) if the cell is not empty.
                 * Add a listener to the emptyProperty
                 */
                cell.emptyProperty().addListener(
                        (obs, wasEmpty, isNowEmpty) -> {
                            if (isNowEmpty) {
                                cell.setContextMenu(null);
                            } else {
                                cell.setContextMenu(listContextMenu); //associate the ContextMenu
                            }
                        }
                );
                return cell;
            }
        });


        /* Listener for list item clicked (left pane, Short description item).
         * Will show the detailed description (TextArea) and due dates (DatePicker) in the
         * UI Center Pane.
         * */
        mTodoListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TodoItem>() {
            @Override
            public void changed(ObservableValue<? extends TodoItem> observable, TodoItem oldValue, TodoItem newValue) {

                if (newValue != null) {
                    TodoItem item = mTodoListView.getSelectionModel().getSelectedItem();
                    mDetailTextArea.setText(item.getDetails());
                    //mDueDateLabel.setText(item.getDeadline().toString());
                    //Can use any format here because the data was converted into a date object
                    DateTimeFormatter df = DateTimeFormatter.ofPattern("E, MMM dd, yyyy");
                    mDueDateLabel.setText(df.format(item.getDeadline()));
                }
            }
        });


        /* Set first item as selected item during initialization.
         * Must be done after Selection Listener so that the Detail and date contents are displayed
         **/
        //set selection mode to single mode
        mTodoListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        //To select the 1st item in the ListView
        mTodoListView.getSelectionModel().selectFirst();


        /* CONTEXT MENU ITEM - DELETE
         * Create a context right-click Delete menu item
         * Note: Method is inside Initialize()
         * */
        MenuItem deleteMenuItem = new MenuItem();
        deleteMenuItem.setText("Delete");
        //set menu item event handler
        deleteMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                //NOTE: The menu is associated to the ListView item  cell by the cellFactory
                TodoItem item = mTodoListView.getSelectionModel().getSelectedItem();
                //our delete Item method
                deleteItem(item);
            }
        });

        //initialize the ContextMenu
        listContextMenu = new ContextMenu();
        //add the menu item to the Context menu
        listContextMenu.getItems().addAll(deleteMenuItem);
        //NOTE: The menu is associated to the ListView item  cell by the cellFactory

    } //end of javaFX Initialize method.


    /*
     * Method to delete an item using an alert dialog, Confirmation type.
     * Uses "Optional": A container object which may or may not contain a non-null value.
     * If a value is present, isPresent() will return true and get() will return the value.
     * ButtonType to specify which buttons are used in the dialogs.
     * */
    private void deleteItem(TodoItem item) {
        if (item != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Delete Item");
            alert.setHeaderText("Deleting item:  " + item.getShortDescription());
            alert.setContentText("\"Ok\" to confirm, \"Cancel\" to go back");
            //Shows the dialog and waits for the user response
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                TodoData.getInstance().deleteItem(item);
                if (sortedList.isEmpty()) {
                    mDetailTextArea.setText("");
                    mDueDateLabel.setText("");
                }
            }
        }
    }


    /* Handling Key Events
     * Method to handle the "delete" key pressed while on ListView
     * */
    @FXML
    public void handleKeyPressed(KeyEvent keyEvent) {
        TodoItem item = mTodoListView.getSelectionModel().getSelectedItem();
        if (keyEvent.getCode() == KeyCode.DELETE) {
            deleteItem(item);
        }
    }


    /*
     * Method to show the "add new item" dialog.
     */
    @FXML
    public void showAddNewItemDialog() {
        //Dialog class for a Dialog Pane.
        // ButtonType class. specifies which buttons should be shown to users in the dialogs
        Dialog<ButtonType> dialog = new Dialog<>();
        //Specifies the owner Window or null for a top-level, unowned stage.
        dialog.initOwner(mainBorderPane.getScene().getWindow());
        dialog.setTitle("Add New Todo Item");
        //dialog.setHeaderText("Header Text "); //not needed.
        //Set the dialog fXML layout  file location.
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("todoItemDialog.fxml"));

        //load the dialog fXML layout  file
        try {
            dialog.getDialogPane().setContent(fxmlLoader.load());
        } catch (IOException e) {
            System.out.println("Couldn't load the dialog");
            e.printStackTrace();
            return;
        }
        //add buttons to the dialog
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        boolean loop = true;
        while (loop) {
            //Show the dialog and suspend the event handle, waits for the user response
            //"Optional" - If a Button value is present, will return true and will return the value.
            Optional<ButtonType> buttonTypeValue = dialog.showAndWait();
            if (buttonTypeValue.isPresent() && buttonTypeValue.get() == ButtonType.OK) {
                //get the dialog controller class obj
                DialogController dialogController = fxmlLoader.getController();
                //call the controller method, save the result as newItem
                TodoItem newItem = dialogController.processResults();

                if (newItem != null) {
                    //reset the todoList
               /*
                No need to get the data. Already done by binding (using observable list.)
                mTodoItemsList = TodoData.getInstance().getTodoItemsList();
                mTodoListView.getItems().setAll(mTodoItemsList);*/
                    //set selection mode to single mode. To avoid user error selecting multiple items.
                    mTodoListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
                    //select the new item.
                    mTodoListView.getSelectionModel().select(newItem);
                    loop = false;
                    //System.out.println("OK Pressed");
                } else {
                    //System.out.println("New item had a null date");
                    loop = true;
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Incomplete Item Data");
                    alert.setHeaderText("Error: Missing Date!");
                    alert.setContentText("Please enter a completion date!");
                    alert.showAndWait();
                }
            } else {
                //System.out.println("Cancel Pressed");
                loop = false;
            }
        }
    }


    /*
     * Method to handle the Filter ToggleButton onAction
     * Filter for Today's date items and button color change.
     * Will maintain the selected item selected if it is in the filtered list.
     * */
    public void handleFilterToggle() {
        TodoItem selectedItem = mTodoListView.getSelectionModel().getSelectedItem();
        if (filterToggleButton.isSelected()) {
            filterToggleButton.setStyle("-fx-background-color: steelblue;"); //change color
            filteredList.setPredicate(filterTodaysItems); //set the filter
            if (filteredList.isEmpty()) {
                mDetailTextArea.setText("");
                mDueDateLabel.setText("");
            } else {
                if (filteredList.contains(selectedItem)) {
                    mTodoListView.getSelectionModel().select(selectedItem);
                } else {
                    mTodoListView.getSelectionModel().selectFirst();
                }
            }

        } else {
            // display all items.
            filterToggleButton.setStyle("-fx-background-color: lightsteelblue;");
            filteredList.setPredicate(filterAllItems);
            mTodoListView.getSelectionModel().select(selectedItem);
        }
    }

    /*
     * Method to exit the application using the File-Exit pull down menu from the Toolbar.
     * */
    public void handleExit(ActionEvent actionEvent) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Exiting Application");
        alert.setHeaderText("Are you sure you want to exit?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Platform.exit(); //Will call the app Stop method.
        } else {
            System.out.println("\"Cancel\" was pressed");
        }
    }
}

