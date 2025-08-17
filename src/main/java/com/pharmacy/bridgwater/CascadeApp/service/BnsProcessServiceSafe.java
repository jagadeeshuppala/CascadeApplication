package com.pharmacy.bridgwater.CascadeApp.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.pharmacy.bridgwater.CascadeApp.model.ActualSupplierData;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import java.util.*;


public class BnsProcessServiceSafe {

    public static void main(String args[]) throws InterruptedException, JsonProcessingException {
        Long startTime = System.currentTimeMillis();
        Map<String,Set<ActualSupplierData>> cascadeDataForTrident = new LinkedHashMap<>();

        BnsProcessServiceSafe process = new BnsProcessServiceSafe();
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

        /*1367C
        0421E
        0359B
        0356A
        4357A
        4357B*/


        cascadeDataForTrident.put("BD Micro-Fine Ultra hypodermic insulin needles for pre-filled / reusable pen injectors screw on 4mm/32gauge Pk: 100",
                new HashSet<>(Arrays.asList(
                        ActualSupplierData.builder().code("1367C").price(Double.valueOf("4.36")).status("Available").build()
                ))


        );

        cascadeDataForTrident.put("BD Micro-Fine Ultra hypodermic insulin needles for pre-filled / reusable pen injectors screw on 4mm/32gauge Pk: 1002",
                new HashSet<>(Arrays.asList(
                        ActualSupplierData.builder().code("0421E").price(Double.valueOf("4.36")).status("Available").build()
                ))


        );

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
                    String descriptionFromWebsite = driver.findElement(By.xpath("/html/body/ul/li[3]/a/table/tbody/tr/td[2]")).getText();
                    String priceFromWebsite = driver.findElement(By.xpath("/html/body/ul/li[3]/a/table/tbody/tr/td[4]")).getText();
                    String priceWithoutPoundSign = !StringUtils.isEmpty(priceFromWebsite)?priceFromWebsite.replaceAll("£",""):"";

                    String availablityFromWebsite = driver.findElement(By.xpath("/html/body/ul/li[3]/a/table/tbody/tr/td[3]/i")).getAttribute("class");
                    String avialabityToText = !StringUtils.isEmpty(availablityFromWebsite)?stockAvailability(availablityFromWebsite):"";
                    driver.findElement(By.xpath("/html/body/ul/li[3]/a/table/tbody/tr/td[3]/i"));
                    System.out.println("PipCode:"+pipCode+"; descFromWebsite:"+descriptionFromWebsite+ "; priceFromWebsite:"+ priceWithoutPoundSign+ "; availability:"+avialabityToText);

                }catch (Exception e){
                    //e.printStackTrace();
                    System.out.println("Zero products might have resulted back for "+ pipCode);
                }
                Thread.sleep(1000);
            }
        }

        try{

            driver.findElement(By.xpath("/html[1]/body[1]/div[1]/strong[1]/strong[1]/div[1]/div[1]/div[1]/div[1]/div[1]/a[1]/span[1]")).click();
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("Falied to click on the logout");
        }


        return cascadeDataForBns;


    }

    private String stockAvailability(String availability){
        switch (availability){
            case "fa fa-circle icon_red":
                return "Not Available";
            case "fa fa-circle icon_green":
                return "Available";
            default:
                return null;

        }
    }




}