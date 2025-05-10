package com.pharmacy.bridgwater.CascadeApp.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.pharmacy.bridgwater.CascadeApp.model.ActualSupplierData;
import com.pharmacy.bridgwater.CascadeApp.model.CascadeSupplier;
import com.pharmacy.bridgwater.CascadeApp.model.OrderListKey;
import com.pharmacy.bridgwater.CascadeApp.model.TridentData;
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
import java.util.stream.Collectors;


public class TridentProcessService implements Callable<Map<OrderListKey, Set<ActualSupplierData>>> {
    private Map<OrderListKey,Set<ActualSupplierData>> cascadeDataForTrident;
    public TridentProcessService(Map<OrderListKey,Set<ActualSupplierData>> cascadeDataForTrident){
        this.cascadeDataForTrident = cascadeDataForTrident;
    }

    public static void main(String args[]) throws InterruptedException, JsonProcessingException {
        Long startTime = System.currentTimeMillis();
        Map<OrderListKey,Set<ActualSupplierData>> cascadeDataForTrident = new LinkedHashMap<>();


       /* CascadeService cascade = new CascadeService();
        Map<String, Set<ActualSupplierData>> cascadeResultsMap = cascade.getCascadeResults();*/

        /*for (Map.Entry<String, Set<ActualSupplierData>> entry : cascadeResultsMap.entrySet()) {
            String key = entry.getKey();
            Set<ActualSupplierData> tridentCascadeSet = entry.getValue().stream().filter(cs ->"Trident Pharmaceuticals".equalsIgnoreCase(cs.getSupplier())
                            && !StringUtils.isEmpty(cs.getCode())
                    )
                    .map(v -> ActualSupplierData.builder().description(key).cascadePrice(v.getCascadePrice()).cascadeStatus(v.getCascadeStatus()).code(v.getCode()).build())
                    .collect(Collectors.toSet());
            cascadeDataForTrident.put(key, tridentCascadeSet);
        }
        TridentProcessService process = new TridentProcessService(cascadeResultsMap);
        Map<String, Set<ActualSupplierData>> p = process.call();*/

        cascadeDataForTrident.put(OrderListKey.builder().orderListDesc("BD Micro-Fine Ultra hypodermic insulin needles for pre-filled / reusable pen injectors screw on 4mm/32gauge Pk: 100").build(),
                new HashSet<>(Arrays.asList(ActualSupplierData.builder().code("1027010").cascadePrice(Double.valueOf("4.36")).cascadeStatus("Available").build(),
                        ActualSupplierData.builder().code("6018477").cascadePrice(Double.valueOf("4.64")).cascadeStatus("Available").build(),
                        ActualSupplierData.builder().code("8282410").cascadePrice(Double.valueOf("4.64")).cascadeStatus("Available").build(),
                        ActualSupplierData.builder().code("6180319").cascadePrice(Double.valueOf("4.64")).cascadeStatus("Available").build(),
                        ActualSupplierData.builder().code("7942741").cascadePrice(Double.valueOf("4.64")).cascadeStatus("Available").build(),
                        ActualSupplierData.builder().code("7377179").cascadePrice(Double.valueOf("4.64")).cascadeStatus("Available").build(),
                        ActualSupplierData.builder().code("6996275").cascadePrice(Double.valueOf("4.64")).cascadeStatus("Available").build(),
                        ActualSupplierData.builder().code("8092728").cascadePrice(Double.valueOf("4.64")).cascadeStatus("Available").build(),
                        ActualSupplierData.builder().code("6996219").cascadePrice(Double.valueOf("4.64")).cascadeStatus("Available").build(),
                        ActualSupplierData.builder().code("7033210").cascadePrice(Double.valueOf("4.64")).cascadeStatus("Available").build(),
                        ActualSupplierData.builder().code("8199010").cascadePrice(Double.valueOf("4.64")).cascadeStatus("Available").build(),
                        ActualSupplierData.builder().code("6013346").cascadePrice(Double.valueOf("4.64")).cascadeStatus("Available").build()
                )
                ));

        /*cascadeDataForTrident.put("Carbimazole 5mg tablets Pk: 100",
                new HashSet<>(Arrays.asList(ActualSupplierData.builder().code("1182302").cascadePrice(Double.valueOf("2.85")).cascadeStatus("Available").build()
                )
                ));

        cascadeDataForTrident.put("Carbocisteine 250mg/5ml oral solution sugar free Pk: 300",
                new HashSet<>(Arrays.asList(ActualSupplierData.builder().code("7365323").cascadePrice(Double.valueOf("4.40")).cascadeStatus("Available").build()
                )
                ));*/
        TridentProcessService process = new TridentProcessService(cascadeDataForTrident);
        Map<OrderListKey,Set<ActualSupplierData>> processedTridentData = process.call();
        System.out.println(processedTridentData);
        Long endTime = System.currentTimeMillis();
        System.out.println("Total time taken for the whole process is "+ (endTime-startTime)/1000 +" seconds");
    }


    @Override
    public Map<OrderListKey,Set<ActualSupplierData>> call() throws InterruptedException {

        if(cascadeDataForTrident.isEmpty()){
            System.out.println("!!!!!Trident!!!!! There data is coming as empty from order pad, so nothing to check");
            return cascadeDataForTrident;
        }
        WebDriverManager.chromedriver().setup();;
        WebDriver driver = new ChromeDriver();
        driver.get("https://www.aah.co.uk/s/signin?startURL=https%3A%2F%2Fwww.tridentonline.co.uk%2Ftrident%2Fsearchresults%3Foperation%3DquickSearch");

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


        int totalNoOfRecords = cascadeDataForTrident.size();
        for(Map.Entry<OrderListKey,Set<ActualSupplierData>> entry: cascadeDataForTrident.entrySet()){
            System.out.println("!!!!!Trident!!!!! still total no of records "+ totalNoOfRecords-- +":");
            OrderListKey description = entry.getKey();
            Set<ActualSupplierData> tridentDataSet = entry.getValue();

            for(ActualSupplierData tridentData : tridentDataSet){

                System.out.println("!!!!Trident !!! Searching for pip code "+tridentData.getCode()+":");
                Thread.sleep(1000);
                String pipCode = tridentData.getCode();
                // search box
                driver.findElement(By.xpath("//html[1]/body[1]/div[1]/header[1]/div[1]/div[1]/div[1]/div[4]/div[1]/div[1]/div[1]/div[1]/span[1]/lightning-input[1]/lightning-primitive-input-simple[1]/div[1]/div[1]/input[1]")).clear();
                driver.findElement(By.xpath("/html[1]/body[1]/div[1]/header[1]/div[1]/div[1]/div[1]/div[4]/div[1]/div[1]/div[1]/div[1]/span[1]/lightning-input[1]/lightning-primitive-input-simple[1]/div[1]/div[1]/input[1]")).sendKeys(pipCode);
                driver.findElement(By.xpath("/html[1]/body[1]/div[1]/header[1]/div[1]/div[1]/div[1]/div[4]/div[1]/div[1]/div[1]/div[1]/span[1]/lightning-input[1]/lightning-primitive-input-simple[1]/div[1]/div[1]/input[1]")).sendKeys(Keys.RETURN);
                Thread.sleep(2000);
                // retrieved product list click
                try{
                /*driver.findElement(By.xpath("/html[1]/body[1]/div[1]/header[1]/div[1]/div[1]/div[1]/div[4]/div[1]/div[1]/div[1]/div[1]/span[1]/div[2]/div[2]/ul[1]/li[1]")).click();
                Thread.sleep(3000);*/
                    //String descriptionFromWebsite = driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[2]/span[1]/div[1]/div[1]/div[2]/div[3]/div[1]/div[1]/div[3]/div[2]/div[1]/h4[1]")).getText();
                    String descriptionFromWebsite = null;
                    try{
                        descriptionFromWebsite = driver.findElement(By.xpath("/html/body/div[1]/div[2]/span/div/div/div[2]/div[6]/div[2]/div[2]/span/div/div/div/div[2]/div/div[1]/span/p[1]")).getText();
                    }catch (Exception e) {
                        System.out.println("description  is failing "+pipCode);
                        try {
                            descriptionFromWebsite = driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[2]/span[1]/div[1]/div[1]/div[2]/div[6]/div[2]/div[2]/span[1]/div[1]/div[1]/div[1]/div[1]/div[1]/div[1]/span[1]/p[1]")).getText();
                        }catch (Exception ex){
                            System.out.println("description  is failing again "+pipCode);
                            try{
                                descriptionFromWebsite = driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[2]/span[1]/div[1]/div[1]/div[2]/div[6]/div[2]/div[3]/span[1]/div[1]/div[1]/div[1]/div[2]/div[1]/div[1]/span[1]/p[1]")).getText();
                            }catch (Exception exc){
                                System.out.println("description is failing again and again "+pipCode);
                            }
                        }
                    }
                    String priceFromWebsite = null;

                    try{
                        // priceFromWebsite = driver.findElement(By.xpath("//html[1]/body[1]/div[1]/div[2]/span[1]/div[1]/div[1]/div[2]/div[3]/div[1]/div[1]/div[3]/div[2]/div[1]/div[4]/div[1]/div[1]/div[1]/span[1]")).getText();
                        priceFromWebsite = driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[2]/span[1]/div[1]/div[1]/div[2]/div[6]/div[2]/div[2]/span[1]/div[1]/div[1]/div[1]/div[2]/div[1]/div[3]/span[1]/div[1]/div[1]/span[1]")).getText();
                    }catch (Exception e){
                        System.out.println("priceFromWebsite is failing "+pipCode);
                        try{
                            priceFromWebsite = driver.findElement(By.xpath("//html[1]/body[1]/div[1]/div[2]/span[1]/div[1]/div[1]/div[2]/div[3]/div[1]/div[1]/div[3]/div[2]/div[1]/div[5]/div[1]/div[1]/div[1]/span[1]")).getText();
                        }catch (Exception exception){
                            System.out.println("priceFromWebsite is failing again "+pipCode);
                            try{
                                priceFromWebsite = driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[2]/span[1]/div[1]/div[1]/div[2]/div[6]/div[2]/div[2]/span[1]/div[1]/div[1]/div[1]/div[1]/div[1]/div[3]/span[1]/div[1]/div[1]/span[1]")).getText();
                            }catch (Exception exc){
                                System.out.println("priceFromWebsite is failing again,again "+pipCode);
                                try{
                                    priceFromWebsite = driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[2]/span[1]/div[1]/div[1]/div[2]/div[6]/div[2]/div[3]/span[1]/div[1]/div[1]/div[1]/div[2]/div[1]/div[3]/span[1]/div[1]/div[1]/span[1]")).getText();
                                }catch (Exception exce){
                                    System.out.println("priceFromWebsite is failing again,again,again "+pipCode);
                                }
                            }
                        }
                    }

                    //String availablityFromWebsite = driver.findElement(By.xpath("//div[@class='avail-col-text avail-col-width']")).getText();
                    String availablityFromWebsite = null;
                    try{
                        availablityFromWebsite = driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[2]/span[1]/div[1]/div[1]/div[2]/div[6]/div[2]/div[2]/span[1]/div[1]/div[1]/div[1]/div[2]/div[1]/div[2]/div[1]/div[2]")).getText();
                    }catch (Exception e){
                        System.out.println("availablity is failing "+pipCode);
                        try{
                            availablityFromWebsite = driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[2]/span[1]/div[1]/div[1]/div[2]/div[6]/div[2]/div[3]/span[1]/div[1]/div[1]/div[1]/div[2]/div[1]/div[2]/div[1]/div[2]")).getText();
                        }catch (Exception ex){
                            System.out.println("availablity is failing again "+pipCode);
                            try{
                                availablityFromWebsite = driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[2]/span[1]/div[1]/div[1]/div[2]/div[6]/div[2]/div[2]/span[1]/div[1]/div[1]/div[1]/div[1]/div[1]/div[2]/div[1]/div[2]")).getText();
                            }catch (Exception exc){
                                System.out.println("availablity is failing again,again "+pipCode );
                                try{

                                }catch (Exception exce){
                                    System.out.println("availablity is failing again,again,again "+pipCode);
                                }
                            }

                        }

                    }
                    if(StringUtils.isEmpty(availablityFromWebsite)){
                        // if availability is not there, then remove it from the list by making pip code null
                        tridentData.setCode(null);
                    }

                    System.out.println("!!!!Trident !!!PipCode:"+pipCode+"; descFromWebsite:"+descriptionFromWebsite+ "; priceFromWebsite:"+ priceFromWebsite+ "; availability:"+availablityFromWebsite);
                    tridentData.setCascadePrice(!StringUtils.isEmpty(priceFromWebsite) ?Double.valueOf(priceFromWebsite.replace("£","")):null);
                    //tridentData.setCascadePrice(0.3);
                    tridentData.setCascadeStatus(!StringUtils.isEmpty(availablityFromWebsite)?stockAvailability(availablityFromWebsite):null);


                }catch (Exception e){
                    //e.printStackTrace();
                    System.out.println("!!!!Trident !!! Zero products might have resulted back for "+ pipCode);
                }
                Thread.sleep(1000);


            }
        }

        try{
            Actions actions = new Actions(driver);
            WebElement element = driver.findElement(By.xpath("/html[1]/body[1]/div[1]/header[1]/div[1]/div[1]/div[1]/div[5]/div[1]/div[1]/div[1]/div[3]/div[1]/div[1]/lightning-icon[1]/span[1]/lightning-primitive-icon[1]/*[name()='svg'][1]"));
            actions.moveToElement(element).click().perform();
            element = driver.findElement(By.xpath("/html[1]/body[1]/div[1]/header[1]/div[1]/div[1]/div[1]/div[5]/div[1]/div[1]/div[1]/div[3]/div[2]/div[1]/a[11]"));
            actions.moveToElement(element).click().perform();
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("!!!!Trident !!! Falied to click on the logout");
        }
        driver.close();

        return cascadeDataForTrident;


    }

    private String stockAvailability(String availability){
        switch (availability){
            case "Out of stock":
            case "Restricted":
                return "Not Available";
            case "In stock":
                return "Available";
            default:
                return null;

        }
    }




}