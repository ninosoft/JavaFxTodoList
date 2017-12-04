package com.ninosoft.example.todolist;

import com.ninosoft.example.todolist.datamodel.TodoData;
import com.ninosoft.example.todolist.datamodel.TodoItem;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.time.LocalDate;

public class DialogController {

    @FXML
    public TextField descriptionField;
    @FXML
    public TextArea detailsAreaField;
    @FXML
    public DatePicker deadLinePicker;

    /*public*/ TodoItem processResults() {
        String description = descriptionField.getText().trim();
        String details = detailsAreaField.getText().trim();
        if (deadLinePicker.getValue() != null) {
            LocalDate deadLineValue = deadLinePicker.getValue();
            //System.out.println(description + "  " + details + deadLineValue);

            TodoItem newItem = new TodoItem(description, details, deadLineValue);
            TodoData.getInstance().addTodoItem(newItem);
            return newItem;
        }
        return null;
    }

}
