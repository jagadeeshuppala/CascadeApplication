package com.pharmacy.bridgwater.CascadeApp.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.pharmacy.bridgwater.CascadeApp.model.ActualSupplierData;
import com.pharmacy.bridgwater.CascadeApp.model.BnsData;
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


public class BnsProcessService {

    public static void main(String args[]) throws InterruptedException, JsonProcessingException {
        Long startTime = System.currentTimeMillis();
        Map<String,Set<ActualSupplierData>> cascadeDataForTrident = new LinkedHashMap<>();

        BnsProcessService process = new BnsProcessService();
        /*CascadeService cascade = new CascadeService();
        Map<String, List<CascadeSupplier>> cascadeResultsMap = cascade.getCascadeResults();
        for (Map.Entry<String, List<CascadeSupplier>> entry : cascadeResultsMap.entrySet()) {
            String key = entry.getKey();
            Set<BnsData> bnsCascadeSet = entry.getValue().stream().filter(cs ->"B&S Colorama".equalsIgnoreCase(cs.getSupplier())
                            && !StringUtils.isEmpty(cs.getCode())
                    )
                    .map(v -> BnsData.builder().description(key).cascadePrice(v.getPrice()).cascadeStatus(v.getStatus()).code(v.getCode()).build())
                    .collect(Collectors.toSet());
            cascadeDataForTrident.put(key, bnsCascadeSet);
        }*/

        cascadeDataForTrident.put("BD Micro-Fine Ultra hypodermic insulin needles for pre-filled / reusable pen injectors screw on 4mm/32gauge Pk: 100",
                new HashSet<>(Arrays.asList(ActualSupplierData.builder().code("7943103").cascadePrice(Double.valueOf("4.36")).cascadeStatus("Available").build()
                        //,TridentData.builder().code("6177560").cascadePrice(Double.valueOf("4.64")).cascadeStatus("Available").build()
                )
                ));

       /* cascadeDataForTrident.put("Carbimazole 5mg tablets Pk: 100",
                new HashSet<>(Arrays.asList(TridentData.builder().code("1182302").cascadePrice(Double.valueOf("2.85")).cascadeStatus("Available").build()
                )
                ));

        cascadeDataForTrident.put("Carbocisteine 250mg/5ml oral solution sugar free Pk: 300",
                new HashSet<>(Arrays.asList(TridentData.builder().code("7365323").cascadePrice(Double.valueOf("4.40")).cascadeStatus("Available").build()
                )
                ));*/
        Map<String,Set<ActualSupplierData>> processedTridentData = process.processBns(cascadeDataForTrident);
        System.out.println(processedTridentData);
        Long endTime = System.currentTimeMillis();
        System.out.println("Total time taken for the whole process is "+ (endTime-startTime)/1000 +" seconds");
    }



    public Map<String,Set<ActualSupplierData>> processBns(Map<String,Set<ActualSupplierData>> cascadeDataForBns) throws InterruptedException {





        WebDriverManager.chromedriver().setup();;
        WebDriver driver = new ChromeDriver();
        driver.get("https://www.bnsgroup.co.uk/login.do");

        Thread.sleep(1000);

        driver.findElement(By.id("userName")).sendKeys("bridgwater.pharmacy@nhs.net");
        driver.findElement(By.id("pass")).sendKeys("Brid@8486");
        driver.findElement(By.xpath("/html[1]/body[1]/main[1]/div[1]/div[2]/div[1]/div[1]/div[1]/div[1]/div[2]/form[1]/div[4]/button[1]"))
                .sendKeys(Keys.RETURN);



        for(Map.Entry<String,Set<ActualSupplierData>> entry: cascadeDataForBns.entrySet()){
            String description = entry.getKey();
            Set<ActualSupplierData> bnsDataSet = entry.getValue();

            for(ActualSupplierData bnsData : bnsDataSet){
                System.out.println("!!!!!Bns Searching for pip code "+bnsData.getCode()+":");
                Thread.sleep(1000);
                String pipCode = bnsData.getCode();
                // search box
                driver.findElement(By.xpath("//html[1]/body[1]/strong[1]/strong[1]/div[2]/div[2]/div[1]/div[1]/input[1]")).clear();
                driver.findElement(By.xpath("//html[1]/body[1]/strong[1]/strong[1]/div[2]/div[2]/div[1]/div[1]/input[1]")).sendKeys(pipCode);
                driver.findElement(By.xpath("/html[1]/body[1]/strong[1]/strong[1]/div[2]/div[2]/div[1]/div[1]/input[1]")).sendKeys(Keys.RETURN);
                Thread.sleep(2000);
                // retrieved product list click
                try{
                    /*driver.findElement(By.xpath("/html[1]/body[1]/div[1]/header[1]/div[1]/div[1]/div[1]/div[4]/div[1]/div[1]/div[1]/div[1]/span[1]/div[2]/div[2]/ul[1]/li[1]")).click();
                    Thread.sleep(3000);*/
                    //String descriptionFromWebsite = driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[2]/span[1]/div[1]/div[1]/div[2]/div[3]/div[1]/div[1]/div[3]/div[2]/div[1]/h4[1]")).getText();
                    String descriptionFromWebsite = driver.findElement(By.xpath("/html/body/div[1]/div[2]/span/div/div/div[2]/div[6]/div[2]/div[2]/span/div/div/div/div[2]/div/div[1]/span/p[1]")).getText();
                    String priceFromWebsite = null;

                    try{
                       // priceFromWebsite = driver.findElement(By.xpath("//html[1]/body[1]/div[1]/div[2]/span[1]/div[1]/div[1]/div[2]/div[3]/div[1]/div[1]/div[3]/div[2]/div[1]/div[4]/div[1]/div[1]/div[1]/span[1]")).getText();
                        priceFromWebsite = driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[2]/span[1]/div[1]/div[1]/div[2]/div[6]/div[2]/div[2]/span[1]/div[1]/div[1]/div[1]/div[2]/div[1]/div[3]/span[1]/div[1]/div[1]/span[1]")).getText();
                    }catch (Exception e){
                        System.out.println("priceFromWebsite is failing at div 4");
                        try{
                            priceFromWebsite = driver.findElement(By.xpath("//html[1]/body[1]/div[1]/div[2]/span[1]/div[1]/div[1]/div[2]/div[3]/div[1]/div[1]/div[3]/div[2]/div[1]/div[5]/div[1]/div[1]/div[1]/span[1]")).getText();
                        }catch (Exception exception){
                            System.out.println("priceFromWebsite is failing at div 5");
                        }
                    }

                    //String availablityFromWebsite = driver.findElement(By.xpath("//div[@class='avail-col-text avail-col-width']")).getText();
                    String availablityFromWebsite = driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[2]/span[1]/div[1]/div[1]/div[2]/div[6]/div[2]/div[2]/span[1]/div[1]/div[1]/div[1]/div[2]/div[1]/div[2]/div[1]/div[2]")).getText();
                    System.out.println("PipCode:"+pipCode+"; descFromWebsite:"+descriptionFromWebsite+ "; priceFromWebsite:"+ priceFromWebsite+ "; availability:"+availablityFromWebsite);
                    bnsData.setSupplierPrice(!StringUtils.isEmpty(priceFromWebsite) ?Double.valueOf(priceFromWebsite.replace("£","")):null);
                    bnsData.setDefinitePrice(!StringUtils.isEmpty(priceFromWebsite) ?Double.valueOf(priceFromWebsite.replace("£","")):null);
                    bnsData.setSupplierStatus(!StringUtils.isEmpty(availablityFromWebsite)?stockAvailability(availablityFromWebsite):null);
                    bnsData.setDefiniteStatus(!StringUtils.isEmpty(availablityFromWebsite)?stockAvailability(availablityFromWebsite):null);

                }catch (Exception e){
                    //e.printStackTrace();
                    System.out.println("Zero products might have resulted back for "+ pipCode);
                }
                Thread.sleep(1000);
            }
        }

        try{
            /*Actions actions = new Actions(driver);
            WebElement element = driver.findElement(By.xpath("/html[1]/body[1]/div[1]/header[1]/div[1]/div[1]/div[1]/div[5]/div[1]/div[1]/div[1]/div[3]/div[1]/div[1]/lightning-icon[1]/span[1]/lightning-primitive-icon[1]/*[name()='svg'][1]"));
            actions.moveToElement(element).click().perform();
            element = driver.findElement(By.xpath("/html[1]/body[1]/div[1]/header[1]/div[1]/div[1]/div[1]/div[5]/div[1]/div[1]/div[1]/div[3]/div[2]/div[1]/a[11]"));
            actions.moveToElement(element).click().perform();*/
            driver.findElement(By.xpath("/html[1]/body[1]/div[1]/strong[1]/strong[1]/div[1]/div[1]/div[1]/div[1]/div[1]/a[1]/i[1]")).click();
            driver.findElement(By.xpath("/html[1]/body[1]/div[11]/div[3]/div[1]/button[1]/i[1]")).click();
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("Falied to click on the logout");
        }


        return cascadeDataForBns;


    }

    private String stockAvailability(String availability){
        switch (availability){
            case "Out of stock":
                return "Not Available";
            case "In stock":
                return "Available";
            default:
                return null;

        }
    }




}