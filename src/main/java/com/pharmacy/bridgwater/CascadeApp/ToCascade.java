package com.pharmacy.bridgwater.CascadeApp;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.commons.io.FileUtils;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import static com.pharmacy.bridgwater.CascadeApp.Server.*;
import static com.pharmacy.bridgwater.CascadeApp.constants.Constants.*;


public class ToCascade {







    public static void main(String[] args) throws IOException, InterruptedException {
        ToCascade cascadeApp = new ToCascade();

        cascadeApp.placeOrder(WORK_TO_BE_DONE_FILE_NAME);
    }


    public void placeOrder(String workToBeDoneFileName) throws IOException, InterruptedException {



        FileInputStream file = new FileInputStream(workToBeDoneFileName);
        Workbook workbook = new XSSFWorkbook(file);
        Sheet sheet = workbook.getSheetAt(0);


        Writer cascadeUploadFile = new FileWriter(CASCADE_UPLOAD_FILE_NAME);
        Writer mappingFile = new FileWriter(CASCADE_UPLOAD_FILE_NAME_WITH_ORDER_LIST_SNO);

        try{
            System.out.println("lastRowNumber::"+sheet.getLastRowNum());



            for (int i = 1; i <= sheet.getLastRowNum() && sheet.getRow(i) != null  ; i++) {
                if( sheet.getRow(i).getCell(ORDER_LIST_FROM) != null
                        && sheet.getRow(i).getCell(ORDER_LIST_FROM).getCellType() != CellType.BLANK
                        &&sheet.getRow(i).getCell(ORDER_LIST_FROM).toString().trim().equalsIgnoreCase("DD")

                        && sheet.getRow(i).getCell(ORDER_LIST_PIP) != null
                        &&sheet.getRow(i).getCell(ORDER_LIST_PIP).getCellType() != CellType.BLANK
                        && !sheet.getRow(i).getCell(ORDER_LIST_PIP).toString().trim().equals("")
                        ) {
                    try{


                        String content = Double.valueOf(String.valueOf(sheet.getRow(i).getCell(ORDER_LIST_PIP))).intValue()+","+Double.valueOf(String.valueOf(sheet.getRow(i).getCell(ORDER_LIST_QTY))).intValue() + "\r\n";
                        cascadeUploadFile.write(content);

                    }catch (Exception e){
                        System.out.println("value of i ="+i );
                        System.out.println(String.valueOf(sheet.getRow(i).getCell(ORDER_LIST_QTY)));
                        System.out.println(String.valueOf(sheet.getRow(i).getCell(ORDER_LIST_FROM)));
                        System.out.println();
                    }

                }

            }



        }catch (Exception e){
            e.printStackTrace();
            System.out.println("Exception occured while writing to the cascade upload file and its mapping file");
        }finally {
            cascadeUploadFile.flush();
            cascadeUploadFile.close();
            mappingFile.flush();
            mappingFile.close();
            file.close();
        }




        System.out.println();



        WebDriverManager.chromedriver().setup();
        WebDriver driver = new ChromeDriver();
        driver.get("https://victoria-os.com/UI/home/aahcascade");

        driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[1]/header[1]/div[1]/section[1]/ul[1]/li[2]/a[1]")).sendKeys(Keys.RETURN);
        driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[2]/section[1]/section[1]/form[1]/div[1]/fieldset[1]/div[2]/input[1]")).sendKeys("Bridgwater");
        driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[2]/section[1]/section[1]/form[1]/div[1]/fieldset[1]/div[4]/input[1]")).sendKeys("Brid@8486");

        driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[2]/section[1]/section[1]/form[1]/div[1]/fieldset[1]/div[6]/input[1]"))
                .sendKeys(Keys.RETURN);
        Thread.sleep(1000);



        //cascade order pad click
        driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[1]/header[1]/div[2]/nav[1]/div[2]/ul[1]/li[3]/a[1]")).click();
        Thread.sleep(1000);

        int numItems = 0;
        String numOfItemsInBanner = driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[2]/form[1]/div[10]/div[2]/div[1]/div[1]/div[1]/div[1]/div[1]")).getText();
        Pattern itemsBannerPattern = Pattern.compile("of\\s+(\\d+)");
        Matcher itemsBannerMatcher = itemsBannerPattern.matcher(numOfItemsInBanner);

        if (itemsBannerMatcher.find()) {
            String number = itemsBannerMatcher.group(1);
            numItems = Integer.valueOf(number);
        } else {
            System.out.println("Could not find Items Filtered: 0 of 57");
        }

        int numberOfPages = numItems!=0? numItems/20: 1;
        for(int i=0;i<numberOfPages+1;i++){
            Thread.sleep(1000);
            // clearing the list, select the select box
            driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[2]/form[1]/div[10]/div[2]/div[1]/div[1]/div[1]/div[1]/table[1]/thead[1]/tr[1]/th[1]/input[1]")).click();
            Thread.sleep(1000);
            //select the delete button
            driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[2]/form[1]/div[1]/div[2]/div[2]/div[1]/div[1]/button[1]")).click();
            Thread.sleep(1000);
            driver.findElement(By.xpath("/html/body/div[4]/div/div/div[3]/div/div/button[2]")).click();

        }



        Thread.sleep(5000);

        //click on upload button
        driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[2]/form[1]/div[1]/div[2]/div[2]/div[1]/div[1]/button[4]")).click();
        Thread.sleep(1000);

        //click on choose button
        driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[2]/form[1]/div[3]/div[1]/div[2]/div[1]/div[1]/div[2]/input[1]"))
                .sendKeys(CASCADE_UPLOAD_FILE_BASE_LOCATION+"\\"+CASCADE_UPLOAD_FILE_NAME);

        //click on import button
        driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[2]/form[1]/div[3]/div[1]/div[2]/div[1]/div[3]/div[2]/button[1]"))
                .click();

        Thread.sleep(10000);
        // if there are any exceptions in cascade uploaded file, need to remove from the mapping list
        List<Integer> snoToBeDeleted = new LinkedList<>();
        try{
            List<WebElement> errorTableList = driver.findElements(By.xpath("/html/body/div[1]/div[2]/form/div[6]/div/div/div[2]/div/table/tr"));
            System.out.println("size of error table :::"+errorTableList.size());
            for(int i=1;i<=errorTableList.size();i++){
                String snoString = driver.findElement(By.xpath("/html/body/div[1]/div[2]/form/div[6]/div/div/div[2]/div/table/tr["+i+"]/td[1]")).getText();
                Integer sno = Integer.valueOf(snoString);
                snoToBeDeleted.add(sno);
            }

            // delete from mapping list

            File overridenMappingFile = new File(CASCADE_UPLOAD_FILE_NAME_WITH_ORDER_LIST_SNO);
            List<String> lines = FileUtils.readLines(overridenMappingFile);
            List<String> linesTobeDeleted = new LinkedList<>();

            for(Integer sno : snoToBeDeleted){
                String s = lines.get(sno - 1);
                System.out.println("Removing "+s+" value from list as cascade said there is a problem with that product");
                linesTobeDeleted.add(s);
            }
            lines.removeAll(linesTobeDeleted);

            FileUtils.writeLines(overridenMappingFile, lines, false);


            driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[2]/form[1]/div[6]/div[1]/div[1]/div[1]/button[1]")).click();



        }catch (Exception e){
            e.printStackTrace();
            System.out.println("There are no errors, so we are ok");
        }
        try{
            //click on ok button after the import
            driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[2]/form[1]/div[9]/div[1]/div[1]/div[2]/div[2]/button[1]")).click();
        }catch (Exception e){
            System.out.println("!!!There is no ok button!!");
        }





        //cascade order pad click
        driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[1]/header[1]/div[2]/nav[1]/div[2]/ul[1]/li[3]/a[1]")).click();


        /*//placing the order
        try{
            driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[2]/form[1]/div[1]/div[2]/div[2]/div[1]/div[1]/input[2]")).click();
            Thread.sleep(1000);
            driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[2]/form[1]/div[1]/div[2]/div[2]/div[1]/div[1]/input[2]")).click();
            Thread.sleep(1000);
        }catch (Exception e){

        }*/




        try{
            //logoff
            driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[1]/header[1]/div[1]/section[1]/form[1]/a[1]")).click();
        }catch (Exception exception){

        }

        System.out.println();

        driver.quit();

    }
}
