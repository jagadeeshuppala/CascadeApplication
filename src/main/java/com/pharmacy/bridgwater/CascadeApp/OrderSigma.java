package com.pharmacy.bridgwater.CascadeApp;

import com.pharmacy.bridgwater.CascadeApp.model.ActualSupplierData;
import com.pharmacy.bridgwater.CascadeApp.model.OrderListKey;
import com.pharmacy.bridgwater.CascadeApp.model.SigmaOrderData;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import java.io.*;
import java.util.*;

import static com.pharmacy.bridgwater.CascadeApp.CascadeResultsWithSigmaApp.*;
import static com.pharmacy.bridgwater.CascadeApp.service.SigmaProcessService.stockAvailability;

public class OrderSigma {

    public static void main(String[] args) throws InterruptedException, IOException {
        OrderSigma orderSigma = new OrderSigma();
        //orderSigma.placeOrder(Arrays.asList(SigmaOrderData.builder().pip("1105865").quantity(5).build(), SigmaOrderData.builder().pip("5012448").quantity(2).build(), SigmaOrderData.builder().pip("1087105").quantity(2).build()));
        List<SigmaOrderData> sigmaOrderDataList = orderSigma.getOrderListBasket();
        orderSigma.placeOrder(sigmaOrderDataList);
        //System.out.println(sigmaOrderDataList);

    }

    public List<SigmaOrderData> getOrderListBasket() throws IOException, InterruptedException {
        FileInputStream file = new FileInputStream(WORK_TO_BE_DONE_FILE_NAME);
        Workbook workbook = new XSSFWorkbook(file);
        Sheet sheet = workbook.getSheetAt(0);

        List<SigmaOrderData> sigmaOrderPipCodes = new LinkedList<>();


        try {
            System.out.println("lastRowNumber::" + sheet.getLastRowNum());

            for (int i = 1; i <= sheet.getLastRowNum() && sheet.getRow(i) != null; i++) {
                if (sheet.getRow(i).getCell(ORDER_LIST_DESC) != null
                        && sheet.getRow(i).getCell(ORDER_LIST_PIP) != null
                        && sheet.getRow(i).getCell(ORDER_LIST_QTY) != null
                        && sheet.getRow(i).getCell(ORDER_LIST_FROM) != null
                        && sheet.getRow(i).getCell(SIGMA_PIP_CELL) != null

                        && sheet.getRow(i).getCell(ORDER_LIST_DESC).getCellType() != CellType.BLANK
                        && !sheet.getRow(i).getCell(ORDER_LIST_DESC).toString().trim().equals("")
                        && sheet.getRow(i).getCell(ORDER_LIST_PIP).getCellType() != CellType.BLANK
                        && !sheet.getRow(i).getCell(ORDER_LIST_PIP).toString().trim().equals("")
                        && sheet.getRow(i).getCell(ORDER_LIST_QTY).getCellType() != CellType.BLANK
                        && !sheet.getRow(i).getCell(ORDER_LIST_QTY).toString().trim().equals("")
                        && sheet.getRow(i).getCell(ORDER_LIST_FROM).getCellType() != CellType.BLANK
                        && sheet.getRow(i).getCell(ORDER_LIST_FROM).toString().trim().equalsIgnoreCase(SIGMA_ORDERING_TEXT_IN_NOTES)
                        && sheet.getRow(i).getCell(SIGMA_PIP_CELL).getCellType() != CellType.BLANK
                        && !sheet.getRow(i).getCell(SIGMA_PIP_CELL).toString().trim().equals("")
                ) {
                    try {
                        // xpath for add the order = "/html[1]/body[1]/article[1]/div[1]/div[3]/div[1]/dl[1]/div[1]/dd[1]/a[1]"
                        String pipToAddToBasket = sheet.getRow(i).getCell(SIGMA_PIP_CELL).toString();
                        Integer quantity = (int)Double.parseDouble(sheet.getRow(i).getCell(ORDER_LIST_QTY).toString());
                        sigmaOrderPipCodes.add(SigmaOrderData.builder().pip(pipToAddToBasket).quantity(quantity).build());

                    } catch (Exception e) {
                        System.out.println("value of i =" + i + "; and the exception is " + e.getMessage());

                    }

                }

            }


        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception occured while writing to the cascade upload file and its mapping file");
        } finally {
            file.close();
        }

        return sigmaOrderPipCodes;
    }

    public void placeOrder(List<SigmaOrderData> sigmaOrderPipCodes) throws InterruptedException {

        // iterate through the list and try to add
        if(!sigmaOrderPipCodes.isEmpty()){
            Map<OrderListKey, Set<ActualSupplierData>> sigmaOnlyProcessedData = new LinkedHashMap<>();
            WebDriverManager.chromedriver().setup();;
            WebDriver driver = new ChromeDriver();
            driver.get("https://www.sigconnect.co.uk/login");

            driver.findElement(By.id("loginform-username")).sendKeys("bridgwater.pharmacy@nhs.net");
            driver.findElement(By.id("loginform-password")).sendKeys("Bridg@8486");
            driver.findElement(By.id("login_btn"))
                    .sendKeys(Keys.RETURN);

            Thread.sleep(1000);

            for(SigmaOrderData sigmaOrderData : sigmaOrderPipCodes){
                Thread.sleep(1000);
                System.out.println("!!!!!Sigma!!!!! Searching for pip code "+sigmaOrderData.getPip()+":");

                try{
                    WebElement pipTextBox = driver.findElement(By.xpath("/html/body/article/div/div[1]/form/div[1]/div[2]/input"));
                    pipTextBox.clear();
                    pipTextBox.sendKeys(sigmaOrderData.getPip());
                    pipTextBox.sendKeys("\n\n");
                    Thread.sleep(5000);

                    String stockClass = driver.findElement(By.xpath("/html/body/article/div/div[3]/div/dl/div/dt")).getAttribute("class");
                    if(!stockClass.equalsIgnoreCase("ng-binding special")){
                        String description = driver.findElement(By.xpath("/html/body/article/div/div[3]/div/dl/div/dt")).getText();
                        String price = driver.findElement(By.xpath("/html/body/article/div/div[3]/div/dl/div/dd/span[3]")).getText();
                        //adding the quantity
                        driver.findElement(By.xpath("/html[1]/body[1]/article[1]/div[1]/div[3]/div[1]/dl[1]/div[1]/dd[1]/input[1]"))
                                .sendKeys(String.valueOf(sigmaOrderData.getQuantity()));
                        String availability = stockAvailability(stockClass);
                        if("Available".equalsIgnoreCase(availability)){
                            driver.findElement(By.xpath("/html[1]/body[1]/article[1]/div[1]/div[3]/div[1]/dl[1]/div[1]/dd[1]/a[1]")).click();
                            System.out.println("Adding "+description +" to the order list");
                        }else{
                            System.out.println("Stock is not available for "+description+", so not adding to the order");
                        }
                    }
                }catch (Exception e){
                    System.out.println("!!!!Sigma!!!! No results for the pip "+ sigmaOrderData.getPip());
                }
            }

            // clicking on logoff
            try {
                driver.findElement(By.xpath("/html[1]/body[1]/nav[1]/ul[1]/li[2]/a[1]")).click();
            }catch (Exception e){
                System.out.println("!!!Sigma!!! Failed to logoff");
            }
            driver.quit();

        }else{
            System.out.println("!!!!There is noting to order from sigma!!!!!");
        }

    }


}
