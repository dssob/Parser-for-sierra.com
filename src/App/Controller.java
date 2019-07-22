package App;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    @FXML private javafx.scene.control.TextField URLbox;
    @FXML private javafx.scene.control.TextField NameBox;
    @FXML private Button Parsebtn;
    @FXML private Button Closebtn;
    @FXML private Label Worker;
    @FXML private ProgressIndicator Progress;

    //переменные с данными
    private static ArrayList<String> titlesv = new ArrayList<>();
    private static ArrayList<String> hrefsv = new ArrayList<>();
    private static ArrayList<String> pricesv = new ArrayList<>();
    private static ArrayList<String> imageURLsv = new ArrayList<>();
    private static ArrayList<String> oldpricesv = new ArrayList<>();
    private String FileName;



    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Progress.setVisible(false);

        Worker.setText("Ready");

        Parsebtn.setOnAction(new EventHandler<ActionEvent>() { //обработчик нажатия кнопки "Parse"
            @Override
            public void handle(ActionEvent event) {
                System.out.println("Parse button pressed");

                hrefsv.clear();
                titlesv.clear();
                pricesv.clear();
                imageURLsv.clear();
                oldpricesv.clear();

                Worker.setText("Running");
                Progress.setVisible(true);


                String URL = URLbox.getText();
                //String URL = "https://www.sierra.com/clearance~1/specdataor~gender!men/colorfamily~red/8/";

                FileName = NameBox.getText();

                //System.out.println(URL + " " + FileName);


               ArrayList<String> nextpageURLs = new ArrayList<>(Parsenumberofpages(URL));

               //System.out.println(nextpageURLs.size());


                Thread controller = new Thread(new parsing_controller(nextpageURLs));
                controller.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {

                    public void uncaughtException(Thread t, Throwable e) {

                        Worker.setVisible(false);
                        Platform.runLater(() -> Worker.setText("Task completed"));

                        System.out.println(hrefsv);
                        System.out.println(titlesv);
                        System.out.println(pricesv);
                        System.out.println(imageURLsv);
                        System.out.println(oldpricesv);

                        excelWriter();

                        System.out.println(hrefsv.size() + " " + titlesv.size() + " " + pricesv.size() + " " + imageURLsv.size() + " " + oldpricesv.size());

                        Worker.setVisible(true);
                        Progress.setVisible(false);
                    }
                });

                controller.start();

               //System.out.println(nextpageURLs.size());*/







            }
        });

        Closebtn.setOnAction(new EventHandler<ActionEvent>() { //обработчик нажатия кнопки "Close"
            @Override
            public void handle(ActionEvent event) {
                System.out.println("Close button pressed");
                System.exit(0);
            }
        });

    }


    private ArrayList Parsenumberofpages(String URL){ //метод для получения количества страниц с товарами
        ArrayList<String> set = new ArrayList<>();
        try {
            Document page = getPage(URL);

            Elements pages = page.select("div [class=pagination pagination--sm] a[class=pageLink lastPage]");
            String num = pages.text();
            //System.out.println(pages);

            if(num.equals("") || num.equals(" ")){
                String num1 = "";
                String num2 = "";
                int num1h = 0;
                int num2h = 0;
                try{
                    Elements pages1 = page.select("div [class=pagination pagination--sm] a[class=pageLink]");
                //System.out.println(pages1.get(pages1.size() - 2).text());
                num1 = pages1.get(pages1.size() - 2).text();
                num1h = Integer.parseInt(num1.substring(num1.lastIndexOf(' ') + 1));
                }
                catch (NumberFormatException e1){
                    num1h = 0;
                }

                try{
                    Elements pages2 = page.select("div [class=pagination pagination--sm] a[class=pageLink active]");
                    //System.out.println(pages2.text());
                    num2 = pages2.text();
                    num2h = Integer.parseInt(num2.substring(num2.lastIndexOf(' ') + 1));
                }
                catch (NumberFormatException e2){
                    num2h = 0;
                }




                if(num1h > num2h) num = num1;
                else num = num2;

            }
            num = num.trim();
            int lastnum = Integer.parseInt(num.substring(num.lastIndexOf(' ') + 1));

            pages = page.select("div [class=pagination pagination--sm] a[class=pageLink]");
            String parsedURL = pages.attr("href");
            String[] parts = parsedURL.split("/");
            try{
                Integer.parseInt(parts [parts.length-1]);
                String[] without = Arrays.copyOf(parts, parts.length-1);
                parsedURL = Arrays.toString(without);
                parsedURL = parsedURL.substring(1, parsedURL.length()-1).replace(", ", "/");
                }
            catch (Exception e0){

            }


            System.out.println(parsedURL);

           for (int i = 1; i<= lastnum; i++ ) {
               set.add("https://www.sierra.com" + parsedURL + "/" + i + "/");

           }
            System.out.println(set);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return set;

    }


    public void parseData(String URL){ //метод для парсинга данных

        //Progress.setVisible(true);
        try{
            Document page = getPage(URL);
            Elements elements = page.select("div [id=products]");
            Elements titlebars= elements.select("div [class=productCard-title-name]");
            for (Element element : titlebars){
                Elements atributes = element.select("a");
                String data = removeWord(atributes.attr("title"), "View");
                String href = atributes.attr("href");
                System.out.println(data);
                System.out.println("https://www.sierra.com" + href);
                hrefsv.add("https://www.sierra.com" + href);
                titlesv.add(data);
            }

           Elements pricingdetails = elements.select("div [class=productPricing prices priceBlock pricingRightBorder] span");
            for (Element price : pricingdetails ){

                String pricev = (price.text());
                System.out.println(pricev);
                pricesv.add(pricev);
            }

            Elements oldpricingdetails = elements.select("div [class=productPricing savingsBlock] span");
            for (Element price : oldpricingdetails ){

                String oldpricev = (price.text());
                oldpricev = oldpricev.substring(oldpricev.lastIndexOf(' ') + 1);
                System.out.println("OLD: " + oldpricev);
                oldpricesv.add(oldpricev);
            }

           Elements imagedetails = elements.select("div [class=productImageContainer MediumLargerG4] a img");
            System.out.println(imagedetails);
            for (Element imageURL : imagedetails ){
                String URLv = imageURL.attr("src");
                if(URLv.equals("https://s.stpost.com/img/blank.gif")){
                    URLv = imageURL.attr("data-src");
                }
                System.out.println(URLv);
                imageURLsv.add(URLv);
            }





        }
        catch (IOException e){
            System.out.println("Error" + e);
            e.printStackTrace();
        }
}
    public static String removeWord(String string, String word) //убераем лишние данные
    {
        if (string.contains(word)) {
            String tempWord = word + " ";
            string = string.replaceAll(tempWord, "");
            tempWord = " " + word;
            string = string.replaceAll(tempWord, "");
        }

        // Return the resultant string
        return string;
    }


    private static Document getPage(String URL) throws IOException {  //метод подключения к старанице

        Document page = Jsoup.connect(URL).userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.77 Safari/537.36").timeout(0).execute().parse();

        /*Document page = Jsoup.connect(URL).proxy("185.253.97.135", 443).userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.77 Safari/537.36")
                .timeout(0).maxBodySize(0).get();*/
        System.out.println(page);
        return page;
    }



    private void excelWriter(){ //метод для записи данных в файл
        Workbook writeWorkbook = new HSSFWorkbook();
        Sheet sheet1 = writeWorkbook.createSheet("Data");
        CellStyle style = writeWorkbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        for (int i = 0; i < titlesv.size(); i++){
            if (i == 0){
                Row row1 = sheet1.createRow(i);
                Cell cellhref = row1.createCell(0);
                Cell celltitle = row1.createCell(1);
                Cell cellprice = row1.createCell(2);
                Cell celloldprice = row1.createCell(3);
                Cell cellimageUrl = row1.createCell(4);


                cellhref.setCellValue("Link");
                cellhref.setCellStyle(style);
                celltitle.setCellValue("Title");
                celltitle.setCellStyle(style);
                cellprice.setCellValue("Price");
                cellprice.setCellStyle(style);
                celloldprice.setCellValue("Old price");
                celloldprice.setCellStyle(style);
                cellimageUrl.setCellValue("Image");
                cellimageUrl.setCellStyle(style);



            }
            Row row1 = sheet1.createRow(i+1);
            Cell cellhref = row1.createCell(0);
            Cell celltitle = row1.createCell(1);
            Cell cellprice = row1.createCell(2);
            Cell celloldprice = row1.createCell(3);
            Cell cellimageUrl = row1.createCell(4);

            cellhref.setCellValue(hrefsv.get(i));
            celltitle.setCellValue(titlesv.get(i));
            cellprice.setCellValue(pricesv.get(i));
            celloldprice.setCellValue(oldpricesv.get(i));
            cellimageUrl.setCellValue(imageURLsv.get(i));

        }


        try {
            String userHomeFolder = System.getProperty("user.home") + "/Desktop";
            File file = new File(userHomeFolder, FileName + ".xls");
            FileOutputStream fileOut = new FileOutputStream(file);
            writeWorkbook.write(fileOut);
            writeWorkbook.close();
            fileOut.close();




        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

}





class parsing_controller implements Runnable{ //класс для создания второго потока, с целью освождения главного потока
    ArrayList<String> URLs;
    public parsing_controller(ArrayList URLs){
        this.URLs = URLs;
    }

    @Override
    public void run() {
        //System.out.println(URLs);

        for(int i = 0; i < URLs.size(); i++){
            Controller controller = new Controller();
            controller.parseData(URLs.get(i));

    }
        throw new RuntimeException();
}
}




