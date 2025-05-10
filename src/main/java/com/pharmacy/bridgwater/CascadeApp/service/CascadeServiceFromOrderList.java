package com.pharmacy.bridgwater.CascadeApp.service;

import com.pharmacy.bridgwater.CascadeApp.model.ActualSupplierData;
import com.pharmacy.bridgwater.CascadeApp.model.OrderListKey;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class CascadeServiceFromOrderList {

    public static final String ORIGINAL_FILE_NAME = "/Users/juppala/MyNewWorkspace/CascadeApplication/src/main/resources/Supp codes.xlsx";
    //public static final String COPIED_FILE_NAME = "/Users/juppala/MyNewWorkspace/CascadeApplication/src/main/resources/Supp codes_copy.xlsx";
    public static final String CASCADE_UPLOAD_FILE_NAME = "upload.csv";
    public static final String CASCADE_UPLOAD_FILE_NAME_WITH_ORDER_LIST_SNO = "mapping.txt";

    public static final int ORDER_LIST_DESC = 0;
    public static final int ORDER_LIST_PIP = 1;
    public static final int ORDER_LIST_QTY = 4;



    public static void main(String[] args) throws IOException, InterruptedException {
        CascadeServiceFromOrderList cascadeApp = new CascadeServiceFromOrderList();
        Map<OrderListKey, Set<ActualSupplierData>> cascadeResults = cascadeApp.getCascadeResult();
    }


    public Map<OrderListKey, Set<ActualSupplierData>> getCascadeResult() throws IOException, InterruptedException {



        FileInputStream file = new FileInputStream(ORIGINAL_FILE_NAME);
        Workbook workbook = new XSSFWorkbook(file);
        Sheet sheet = workbook.getSheetAt(0);

        List<Integer> orderListSnoMapping = new LinkedList<>();
        List<String> orderListPipCodesMapping = new LinkedList<>();
        List<String> orderListDescMapping = new LinkedList<>();

        Writer cascadeUploadFile = new FileWriter(CASCADE_UPLOAD_FILE_NAME);
        Writer mappingFile = new FileWriter(CASCADE_UPLOAD_FILE_NAME_WITH_ORDER_LIST_SNO);

        try{
            System.out.println("lastRowNumber::"+sheet.getLastRowNum());

            for (int i = 1; i <= sheet.getLastRowNum() && sheet.getRow(i) != null  ; i++) {
                if (sheet.getRow(i).getCell(ORDER_LIST_DESC) != null
                        && sheet.getRow(i).getCell(ORDER_LIST_PIP) != null
                        && sheet.getRow(i).getCell(ORDER_LIST_QTY)!=null

                        && sheet.getRow(i).getCell(ORDER_LIST_DESC).getCellType() != CellType.BLANK
                        && !sheet.getRow(i).getCell(ORDER_LIST_DESC).toString().trim().equals("")
                        &&sheet.getRow(i).getCell(ORDER_LIST_PIP).getCellType() != CellType.BLANK
                        && !sheet.getRow(i).getCell(ORDER_LIST_PIP).toString().trim().equals("")
                        && sheet.getRow(i).getCell(ORDER_LIST_QTY).getCellType() != CellType.BLANK
                        && !sheet.getRow(i).getCell(ORDER_LIST_QTY).toString().trim().equals("")

                ) {

                    String content = Double.valueOf(String.valueOf(sheet.getRow(i).getCell(ORDER_LIST_PIP))).intValue()+","+Double.valueOf(String.valueOf(sheet.getRow(i).getCell(ORDER_LIST_QTY))).intValue() + "\r\n";
                    String mappingFileContent = i+","+sheet.getRow(i).getCell(ORDER_LIST_DESC)+","+Double.valueOf(String.valueOf(sheet.getRow(i).getCell(ORDER_LIST_PIP))).intValue()+","+Double.valueOf(String.valueOf(sheet.getRow(i).getCell(ORDER_LIST_QTY))).intValue() + "\r\n";

                    cascadeUploadFile.write(content);
                    mappingFile.write(mappingFileContent);

                }

            }



        }catch (Exception e){
            System.out.println("Exception occured while writing to the cascade upload file and its mapping file");
        }finally {
            cascadeUploadFile.flush();
            cascadeUploadFile.close();
            mappingFile.flush();
            mappingFile.close();
            file.close();
        }

        //iterating over th mapping to enrich the data

        try (Stream<String> stream = Files.lines(Paths.get(CASCADE_UPLOAD_FILE_NAME_WITH_ORDER_LIST_SNO))) {
            stream.forEach( v ->{
                orderListSnoMapping.add(Integer.valueOf(v.split(",")[0]));
                orderListDescMapping.add(v.split(",")[1]);
                orderListPipCodesMapping.add(v.split(",")[2]);
            });
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
                .sendKeys("/Users/juppala/MyNewWorkspace/CascadeApplication/"+CASCADE_UPLOAD_FILE_NAME);

        //click on import button
        driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[2]/form[1]/div[3]/div[1]/div[2]/div[1]/div[3]/div[2]/button[1]"))
                .click();

        Thread.sleep(10000);
        //click on ok button after the import
        driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[2]/form[1]/div[9]/div[1]/div[1]/div[2]/div[2]/button[1]")).click();

        //cascade order pad click
        driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[1]/header[1]/div[2]/nav[1]/div[2]/ul[1]/li[3]/a[1]")).click();

        //click on view to get the orderId from alternate product
        String orderId = null;
        if (driver instanceof JavascriptExecutor) {
            try{
                JavascriptExecutor js = (JavascriptExecutor)driver;
                WebElement element = driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[2]/form[1]/div[10]/div[2]/div[1]/div[1]/div[1]/div[1]/table[1]/tbody[1]/tr[1]/td[10]/a[2]"));
                js.executeScript("arguments[0].click();", element);
                Thread.sleep(2000);

                String href = driver.findElement(By.xpath("/html/body/div/div[2]/form/div[1]/div[2]/div/div/div[2]/div[1]/div/a[1]")).getAttribute("href");
                //https://victoria-os.com/UI/VicOrdering/AlternativeProduct?orderId=11138&lineNo=1
                Pattern p = Pattern.compile("orderId=(.*?)&lineNo");
                Matcher m = p.matcher(href);


                if (m.find()) {
                    orderId = m.group(1);
                } else {
                    System.out.println("Could not derive orderId from the alternate button");
                }
                // close button click
                driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[2]/form[1]/div[3]/div[2]/div[1]/div[2]/div[1]/a[1]")).sendKeys(Keys.RETURN);

            }catch (Exception e){
                System.out.println("Please check why this has failed!!!!!!!");
                e.printStackTrace();
            }

        }



        String descOfNumberOfItemsDisplayedInBanner = driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[2]/form[1]/div[10]/div[2]/div[1]/div[1]/div[1]/div[1]/div[1]")).getText();
        Pattern pattern = Pattern.compile("of\\s+(\\d+)");
        Matcher matcher = pattern.matcher(descOfNumberOfItemsDisplayedInBanner);
        int totalNoOfProducts = 0;

        if (matcher.find()) {
            String number = matcher.group(1);
            totalNoOfProducts = Integer.valueOf(number);
        } else {
            System.out.println("Could not find Items Filtered: 0 of 57");
        }


        Map<String, Set<ActualSupplierData>> cascadeProductList = new LinkedHashMap<>();
        int noOfProducts  = totalNoOfProducts;
        for(int actualValue=1; actualValue<=totalNoOfProducts; actualValue++){
            System.out.println("!!!! Sno: " + actualValue+ " Still remaining products: " + --noOfProducts);
            driver.get("https://victoria-os.com/UI/VicOrdering/Expanded?orderId="+orderId+"&lineNo="+actualValue);
            Thread.sleep(1000);

            try{
                List<WebElement> suppliersList = driver.findElements(By.xpath("/html[1]/body[1]/div[1]/div[2]/form[1]/div[2]/div[2]/div[1]/div[1]/div[1]/div[1]/table[1]/tbody[1]/tr"));
                System.out.println("size of internal table :::"+suppliersList.size());

                String quantityFromWebsite =  driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[2]/form[1]/div[1]/div[2]/div[1]/div[1]/div[1]/div[1]/div[2]")).getText().replaceAll("Quantity:","");
                String tariffFromWebsite = driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[2]/form[1]/div[1]/div[2]/div[1]/div[1]/div[1]/div[2]/div[1]")).getText().replaceAll("Tariff:","");
                String tariffAfterDeductionFromWebsite = driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[2]/form[1]/div[1]/div[2]/div[1]/div[1]/div[1]/div[2]/div[2]")).getText().replaceAll("Tariff After Deduction:","");
                String concessionPriceFromWebsite = driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[2]/form[1]/div[1]/div[2]/div[1]/div[1]/div[1]/div[2]/div[3]")).getText().replaceAll("Concession Price:","");
                System.out.println("!!!!!! QuantityFromWebsite:"+quantityFromWebsite+ "; tariff:"+tariffFromWebsite + "; tariffAfterDeduction:"+tariffAfterDeductionFromWebsite+"; concessionPrice:"+concessionPriceFromWebsite+";");

                Set<ActualSupplierData> cascadeSupplierList = new LinkedHashSet<>();
                String descriptionFromWebsite = null;
                for(int j=1; j<=suppliersList.size();j++){
                    descriptionFromWebsite = driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[2]/form[1]/div[2]/div[2]/div[1]/div[1]/div[1]/div[1]/table[1]/tbody[1]/tr["+j+"]/td[2]")).getText();
                    String supplier =driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[2]/form[1]/div[2]/div[2]/div[1]/div[1]/div[1]/div[1]/table[1]/tbody[1]/tr["+j+"]/td[3]")).getText();
                    String price =driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[2]/form[1]/div[2]/div[2]/div[1]/div[1]/div[1]/div[1]/table[1]/tbody[1]/tr["+j+"]/td[4]")).getText();
                    Double priceInDouble = price!=null?Double.valueOf(price):null;
                    String code =driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[2]/form[1]/div[2]/div[2]/div[1]/div[1]/div[1]/div[1]/table[1]/tbody[1]/tr["+j+"]/td[7]")).getText();
                    String availability = "Not Available";
                    try{
                        availability = driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[2]/form[1]/div[2]/div[2]/div[1]/div[1]/div[1]/div[1]/table[1]/tbody[1]/tr["+j+"]/td[10]/i[1]")).getAttribute("style");
                        if("color: green;".equalsIgnoreCase(availability)){
                            availability = "Available";
                        }else if("color: red;".equalsIgnoreCase(availability)){
                            availability = "Not Available";
                        }else{
                            availability = "please check if there is any change from website on css";
                        }
                    }catch (Exception e){
                        //e.printStackTrace();
                        System.out.println("please check what is the status from website I value is "+ actualValue + "; J value is "+j);
                    }

                    if(!StringUtils.isEmpty(code)){
                        cascadeSupplierList.add(ActualSupplierData.builder()
                                .supplier(supplier).cascadePrice(priceInDouble).code(code).cascadeStatus(availability)
                                .description(descriptionFromWebsite)
                                .quantity(!StringUtils.isEmpty(quantityFromWebsite)?Integer.valueOf(quantityFromWebsite.trim()):null)
                                .tariff(!StringUtils.isEmpty(tariffFromWebsite)?Double.valueOf(tariffFromWebsite.trim()):null)
                                .tariffAfterDeduction(!StringUtils.isEmpty(tariffAfterDeductionFromWebsite)?Double.valueOf( tariffAfterDeductionFromWebsite.trim()):null)
                                .concession(!StringUtils.isEmpty(concessionPriceFromWebsite)?Double.valueOf(tariffFromWebsite.trim()):null)
                                .orderListPip(orderListPipCodesMapping.get(actualValue-1))
                                .build());
                    }

                }
                if(descriptionFromWebsite!=null){
                    cascadeProductList.put(descriptionFromWebsite, cascadeSupplierList);
                }


            }catch (Exception e){
                e.printStackTrace();
            }
        }

        try{
            //logoff
            driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[1]/header[1]/div[1]/section[1]/form[1]/a[1]")).click();
        }catch (Exception exception){

        }

        System.out.println();

        driver.close();




        Map<OrderListKey, Set<ActualSupplierData>> orderListDataPrepared = new LinkedHashMap<>();
        int mappingSheetRownumber = 0;
        for (Map.Entry<String, Set<ActualSupplierData>> entry : cascadeProductList.entrySet()) {
            Integer rowNumberOrderList = orderListSnoMapping.get(mappingSheetRownumber);
            String descOrderList = orderListDescMapping.get(mappingSheetRownumber);
            String pipCodeOrderList = orderListPipCodesMapping.get(mappingSheetRownumber);
            mappingSheetRownumber++;

            String description = entry.getKey();
            System.out.println("OrderList row number: "+rowNumberOrderList +"; Description from cascade: "+description);
            orderListDataPrepared.put(OrderListKey.builder()
                            .sno(rowNumberOrderList).orderListDesc(descOrderList).orderListPipCode(pipCodeOrderList)
                    .build(), entry.getValue());
        }

        return orderListDataPrepared;

    }
}
