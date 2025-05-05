package com.pharmacy.bridgwater.CascadeApp.service;


import com.pharmacy.bridgwater.CascadeApp.model.ActualSupplierData;
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



public class AahProcessService implements Callable<Map<String, Set<ActualSupplierData>>> {
    Map<String,Set<ActualSupplierData>> cascadeDataForAah;
    public AahProcessService(Map<String,Set<ActualSupplierData>> cascadeDataForAah){
        this.cascadeDataForAah = cascadeDataForAah;
    }

    /*public static void main(String args[]) throws InterruptedException, JsonProcessingException {
        Long startTime = System.currentTimeMillis();
        Map<String,Set<ActualSupplierData>> cascadeDataForAah = new LinkedHashMap<>();

        AahProcessService process = new AahProcessService();
        CascadeServiceNew cascade = new CascadeServiceNew();
        Map<String, Set<ActualSupplierData>> cascadeResultsMap = cascade.getCascadeResults();
        *//*for (Map.Entry<String, List<ActualSupplierData>> entry : cascadeResultsMap.entrySet()) {
            String key = entry.getKey();
            Set<ActualSupplierData> aahCascadeSet = entry.getValue().stream().filter(cs ->"AAH Pharmaceuticals".equalsIgnoreCase(cs.getSupplier())
                            && !StringUtils.isEmpty(cs.getCode())
                    )
                    .map(v -> ActualSupplierData.builder().description(key).cascadePrice(v.getCascadePrice()).cascadeStatus(v.getCascadeStatus()).code(v.getCode()).build())
                    .collect(Collectors.toSet());
            cascadeDataForAah.put(key, aahCascadeSet);
        }*//*

        *//*cascadeDataForAah.put("BD Micro-Fine Ultra hypodermic insulin needles for pre-filled / reusable pen injectors screw on 4mm/32gauge Pk: 100",
                new HashSet<>(Arrays.asList(ActualSupplierData.builder().code("7943103").cascadePrice(Double.valueOf("4.36")).cascadeStatus("Available").build(),
                        ActualSupplierData.builder().code("6177560").cascadePrice(Double.valueOf("4.64")).cascadeStatus("Available").build()
                )
                ));

        cascadeDataForAah.put("Carbimazole 5mg tablets Pk: 100",
                new HashSet<>(Arrays.asList(ActualSupplierData.builder().code("1182302").cascadePrice(Double.valueOf("2.85")).cascadeStatus("Available").build()
                )
                ));

        cascadeDataForAah.put("Carbocisteine 250mg/5ml oral solution sugar free Pk: 300",
                new HashSet<>(Arrays.asList(ActualSupplierData.builder().code("1266535").cascadePrice(Double.valueOf("4.40")).cascadeStatus("Available").build()
                )
                ));*//*

        Map<String,Set<ActualSupplierData>> processedSigmaData = process.processAah(cascadeResultsMap);
        System.out.println(processedSigmaData);
        Long endTime = System.currentTimeMillis();
        System.out.println("Total time taken for the whole process is "+ (endTime-startTime)/1000 +" seconds");
    }*/



    @Override
    public Map<String,Set<ActualSupplierData>> call() throws InterruptedException {



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

        for(Map.Entry<String,Set<ActualSupplierData>> entry: cascadeDataForAah.entrySet()){
            String description = entry.getKey();
            Set<ActualSupplierData> aahDataSet = entry.getValue();

            for(ActualSupplierData aahData : aahDataSet){
                if("AAH Pharmaceuticals".equalsIgnoreCase(aahData.getSupplier()) && !StringUtils.isEmpty(aahData.getCode())){
                    System.out.println("!!!!!AAH!!!!! Searching for pip code "+aahData.getCode()+":");
                    Thread.sleep(1000);
                    String pipCode = aahData.getCode();
                    // search box
                    driver.findElement(By.xpath("/html[1]/body[1]/div[1]/header[1]/div[1]/div[1]/div[1]/div[4]/div[1]/div[1]/div[1]/div[1]/div[1]/div[1]/span[1]/lightning-input[1]/lightning-primitive-input-simple[1]/div[1]/div[1]/input[1]")).clear();
                    driver.findElement(By.xpath("/html[1]/body[1]/div[1]/header[1]/div[1]/div[1]/div[1]/div[4]/div[1]/div[1]/div[1]/div[1]/div[1]/div[1]/span[1]/lightning-input[1]/lightning-primitive-input-simple[1]/div[1]/div[1]/input[1]")).sendKeys(pipCode);
                    Thread.sleep(1000);
                    // retrieved product list click
                    try{
                        driver.findElement(By.xpath("/html[1]/body[1]/div[1]/header[1]/div[1]/div[1]/div[1]/div[4]/div[1]/div[1]/div[1]/div[1]/div[1]/div[1]/span[1]/div[2]/div[2]/ul[1]/li[1]")).click();
                        Thread.sleep(3000);
                        String descriptionFromWebsite = driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[2]/span[1]/div[1]/div[1]/div[2]/div[3]/div[1]/div[1]/div[3]/div[2]/div[1]/h4[1]")).getText();
                        String priceFromWebsite = null;

                        try{
                            priceFromWebsite = driver.findElement(By.xpath("//html[1]/body[1]/div[1]/div[2]/span[1]/div[1]/div[1]/div[2]/div[3]/div[1]/div[1]/div[3]/div[2]/div[1]/div[4]/div[1]/div[1]/div[1]/span[1]")).getText();
                        }catch (Exception e){
                            System.out.println("priceFromWebsite is failing at div 4");
                            try{
                                priceFromWebsite = driver.findElement(By.xpath("//html[1]/body[1]/div[1]/div[2]/span[1]/div[1]/div[1]/div[2]/div[3]/div[1]/div[1]/div[3]/div[2]/div[1]/div[5]/div[1]/div[1]/div[1]/span[1]")).getText();
                            }catch (Exception exception){
                                System.out.println("priceFromWebsite is failing at div 5");
                            }
                        }

                        //String availablity = driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[2]/span[1]/div[1]/div[1]/div[2]/div[3]/div[1]/div[1]/div[3]/div[2]/div[1]/div[2]/span[1]/div[1]/div[2]")).getText();
                        String availablityFromWebsite = driver.findElement(By.xpath("//div[@class='avail-col-text avail-col-width']")).getText();
                        System.out.println("!!!!AAH !!! PipCode:"+pipCode+"; descFromWebsite:"+descriptionFromWebsite+ "; priceFromWebsite:"+ priceFromWebsite+ "; availability:"+availablityFromWebsite);
                        aahData.setSupplierPrice(!StringUtils.isEmpty(priceFromWebsite) ?Double.valueOf(priceFromWebsite.replace("£","")):null);
                        aahData.setDefinitePrice(!StringUtils.isEmpty(priceFromWebsite) ?Double.valueOf(priceFromWebsite.replace("£","")):null);
                        aahData.setSupplierStatus(!StringUtils.isEmpty(availablityFromWebsite)?stockAvailability(availablityFromWebsite):null);
                        aahData.setDefiniteStatus(!StringUtils.isEmpty(availablityFromWebsite)?stockAvailability(availablityFromWebsite):null);
                        aahData.setSupplier("AAH Pharmaceuticals");

                    }catch (Exception e){
                        System.out.println("!!!!AAH !!! Zero products might have resulted back for "+ pipCode);
                    }
                    Thread.sleep(1000);
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
            System.out.println("!!!!AAH !!! Falied to click on the logout");
        }


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