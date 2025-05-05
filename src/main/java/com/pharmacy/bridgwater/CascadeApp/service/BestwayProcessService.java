package com.pharmacy.bridgwater.CascadeApp.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.pharmacy.bridgwater.CascadeApp.model.ActualSupplierData;
import com.pharmacy.bridgwater.CascadeApp.model.BestwayData;
import com.pharmacy.bridgwater.CascadeApp.model.CascadeSupplier;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class BestwayProcessService implements Callable<Map<String, Set<ActualSupplierData>>> {
    Map<String,Set<ActualSupplierData>> cascadeDataForBestway;
    public BestwayProcessService(Map<String,Set<ActualSupplierData>> cascadeDataForBestway){
        this.cascadeDataForBestway = cascadeDataForBestway;
    }

    /*public static void main(String args[]) throws InterruptedException, JsonProcessingException {
        Long startTime = System.currentTimeMillis();
        Map<String,Set<ActualSupplierData>> cascadeDataForBestway = new LinkedHashMap<>();

        BestwayProcessService process = new BestwayProcessService();
        CascadeServiceNew cascade = new CascadeServiceNew();
        Map<String, Set<ActualSupplierData>> cascadeResultsMap = cascade.getCascadeResults();
        *//*for (Map.Entry<String, Set<ActualSupplierData>> entry : cascadeResultsMap.entrySet()) {
            String key = entry.getKey();
            Set<ActualSupplierData> bestwayCascadeSet = entry.getValue().stream().filter(cs ->"Bestway MedHub".equalsIgnoreCase(cs.getSupplier())
                            && !StringUtils.isEmpty(cs.getCode())
                    )
                    .map(v -> ActualSupplierData.builder().description(key).cascadePrice(v.getCascadePrice()).cascadeStatus(v.getCascadeStatus()).code(v.getCode()).build())
                    .collect(Collectors.toSet());
            cascadeDataForBestway.put(key, bestwayCascadeSet);
        }*//*

        *//*cascadeDataForBestway.put("Levothyroxine sodium 100microgram tablets Pk: 28",
                new HashSet<>(Arrays.asList(ActualSupplierData.builder().description("Levothyroxine sodium 100microgram tablets Pk: 28").code("1022144").cascadePrice(Double.valueOf("20.00")).cascadeStatus("Available").build()
                        //,BestwayData.builder().code("6177560").cascadePrice(Double.valueOf("4.64")).cascadeStatus("Available").build()
                )
                ));

        cascadeDataForBestway.put("Lisinopril 20mg tablets Pk: 28",
                new HashSet<>(Arrays.asList(ActualSupplierData.builder().description("Lisinopril 20mg tablets Pk: 28").code("1103282").cascadePrice(Double.valueOf("20.00")).cascadeStatus("Available").build()
                )
                ));

        cascadeDataForBestway.put("Carbocisteine 250mg/5ml oral solution sugar free Pk: 300",
                new HashSet<>(Arrays.asList(ActualSupplierData.builder().code("7365323").cascadePrice(Double.valueOf("4.40")).cascadeStatus("Available").build()
                )
                ));*//*
        Map<String,Set<ActualSupplierData>> processedTridentData = process.processBestway(cascadeResultsMap);
        System.out.println(processedTridentData);
        Long endTime = System.currentTimeMillis();
        System.out.println("Total time taken for the whole process is "+ (endTime-startTime)/1000 +" seconds");
    }*/



    @Override
    public Map<String,Set<ActualSupplierData>> call() throws InterruptedException {
        WebDriverManager.chromedriver().setup();;
        WebDriver driver = new ChromeDriver();
        driver.get("https://portal.bestwaymedhub.co.uk/en-GB/Account/Login");

        Thread.sleep(1000);

        //login
        driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[2]/div[1]/div[1]/section[1]/form[1]/div[1]/div[2]/input[1]")).sendKeys("bridgwater.pharmacy@nhs.net");
        driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[2]/div[1]/div[1]/section[1]/form[1]/div[2]/div[2]/input[1]")).sendKeys("Bridg@8486");
        Thread.sleep(1000);

        //button
        driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[2]/div[1]/div[1]/section[1]/form[1]/div[4]/div[2]/input[1]"))
                .sendKeys(Keys.RETURN);
        Thread.sleep(1000);



        for(Map.Entry<String,Set<ActualSupplierData>> entry: cascadeDataForBestway.entrySet()){
            String description = entry.getKey();
            Set<ActualSupplierData> bestwayDataSet = entry.getValue();

            for(ActualSupplierData bestwayData : bestwayDataSet){
                if("Bestway MedHub".equalsIgnoreCase(bestwayData.getSupplier())  && !StringUtils.isEmpty(bestwayData.getCode())){
                    System.out.println("!!!!Bestway!!!! Searching for pip code "+bestwayData.getDescription()+":");
                    Thread.sleep(1000);
                    String bestwayDescription = bestwayData.getDescription();
                    String[] descriptionTokenizer = bestwayDescription.split(" ");
                    // search box
                    driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[1]/div[3]/div[3]/form[1]/div[1]/div[1]/div[1]/div[1]/input[1]")).clear();
                    driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[1]/div[3]/div[3]/form[1]/div[1]/div[1]/div[1]/div[1]/input[1]")).sendKeys(descriptionTokenizer[0]);
                    driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[1]/div[3]/div[3]/form[1]/div[1]/div[1]/div[1]/div[1]/input[1]")).sendKeys(Keys.RETURN);
                    Thread.sleep(1000);


                    //check if any results came back
                    //List<WebElement> productTableListElement = driver.findElements(By.xpath("//*[@id=\"TabsId-prodListView\"]"));
                    String textDisplayedAslistOfNumber = driver.findElement(By.xpath("//*[@id=\"content-container\"]/div/div[2]/div[2]")).getText();
                    System.out.println("textDisplayedAslistOfNumber:"+textDisplayedAslistOfNumber);
                    Pattern pattern = Pattern.compile("of\\s+(\\d+)");
                    Matcher matcher = pattern.matcher(textDisplayedAslistOfNumber);
                    Integer numberOfProducts = 0;
                    if (matcher.find()) {
                        String number = matcher.group(1);
                        numberOfProducts = Integer.valueOf(number);
                    } else {
                        System.out.println("No results found.");
                    }
                    System.out.println(textDisplayedAslistOfNumber);
                    //for(int i=0; i<numberOfProducts  ; i++) {
                    //List<WebElement> productListElement = driver.findElements(By.xpath("//*[@id=\"item\"]"));


                    int actualJvalue =1;
                    for(int j = 1; j<= numberOfProducts && actualJvalue <=numberOfProducts; j++){
                        System.out.println("Acutal J value "+actualJvalue + " prodListElement size "+ numberOfProducts);
                        actualJvalue++;

                        try{
                            String descFromBestwayWebsite = driver.findElement(By.xpath("/html/body/div[1]/div[2]/div/div[2]/div[3]/div/div/div/div[1]/div["+(j)+"]/div[1]")).getText();
                            String priceFromBestwayWebsite = driver.findElement(By.xpath("/html/body/div[1]/div[2]/div/div[2]/div[3]/div/div/div/div[1]/div["+(j)+"]/div[7]")).getText();
                            String pipCodeFromBestwayWebsiteWithCat = driver.findElement(By.xpath("/html/body/div[1]/div[2]/div/div[2]/div[3]/div/div/div/div[1]/div["+(j)+"]/div[6]")).getText();
                            String stockFromBestwayWebsite = driver.findElement(By.xpath("/html/body/div[1]/div[2]/div/div[2]/div[3]/div/div/div/div[1]/div["+(j)+"]/div[9]/img")).getAttribute("src");

                            String pipCodeFromBestwayWebsite = !StringUtils.isEmpty(pipCodeFromBestwayWebsiteWithCat)? pipCodeFromBestwayWebsiteWithCat.split(" ")[1]:null;

                            System.out.println("!!!!Bestway !!! Description:"+descFromBestwayWebsite+"; price:"+priceFromBestwayWebsite+"; stock"+stockAvailability(stockFromBestwayWebsite)+ "; pipcode"+pipCodeFromBestwayWebsite);
                            if(!StringUtils.isEmpty(pipCodeFromBestwayWebsite) && pipCodeFromBestwayWebsite.equals(!StringUtils.isEmpty(bestwayData.getCode()) ? bestwayData.getCode() : "")){
                                bestwayData.setSupplierPrice(!StringUtils.isEmpty(priceFromBestwayWebsite) ? Double.valueOf(priceFromBestwayWebsite.replace("Price: ","").trim()):null);
                                bestwayData.setDefinitePrice(!StringUtils.isEmpty(priceFromBestwayWebsite) ? Double.valueOf(priceFromBestwayWebsite.replace("Price: ","").trim()):null);
                                bestwayData.setSupplierStatus(stockAvailability(stockFromBestwayWebsite));
                                bestwayData.setDefiniteStatus(stockAvailability(stockFromBestwayWebsite));
                                bestwayData.setSupplier("Bestway MedHub");
                                System.out.println("!!!!Bestway !!! Found the match at:" +actualJvalue);
                                break;
                            }
                            if(j%6 == 0){
                                System.out.println("Into this loop");
                                j=1;
                                //click on next button
                                try{
                                    driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[2]/div[1]/div[2]/div[4]/div[1]/ul[1]/li[3]/a[1]")).click();
                                    Thread.sleep(1000);
                                }catch (Exception e){
                                    //e.printStackTrace();
                                    System.out.println("!!!!Bestway !!! There is no next button");
                                }
                            }

                        }catch (Exception e){
                            e.printStackTrace();
                            System.out.println("!!!!Bestway !!! It might be end of the list");
                        }
                    }
                }



            }
            System.out.println("================================================================");

        }

        //}

        try{
            driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[1]/div[1]/div[7]/form[1]/div[1]/a[1]")).click();
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("!!!!Bestway !!! Falied to click on the logout");
        }


        return cascadeDataForBestway;


    }

    private String stockAvailability(String availability){
        switch (availability){
            //<img src="/Content/Images/Low_Stock.jpg">
            case "https://portal.bestwaymedhub.co.uk/Out of stock":
            case "https://portal.bestwaymedhub.co.uk/Content/Images/Low_Stock.jpg":
                return "Not Available";
            case "https://portal.bestwaymedhub.co.uk/Content/Images/High_Stock.jpg":
                return "Available";
            default:
                return null;

        }
    }




}