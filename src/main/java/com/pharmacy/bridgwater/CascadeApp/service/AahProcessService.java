package com.pharmacy.bridgwater.CascadeApp.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.pharmacy.bridgwater.CascadeApp.model.ActualSupplierData;
import com.pharmacy.bridgwater.CascadeApp.model.OrderListKey;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;

import java.util.*;
import java.util.concurrent.Callable;



public class AahProcessService implements Callable<Map<OrderListKey, Set<ActualSupplierData>>> {
    Map<OrderListKey,Set<ActualSupplierData>> cascadeDataForAah;
    public AahProcessService(Map<OrderListKey,Set<ActualSupplierData>> cascadeDataForAah){
        this.cascadeDataForAah = cascadeDataForAah;
    }

    public static void main(String args[]) throws InterruptedException, JsonProcessingException {
        Long startTime = System.currentTimeMillis();
       /* Map<String,Set<ActualSupplierData>> overriddenAahDataFromWebsite = new LinkedHashMap<>();

        CascadeService cascade = new CascadeService();
        Map<String, Set<ActualSupplierData>> cascadeResultsMap = cascade.getCascadeResults();

        AahProcessService process = new AahProcessService(cascadeResultsMap);
        Map<String, Set<ActualSupplierData>> aahOnlyResults = process.call();*/
        Map<OrderListKey,Set<ActualSupplierData>> cascadeDataForAah = new HashMap<>();
        cascadeDataForAah.put(OrderListKey.builder().orderListDesc("BD Micro-Fine Ultra hypodermic insulin needles for pre-filled / reusable pen injectors screw on 4mm/32gauge Pk: 100").build() ,
                new HashSet<>(Arrays.asList(

                        /*ActualSupplierData.builder().code("1195825").cascadePrice(Double.valueOf("4.36")).cascadeStatus("Available").build(),
                        ActualSupplierData.builder().code("1127950").cascadePrice(Double.valueOf("4.64")).cascadeStatus("Available").build(),
                        ActualSupplierData.builder().code("8282410").cascadePrice(Double.valueOf("4.64")).cascadeStatus("Available").build(),
                        ActualSupplierData.builder().code("3403953").cascadePrice(Double.valueOf("4.64")).cascadeStatus("Available").build(),
                        ActualSupplierData.builder().code("3676251").cascadePrice(Double.valueOf("4.64")).cascadeStatus("Available").build(),
                        ActualSupplierData.builder().code("6180319").cascadePrice(Double.valueOf("4.64")).cascadeStatus("Available").build(),
                        ActualSupplierData.builder().code("2605277").cascadePrice(Double.valueOf("4.64")).cascadeStatus("Available").build(),
                        ActualSupplierData.builder().code("1183961").cascadePrice(Double.valueOf("4.64")).cascadeStatus("Available").build(),
                        ActualSupplierData.builder().code("1137959").cascadePrice(Double.valueOf("4.64")).cascadeStatus("Available").build(),
                        ActualSupplierData.builder().code("1133065").cascadePrice(Double.valueOf("4.64")).cascadeStatus("Available").build(),
                        ActualSupplierData.builder().code("1183540").cascadePrice(Double.valueOf("4.64")).cascadeStatus("Available").build(),
                        ActualSupplierData.builder().code("1101252").cascadePrice(Double.valueOf("4.64")).cascadeStatus("Available").build(),
                        ActualSupplierData.builder().code("1099704").cascadePrice(Double.valueOf("4.64")).cascadeStatus("Available").build(),
                        ActualSupplierData.builder().code("1098904").cascadePrice(Double.valueOf("4.64")).cascadeStatus("Available").build(),
                        ActualSupplierData.builder().code("6745319").cascadePrice(Double.valueOf("4.64")).cascadeStatus("Available").build(),
                        ActualSupplierData.builder().code("1101245").cascadePrice(Double.valueOf("4.64")).cascadeStatus("Available").build(),
                        ActualSupplierData.builder().code("1099696").cascadePrice(Double.valueOf("4.64")).cascadeStatus("Available").build(),
                        ActualSupplierData.builder().code("6745244").cascadePrice(Double.valueOf("4.64")).cascadeStatus("Available").build(),
                        ActualSupplierData.builder().code("1173525").cascadePrice(Double.valueOf("4.64")).cascadeStatus("Available").build()*/


                        ActualSupplierData.builder().code("1099696").price(Double.valueOf("4.64")).status("Available").build()




                )
                ));
        AahProcessService process = new AahProcessService(cascadeDataForAah);
        Map<OrderListKey,Set<ActualSupplierData>> processedAahData = process.call();
        System.out.println(processedAahData);


        //1184951
        //1099720

        System.out.println();
        //Adding sigma results to the main list
        /*for (Map.Entry<String, Set<ActualSupplierData>> entry : cascadeResultsMap.entrySet()) {
            String key = entry.getKey();
            Set<ActualSupplierData> sigmaResults = sigmaOnlyResults.get(key);
            Set<ActualSupplierData> value = entry.getValue();
            //Adding sigma results
            value.addAll(sigmaResults);
            overriddenAahDataFromWebsite.put(key, value);


        }*/
        //System.out.println(cascadeDataWithSigmaDataAdded);
        Long endTime = System.currentTimeMillis();
        System.out.println("Total time taken for the whole process is "+ (endTime-startTime)/1000 +" seconds");
    }



    @Override
    public Map<OrderListKey,Set<ActualSupplierData>> call() throws InterruptedException {
        if(cascadeDataForAah.isEmpty()){
            System.out.println("!!!!!AAH!!!!! There data is coming as empty from order pad, so nothing to check");
            return cascadeDataForAah;
        }

        WebDriverManager.chromedriver().setup();;
        WebDriver driver = new ChromeDriver();
        driver.get("https://www.aah.co.uk/s/signin?startURL=https%3A%2F%2Fwww.aah.co.uk%2Faahpoint%2Fsearchresults%3Foperation%3DquickSearch");

        Thread.sleep(5000);

        driver.findElement(By.id("onetrust-reject-all-handler")).click();

        //login
        driver.findElement(By.xpath("/html[1]/body[1]/div[3]/div[1]/div[1]/div[1]/div[2]/div[1]/div[1]/div[1]/article[1]/div[2]/div[2]/div[1]/lightning-input[1]/lightning-primitive-input-simple[1]/div[1]/div[1]/input[1]")).sendKeys("bridgwaterpharmacy");
        driver.findElement(By.xpath("/html[1]/body[1]/div[3]/div[1]/div[1]/div[1]/div[2]/div[1]/div[1]/div[1]/article[1]/div[2]/div[2]/div[1]/div[1]/lightning-input[1]/lightning-primitive-input-simple[1]/div[1]/div[1]/input[1]")).sendKeys("Brid@8486");
        Thread.sleep(5000);

        //button
        driver.findElement(By.xpath("/html[1]/body[1]/div[3]/div[1]/div[1]/div[1]/div[2]/div[1]/div[1]/div[1]/article[1]/div[2]/div[2]/div[2]/button[1]"))
                .sendKeys(Keys.RETURN);
        Thread.sleep(20000);

        try{
            driver.findElement(By.xpath("//*[@id=\"CustomHeaderContainer\"]/div[3]/div[1]/div/div/footer/button[2]")).click();
        }catch (Exception e) {
            System.out.println("No customer banner");
        }
        int totalNoOfRecords = cascadeDataForAah.size();

        for(Map.Entry<OrderListKey,Set<ActualSupplierData>> entry: cascadeDataForAah.entrySet()){
            System.out.println("!!!!!AAH!!!!! still total no of records "+ totalNoOfRecords-- +":");
            OrderListKey description = entry.getKey();
            //Set<ActualSupplierData> aahDataSet = entry.getValue().stream().filter(Constants.SUPPLIER_AAH.equals(""));
            Set<ActualSupplierData> aahDataSet = entry.getValue();

            for(ActualSupplierData aahData : aahDataSet){
                try{
                    System.out.println("!!!!!AAH!!!!! Searching for pip code "+aahData.getCode()+":");
                    Thread.sleep(1000);
                    String pipCode = aahData.getCode();
                    // search box
                    driver.findElement(By.xpath("/html[1]/body[1]/div[1]/header[1]/div[1]/div[1]/div[1]/div[4]/div[1]/div[1]/div[1]/div[1]/div[1]/div[1]/span[1]/lightning-input[1]/lightning-primitive-input-simple[1]/div[1]/div[1]/input[1]")).clear();
                    driver.findElement(By.xpath("/html[1]/body[1]/div[1]/header[1]/div[1]/div[1]/div[1]/div[4]/div[1]/div[1]/div[1]/div[1]/div[1]/div[1]/span[1]/lightning-input[1]/lightning-primitive-input-simple[1]/div[1]/div[1]/input[1]")).sendKeys(pipCode);
                    Thread.sleep(1000);
                    // retrieved product list click
                    boolean success = false;
                    int retryCount = 0;
                    while(retryCount<3 && !success) {
                        try {
                            retryCount++;
                            driver.findElement(By.xpath("/html[1]/body[1]/div[1]/header[1]/div[1]/div[1]/div[1]/div[4]/div[1]/div[1]/div[1]/div[1]/div[1]/div[1]/span[1]/div[2]/div[2]/ul[1]/li[1]")).click();
                            Thread.sleep(3000);


                            String descriptionFromWebsite = driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[2]/span[1]/div[1]/div[1]/div[2]/div[3]/div[1]/div[1]/div[3]/div[2]/div[1]/h4[1]")).getText();
                            String priceFromWebsite = null;

                            try {
                                priceFromWebsite = driver.findElement(By.xpath("//html[1]/body[1]/div[1]/div[2]/span[1]/div[1]/div[1]/div[2]/div[3]/div[1]/div[1]/div[3]/div[2]/div[1]/div[4]/div[1]/div[1]/div[1]/span[1]")).getText();
                            } catch (Exception e) {
                                System.out.println("priceFromWebsite is failing "+pipCode);
                                try {
                                    priceFromWebsite = driver.findElement(By.xpath("//html[1]/body[1]/div[1]/div[2]/span[1]/div[1]/div[1]/div[2]/div[3]/div[1]/div[1]/div[3]/div[2]/div[1]/div[5]/div[1]/div[1]/div[1]/span[1]")).getText();
                                } catch (Exception exception) {
                                    System.out.println("priceFromWebsite is failing again "+pipCode);
                                }
                            }

                            //String availablity = driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[2]/span[1]/div[1]/div[1]/div[2]/div[3]/div[1]/div[1]/div[3]/div[2]/div[1]/div[2]/span[1]/div[1]/div[2]")).getText();
                            String availablityFromWebsite = driver.findElement(By.xpath("//div[@class='avail-col-text avail-col-width']")).getText();
                            System.out.println("!!!!AAH !!! PipCode:" + pipCode + "; descFromWebsite:" + descriptionFromWebsite + "; priceFromWebsite:" + priceFromWebsite + "; availability:" + availablityFromWebsite);
                            aahData.setPrice(!StringUtils.isEmpty(priceFromWebsite) ?Double.valueOf(priceFromWebsite.replace("£","")):null);
                            //aahData.setCascadePrice(0.1);
                            aahData.setStatus(!StringUtils.isEmpty(availablityFromWebsite) ? stockAvailability(availablityFromWebsite) : null);
                            aahData.setSupplier("AAH Pharmaceuticals");
                            success = true;


                        } catch (Exception e) {
                            success = false;
                            System.out.println("!!!!AAH !!! Zero products might have resulted back for "+ pipCode+" so trying again " + retryCount);
                        }
                    }
                    Thread.sleep(1000);

                }catch (Exception e){
                    System.out.println("There is a big exception in getting the pip "+ aahData.getCode());
                }


            }
        }

        try{
            Actions actions = new Actions(driver);
            WebElement element = driver.findElement(By.xpath("/html[1]/body[1]/div[1]/header[1]/div[1]/div[1]/div[1]/div[5]/div[1]/div[1]/div[1]/div[3]/div[1]/div[1]/span[5]"));
            actions.moveToElement(element).click().perform();
            element = driver.findElement(By.xpath("/html[1]/body[1]/div[1]/header[1]/div[1]/div[1]/div[1]/div[5]/div[1]/div[1]/div[1]/div[3]/div[2]/div[1]/a[11]"));
            actions.moveToElement(element).click().perform();
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("!!!!AAH !!! Failed to click on the logout");
        }

        driver.quit();;

        return cascadeDataForAah;


    }

    private String stockAvailability(String availability){
        switch (availability){
            case "Out of stock":
                return "Not Available";
            case "In stock":
            case "Third party":
                return "Available";
            default:
                return null;

        }
    }


}