package com.ninosoft.example.todolist.datamodel;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;

//SINGLETON CLASS
public class TodoData {
    //single instance for singleton class
    private static TodoData instance = new TodoData();
    //file name to write and read data
    private static String filename = "TodoListItems.txt";

    //ObservableList for the todoItem objects. Used for data binding.
    //A list that allows listeners to track changes when they occur.
    private ObservableList<TodoItem> todoItemsList;
    //Formatter for printing and parsing date-time objects.
    private DateTimeFormatter formatter;

    //method to get the single instance. Required for singleton class.
    public static TodoData getInstance() {
        return instance;
    }

    //private constructor, required for singleton class.
    private TodoData() {
        formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");//defining the format
    }

    public ObservableList<TodoItem> getTodoItemsList() {
        return todoItemsList;
    }

/* Only used for testing during app development
    public void setTodoItemsList(List<TodoItem> todoItemsList) {
        this.todoItemsList = todoItemsList;
    }
*/

    /* Method to read todoItems data. */
    public void loadItems() throws IOException {
        //Creates a new empty observable list that is backed by an arrayList.
        todoItemsList = FXCollections.observableArrayList();
        //Converts the given URI to a Path object.
        Path path = Paths.get(filename);
        BufferedReader br = Files.newBufferedReader(path);

        String input;
        try {
            while ((input = br.readLine()) != null) { //reads on line at a time..
                String[] itemFields = input.split("\t"); //convert line into array
                String shortDescription = itemFields[0];
                String details = itemFields[1];
                String dateString = itemFields[2];
                //Local date returns a string, so we need to provide the format.
                LocalDate date = LocalDate.parse(dateString, formatter);
                TodoItem todoItem = new TodoItem(shortDescription, details, date);
                todoItemsList.add(todoItem);
            }
        } finally {
            if (br != null) {
                br.close();
            }
        }
    }

    /* Method to write todoItems data */
    public void storeTodoItems() throws IOException {
        Path path = Paths.get(filename);
        BufferedWriter bw = Files.newBufferedWriter(path);
        try {
            Iterator<TodoItem> iter = todoItemsList.iterator();
            while (iter.hasNext()) {
                TodoItem item = iter.next();
                bw.write(String.format("%s\t%s\t%s",
                        item.getShortDescription(),
                        item.getDetails(),
                        //save the date as a String
                        item.getDeadline().format(formatter)));
                bw.newLine();
            }
        } finally {
            if (bw != null) {
                bw.close();  //if the bufferedWriter is not close the file content is not write.
            }
        }
    }


    public void addTodoItem(TodoItem todoItem) {
        todoItemsList.add(todoItem);
    }

    public void deleteItem(TodoItem item) {
        todoItemsList.remove(item);
    }
}



