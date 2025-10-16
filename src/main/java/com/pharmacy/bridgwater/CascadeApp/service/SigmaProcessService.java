package com.pharmacy.bridgwater.CascadeApp.service;


import com.pharmacy.bridgwater.CascadeApp.model.ActualSupplierData;
import com.pharmacy.bridgwater.CascadeApp.model.OrderListKey;
import io.github.bonigarcia.wdm.WebDriverManager;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.*;
import java.util.concurrent.Callable;


public class SigmaProcessService implements Callable<Map<OrderListKey, Set<ActualSupplierData>>> {
    Map<OrderListKey,Set<String>> cascadeDataForSigma;
    public SigmaProcessService(Map<OrderListKey,Set<String>> cascadeDataForSigma){
        this.cascadeDataForSigma = cascadeDataForSigma;
    }

   /* public static void main(String args[]) throws InterruptedException, JsonProcessingException, JsonProcessingException {
        Long startTime = System.currentTimeMillis();
        Map<String,Set<ActualSupplierData>> cascadeDataWithSigmaDataAdded = new LinkedHashMap<>();

        CascadeService cascade = new CascadeService();
        Map<String, Set<ActualSupplierData>> cascadeResultsMap = cascade.getCascadeResults();

        SigmaProcessService process = new SigmaProcessService(cascadeResultsMap);
        Map<String, Set<ActualSupplierData>> sigmaOnlyResults = process.call();

        //Adding sigma results to the main list
        for (Map.Entry<String, Set<ActualSupplierData>> entry : cascadeResultsMap.entrySet()) {
            String key = entry.getKey();
            Set<ActualSupplierData> sigmaResults = sigmaOnlyResults.get(key);
            Set<ActualSupplierData> value = entry.getValue();
            //Adding sigma results
            value.addAll(sigmaResults);
            cascadeDataWithSigmaDataAdded.put(key, value);


        }
        //System.out.println(cascadeDataWithSigmaDataAdded);
        Long endTime = System.currentTimeMillis();
        System.out.println("Total time taken for the whole process is "+ (endTime-startTime)/1000 +" seconds");
    }*/


    @Override
    public Map<OrderListKey,Set<ActualSupplierData>> call() throws InterruptedException {

        Map<OrderListKey,Set<ActualSupplierData>> sigmaOnlyProcessedData = new LinkedHashMap<>();

        WebDriverManager.chromedriver().setup();;
        //setup chromeoptions
        ChromeOptions options = new ChromeOptions();

        options.addArguments("window-size=1920,1080");

        options.addArguments("disable-blink-features=AutomationControlled") ;
        options.setExperimentalOption("excludeSwitches", Arrays.asList("enable-automation"));
        options.setExperimentalOption("useAutomationExtension", false);

        WebDriver driver = new ChromeDriver(options);

        driver.get("https://www.sigconnect.co.uk/login");

        driver.findElement(By.id("loginform-username")).sendKeys("bridgwater.pharmacy@nhs.net");
        driver.findElement(By.id("loginform-password")).sendKeys("Bridg@8486");
        driver.findElement(By.id("login_btn"))
                .sendKeys(Keys.RETURN);

        Thread.sleep(1000);

        int totalNoOfRecords = cascadeDataForSigma.size();

        for (Map.Entry<OrderListKey, Set<String>> entry : cascadeDataForSigma.entrySet()) {
            System.out.println("!!!!!Sigma!!!!! still total no of records "+ totalNoOfRecords-- +":");
            Set<String> existingData  = entry.getValue();

            Set<ActualSupplierData> sigmaOnlyData = new LinkedHashSet<>();
            for(String pip : existingData){
                Thread.sleep(1000);
                System.out.println("!!!!!Sigma!!!!! Searching for pip code "+pip+":");


                try{
                    WebElement pipTextBox = driver.findElement(By.xpath("/html/body/article/div/div[1]/form/div[1]/div[2]/input"));
                    pipTextBox.clear();
                    pipTextBox.sendKeys(pip);
                    pipTextBox.sendKeys("\n\n");
                    Thread.sleep(5000);

                    String stockClass = driver.findElement(By.xpath("/html/body/article/div/div[3]/div/dl/div/dt")).getAttribute("class");
                    if(!stockClass.equalsIgnoreCase("ng-binding special")){
                        String description = driver.findElement(By.xpath("/html/body/article/div/div[3]/div/dl/div/dt")).getText();
                        String price = driver.findElement(By.xpath("/html/body/article/div/div[3]/div/dl/div/dd/span[3]")).getText();
                        String availability = stockAvailability(stockClass);
                        System.out.println("!!!! Sigma !!!! description:"+description+"; pip:"+pip+"; availability:"+availability + "; price"+price);
                        ActualSupplierData s = ActualSupplierData.builder().description(description)
                                .price(!StringUtils.isEmpty(price)? Double.valueOf(price.replaceAll("£","")):null)
                                //.cascadePrice(0.2)
                                .supplier("Sigma")
                                //.definitePrice(!StringUtils.isEmpty(price)? Double.valueOf(price.replaceAll("£","")):null)
                                .status(availability).code(pip)
                               // .definiteStatus(availability)
                                //.tariff(anyOfCascadeData.getTariff())
                                //.tariffAfterDeduction(anyOfCascadeData.getTariffAfterDeduction())
                                //.concession(anyOfCascadeData.getConcession())
                                //.quantity(anyOfCascadeData.getQuantity())
                                .build();
                        sigmaOnlyData.add(s);
                        // breaking as i am not going to search for other products
                        break;
                    }
                }catch (Exception e){
                    System.out.println("!!!!Sigma!!!! No results for the pip "+ pip);
                }
            }
            sigmaOnlyProcessedData.put(entry.getKey(), sigmaOnlyData);
        }
        try {
            driver.findElement(By.xpath("/html[1]/body[1]/nav[1]/ul[1]/li[2]/a[1]")).click();
        }catch (Exception e){
            System.out.println("!!!Sigma!!! Failed to logoff");
        }
        driver.quit();
        return sigmaOnlyProcessedData;

    }

    public static String stockAvailability(String stockAvailablityClass){
        switch (stockAvailablityClass){
            case "ng-binding no_stock":
                return "Not Available";
            case "ng-binding":
                return "Available";
            case "ng-binding low_stock":
                //return "Low Stock";
                return "Available";
            default:
                return null;

        }
    }




}