package sample;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.StageStyle;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Optional;
import java.util.Properties;
import java.util.Random;


public class Controller {
    @FXML
    private TextField txtFieldGuessedNumber;

    @FXML
    private Button btnConfirmGuessedNumber;

    @FXML
    private TextArea txtAreaDisplayHistory;

    @FXML
    private Button btnClearHistory;

    @FXML
    private Button btnUpdateHistory;

    @FXML
    private Label lblBest;

    @FXML
    private Label lblWorst;

    @FXML
    private Label lblCurrent;

    public void initialize(){
        updateHistory();
        updateLabels();
        txtAreaDisplayHistory.requestFocus();
        txtAreaDisplayHistory.setScrollTop(Double.MAX_VALUE);
    }



    @FXML
    void btnConfirmGuessedNumberHandler (){
        int actualNumber = Integer.parseInt(getMyProperty("guessNumber"));
        incrementCurrent();
        if(Integer.parseInt(txtFieldGuessedNumber.getText()) > actualNumber){
            saveHigherResultToTxtFile(txtFieldGuessedNumber.getText());
            //System.out.println("Lower mate");
        }
        else if(Integer.parseInt(txtFieldGuessedNumber.getText()) < actualNumber){
            saveLowerResultToTxtFile(txtFieldGuessedNumber.getText());
            //System.out.println("Higher mate");
        }
        else{
            saveCorrectResultToTxtFile(txtFieldGuessedNumber.getText());
            if(Integer.parseInt(getMyProperty("current")) > Integer.parseInt(getMyProperty("longest"))){
                setMyProperty("longest", getMyProperty("current"));
            }
            if (Integer.parseInt(getMyProperty("current")) < Integer.parseInt(getMyProperty("shortest"))){
                setMyProperty("shortest", getMyProperty("current"));
            }
            setMyProperty("current", "0");
            //System.out.println("You got it right mate");
        }
        updateLabels();
        updateHistory();
        txtFieldGuessedNumber.clear();
        txtFieldGuessedNumber.requestFocus();
    }

    @FXML
    void btnClearHistoryHandler () {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Mordo, to kawał historii, chcesz usunąć?");
        alert.setHeaderText("Gonna wipe this sh*t!");
        alert.setContentText("Usuwamy czy nie, nie mam czasu?!");
        alert.initStyle(StageStyle.UTILITY);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            setMyProperty("shortest", "99999");
            setMyProperty("longest", "0");
            setMyProperty("current", "0");
            setMyProperty("guessNumber", generateRandomGuessNumberAsString());
            try {
                new PrintWriter("historia.txt").close();
            } catch (FileNotFoundException e) {
                System.out.println("Nie udało się wyczyścić!");
            }
        }
        else {
            alert.close();
        }
        updateLabels();

    }

    @FXML
    void btnUpdateHistoryHandler () {
        updateHistory();
    }

    private void updateHistory() {
        txtAreaDisplayHistory.clear();
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(new File("historia.txt")));
            String currentLine;
            int i = 1;
            while((currentLine = bufferedReader.readLine()) != null){
                if (i%3 != 0) {
                    //System.out.println("Line read: " + currentLine);
                    txtAreaDisplayHistory.appendText(currentLine + "\n");
                }
                else{
                    txtAreaDisplayHistory.appendText("----------\n");
                }
                i++;
            }
        } catch (IOException e) {
            System.out.println("Couldn't open the file biatch");
        }
        txtAreaDisplayHistory.setScrollTop(Double.MAX_VALUE);
        System.out.println(getLastGuessDate());
    }

    //zmienić zeby zwracało date nie stringa i potem zrobić porównanie tej
    //i daty ostatniego zgadnięcia, sprawdzic czy minęło 20h
    public String getFormattedCurrentDateTimeAsString () {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDate = now.format(formatter);
        return formattedDate;
    }

    void saveLowerResultToTxtFile (String guessedNumber) {
        try {
            String textToFile =guessedNumber + "\n" + "Too low!" + "\n" + getFormattedCurrentDateTimeAsString() + "\n";
            Path filePath = Paths.get("historia.txt");
            Files.write(filePath, textToFile.getBytes(), Files.exists(filePath) ? StandardOpenOption.APPEND : StandardOpenOption.CREATE);
        } catch (IOException e) {
            System.out.println("Can't open the file!");
        }
    }

    void saveHigherResultToTxtFile (String guessedNumber){
        try {
            String textToFile =guessedNumber + "\n" + "Too high!" + "\n" + getFormattedCurrentDateTimeAsString() + "\n";
            Path filePath = Paths.get("historia.txt");
            Files.write(filePath, textToFile.getBytes(), Files.exists(filePath) ? StandardOpenOption.APPEND : StandardOpenOption.CREATE);
        } catch (IOException e) {
            System.out.println("Can't open the file!");
        }
    }

    void saveCorrectResultToTxtFile (String guessedNumber){
        try {
            String textToFile =guessedNumber + "\n" + "You got it right mate" + "\n" + getFormattedCurrentDateTimeAsString() + "\n";
            Path filePath = Paths.get("historia.txt");
            Files.write(filePath, textToFile.getBytes(), Files.exists(filePath) ? StandardOpenOption.APPEND : StandardOpenOption.CREATE);
        } catch (IOException e) {
            System.out.println("Can't open the file!");
        }
    }

    String getMyProperty(String propertyName) {
        File configFile = new File("shortestLongest.properties");
        String property = "";
        try{
            FileReader fileReader = new FileReader(configFile);
            Properties properties = new Properties();
            properties.load(fileReader);
            property = properties.getProperty(propertyName);
            fileReader.close();
        }
        catch(Exception e){
            System.out.println("Nie można otoworzyć pliku!");
        }
        return property;
    }

    void setMyProperty(String propertyName, String propertyValue){
        File configFile = new File("shortestLongest.properties");
        try{
            FileReader fileReader = new FileReader(configFile);
            Properties properties = new Properties();
            properties.load(fileReader);
            properties.setProperty(propertyName, propertyValue);
            FileWriter fileWriter = new FileWriter(configFile);
            properties.store(fileWriter, null);
            fileWriter.close();
        }
        catch(Exception e){
            System.out.println("Nie można otoworzzyć pliku!");
        }
    }

    void incrementCurrent (){
        String current = getMyProperty("current");
        int incCurrent = Integer.parseInt(current) + 1;
        setMyProperty("current", Integer.toString(incCurrent));
    }

    void updateLabels () {
        lblBest.setText(getMyProperty("shortest"));
        lblWorst.setText(getMyProperty("longest"));
        lblCurrent.setText(getMyProperty("current"));
    }

    String generateRandomGuessNumberAsString(){
        Random randomGenerator = new Random();
        int random = randomGenerator.nextInt(70);
        String sRandom = Integer.toString(random);
        return sRandom;
    }

    public Date getLastGuessDate (){
        String sDate = "";
        String sDateProperFormat = "";
        Date date = null;
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(new File("historia.txt")));
            String currentLine;
            int i = 1;
            while((currentLine = bufferedReader.readLine()) != null){
                if (i%3 == 0) {
                    System.out.println("Line read: " + currentLine);
                    sDate = currentLine;
                }
                i++;
            }
            //System.out.println("Data zebrana: " + sDate);
        } catch (IOException e) {
            System.out.println("Couldn't open the file biatch");
        }
        sDateProperFormat = sDate.replace('T', ' ');
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            date = formatter.parse(sDateProperFormat);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }
}
