import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import javax.mail.*;
import java.io.IOException;
import java.util.Properties;


public class LoginController {
    @FXML
    private TextField email;
    @FXML
    private PasswordField pass;
    @FXML
    private Button login;
    @FXML
    private Button minimize;
    @FXML
    private Button exit;
    @FXML
    private Label error;

    private final String protocol = "imaps";

    private Stage stage;

    private String username;
    private String password;

    private double xOffset = 0;
    private double yOffset = 0;


    @FXML
    protected void emailEntered() {
        username = email.getText();
        System.out.println(username);
        onLoginPressed();
    }

    @FXML
    protected void emailMouseEnter() {
        email.setStyle("-fx-border-color: #9F52AE;" +
                "-fx-background-color: #1E0424;" +
                "-fx-border-radius: 20;" +
                "-fx-background-radius: 20;" +
                "-fx-text-inner-color: #FFFFFF");
    }

    @FXML
    protected void emailMouseExit() {
        email.setStyle("-fx-border-color: #9F52AE;" +
                "-fx-background-color: transparent;" +
                "-fx-border-radius: 20;" +
                "-fx-background-radius: 20;" +
                "-fx-text-inner-color: #FFFFFF");
    }

    @FXML
    protected void passwordEntered() {
        password = pass.getText();
        System.out.println(password);
        onLoginPressed();
    }

    @FXML
    protected void passwordMouseEnter() {
        pass.setStyle("-fx-border-color: #9F52AE;" +
                "-fx-background-color: #1E0424;" +
                "-fx-border-radius: 20;" +
                "-fx-background-radius: 20;" +
                "-fx-text-inner-color: #FFFFFF");
    }

    @FXML
    protected void passwordMouseExit() {
        pass.setStyle("-fx-border-color: #9F52AE;" +
                "-fx-background-color: transparent;" +
                "-fx-border-radius: 20;" +
                "-fx-background-radius: 20;" +
                "-fx-text-inner-color: #FFFFFF");
    }

    @FXML
    protected void onLoginPressed() {
        error.setVisible(false);
        stage = (Stage) login.getScene().getWindow();
        username = email.getText();
        password = pass.getText();
        Properties property = getServerProperties();
        Emails emails = new Emails(protocol);
        try {
            Store store = emails.connectStore(property, username, password);
            Folder inbox = emails.openFolder(store);
            System.out.println(inbox.getName());
            FXMLLoader loader = new FXMLLoader(getClass().getResource("application.fxml"));
            Parent root = loader.load();
            ApplicationController appController = loader.getController();
            appController.setEmails(emails, store);

            if (username.length() >= 25) {
                int atIndex = username.indexOf("@");
                appController.setConnected("Connected to: " + username.substring(0,5) + "..." + username.substring(atIndex - 4, atIndex) + username.substring(atIndex));
            }
            else
                appController.setConnected("Connected to: " + username);

            root.setOnMousePressed(event -> {
                xOffset = event.getSceneX();
                yOffset = event.getSceneY();
            });
            root.setOnMouseDragged(event -> {
                stage.setX(event.getScreenX() - xOffset);
                stage.setY(event.getScreenY() - yOffset);
            });
            Scene scene = new Scene(root);
            stage.setScene(scene);
        }
        catch (AuthenticationFailedException e) {
            error.setVisible(true);
            System.out.println("AuthenticationFailedException.");
        }
        catch (IOException e) {
            error.setVisible(true);
            System.out.println("IOException.");
        }
        catch (MessagingException e) {
            error.setVisible(true);
            System.out.println("MessagingException.");
        }
        catch (Exception e) {
            error.setVisible(true);
            System.out.println("BadCommandException.");
        }
    }

    @FXML
    protected void loginMouseEnter() {
        login.setStyle("-fx-background-color: #6A3674;" +
                "-fx-background-radius: 13");
    }

    @FXML
    protected void loginMouseExit() {
        login.setStyle("-fx-background-color: #9F52AE;" +
                "-fx-background-radius: 13");
    }

    @FXML
    protected void minimize() {
        stage = (Stage) minimize.getScene().getWindow();
        stage.setIconified(true);
    }

    @FXML
    protected void minimizeMouseEnter() {
        minimize.setStyle("-fx-background-color: #735979");
    }

    @FXML
    protected void minimizeMouseExit() {
        minimize.setStyle("-fx-background-color: #2E0737");
    }

    @FXML
    protected void close() {
        Platform.exit();
    }

    @FXML
    protected void exitMouseEnter() {
        exit.setStyle("-fx-background-color: #E81123;");
    }

    @FXML
    protected void exitMouseExit() {
        exit.setStyle("-fx-background-color: #2E0737");
    }

    private Properties getServerProperties() {
        Properties properties = new Properties();

        // server settings
        String host = "imap.gmail.com";
        properties.put(String.format("mail.imaps.host", protocol), host);
        String port = "993";
        properties.put(String.format("mail.imaps.port", protocol), port);
        properties.put("mail.imaps.ssl.trust", "*");

        return properties;
    }
}
