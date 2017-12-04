package com.ninosoft.example.todolist;


import com.ninosoft.example.todolist.datamodel.TodoData;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;


public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("main_window.fxml"));
        primaryStage.setTitle("Todo List");
        primaryStage.setScene(new Scene(root, 1280, 720));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }

    //Read the data during initialization.
    @Override
    public void init() {
        try {
            TodoData.getInstance().loadItems();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    //Store (write) the data every time we close the application
    @Override
    public void stop() {
        try {
            TodoData.getInstance().storeTodoItems(); //calling the singleton class method.
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
