import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import javax.mail.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.NoSuchFileException;

public class ApplicationController {
    @FXML
    private Button back;
    @FXML
    private Button browse;
    @FXML
    private Button downloadAttachments;
    @FXML
    private Button minimize;
    @FXML
    private Button exit;
    @FXML
    private TextField directoryField;
    @FXML
    private Label connected;
    @FXML
    private Label error;
    @FXML
    private Label error1;
    @FXML
    private Label error2;
    @FXML
    private Label error3;

    private Emails emails;
    private Store store;
    private Stage stage;
    private double xOffset = 0;
    private double yOffset = 0;

    public void setEmails(Emails emails, Store store) {
        this.emails = emails;
        this.store = store;
    }

    @FXML
    protected void setConnected(String message) {
        connected.setText(message);
    }

    @FXML
    protected void backToLogin() throws IOException {
        stage = (Stage) back.getScene().getWindow();
        Parent root = FXMLLoader.load(getClass().getResource("login.fxml"));
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

    @FXML
    protected void onBackMouseEnter() {
        back.setStyle("-fx-background-color: #6A3674;" +
                "-fx-background-radius: 20");
    }

    @FXML
    protected void onBackMouseExit() {
        back.setStyle("-fx-background-color: #9F52AE;" +
                "-fx-background-radius: 20");
    }

    @FXML
    protected void openDirectoryChooser() {
        DirectoryChooser dirChooser = new DirectoryChooser();
        File selectedDirectory = dirChooser.showDialog(stage);
        directoryField.setText(selectedDirectory.getAbsolutePath());
        emails.setDirectory(selectedDirectory.getAbsolutePath());
    }

    @FXML
    protected void directoryEntered() {
        emails.setDirectory(directoryField.getText());
        downloadAttachments.fire();
    }

    @FXML
    protected void directoryMouseEnter() {
        directoryField.setStyle("-fx-border-color: #9F52AE;" +
                "-fx-background-color: #1E0424;" +
                "-fx-border-radius: 20;" +
                "-fx-background-radius: 20;" +
                "-fx-text-inner-color: #FFFFFF");
    }

    @FXML
    protected void directoryMouseExit() {
        directoryField.setStyle("-fx-border-color: #9F52AE;" +
                "-fx-background-color: transparent;" +
                "-fx-border-radius: 20;" +
                "-fx-background-radius: 20;" +
                "-fx-text-inner-color: #FFFFFF");
    }

    @FXML
    protected void onBrowseMouseEnter() {
        browse.setStyle("-fx-background-color: #6A3674;" +
                "-fx-background-radius: 20");
    }

    @FXML
    protected void onBrowseMouseExit() {
        browse.setStyle("-fx-background-color: #9F52AE;" +
                "-fx-background-radius: 20");
    }

    @FXML
    protected void onDownloadMouseEnter() {
        downloadAttachments.setStyle("-fx-background-color: #6A3674;" +
                "-fx-background-radius: 20");
    }

    @FXML
    protected void onDownloadMouseExit() {
        downloadAttachments.setStyle("-fx-background-color: #9F52AE;" +
                "-fx-background-radius: 20");
    }

    @FXML
    protected void downloadPressed() throws IOException {
        try {
            emails.setDirectory(directoryField.getText());
            emails.createSubDirectories();
            Folder inbox = emails.openFolder(store);
            Message[] messages = emails.openMessages(inbox);
            for (int i = 0; i < messages.length; i++) {
                String fromAddress = emails.getFromAddress(messages[i]);
                String emailsubject = emails.getSubject(messages[i]);
                System.out.println("From: " + fromAddress);
                System.out.println(emailsubject);
                emails.downloadAttachments(messages[i]);
            }
            emails.closeFolder(inbox);
            emails.disconnectStore(store);
        }
        catch (NullPointerException e) {
            error.setVisible(false);
            error1.setVisible(false);
            error3.setVisible(false);
            System.out.println("NullPointerException");
            error2.setVisible(true);
        }
        catch (NoSuchFileException e) {
            error.setVisible(false);
            error1.setVisible(false);
            error2.setVisible(false);
            System.out.println("NoSuchFileException");
            error3.setVisible(true);
        }
        catch (FolderClosedException e) {
            error1.setVisible(false);
            error2.setVisible(false);
            error3.setVisible(false);
            System.out.println("FolderClosedException");
            error.setVisible(true);
        }
        catch (MessagingException e) {
            error.setVisible(false);
            error2.setVisible(false);
            error3.setVisible(false);
            System.out.println("MessagingException");
            error1.setVisible(true);
        }
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
}
