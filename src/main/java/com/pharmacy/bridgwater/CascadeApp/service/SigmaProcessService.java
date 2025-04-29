package com.pharmacy.bridgwater.CascadeApp.service;


import com.pharmacy.bridgwater.CascadeApp.model.SigmaData;
import io.github.bonigarcia.wdm.WebDriverManager;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;

import java.util.*;


public class SigmaProcessService {

    /*public static void main(String args[]) throws InterruptedException, JsonProcessingException {
        Long startTime = System.currentTimeMillis();
        Map<String,Set<String>> cascadeDataForSigma = new LinkedHashMap<>();

        SigmaProcess process = new SigmaProcess();
        Cascade cascade = new Cascade();
        Map<String, List<CascadeSupplier>> cascadeResultsMap = cascade.getCascadeResults();
        for (Map.Entry<String, List<CascadeSupplier>> entry : cascadeResultsMap.entrySet()) {
            String key = entry.getKey();
            Set<String> pipSet = entry.getValue().stream().filter(cs ->!"".equalsIgnoreCase(cs.getCode()))
                    .map(cs -> cs.getCode())
                    .collect(Collectors.toSet());
            cascadeDataForSigma.put(key, pipSet);
        }

        Map<String,Set<SigmaData>> processedSigmaData = process.processSigma(cascadeDataForSigma);
        System.out.println(processedSigmaData);
        Long endTime = System.currentTimeMillis();
        System.out.println("Total time taken for the whole process is "+ (endTime-startTime)/1000 +" seconds";);
    }*/



    public Map<String,Set<SigmaData>> processSigma(Map<String,Set<String>> cascadeDataForSigma) throws InterruptedException {

        Map<String,Set<SigmaData>> sigmaProcessedData = new LinkedHashMap<>();

        WebDriverManager.chromedriver().setup();;
        WebDriver driver = new ChromeDriver();
        driver.get("https://www.sigconnect.co.uk/login");

        driver.findElement(By.id("loginform-username")).sendKeys("bridgwater.pharmacy@nhs.net");
        driver.findElement(By.id("loginform-password")).sendKeys("Bridg@8486");
        driver.findElement(By.id("login_btn"))
                .sendKeys(Keys.RETURN);

        Thread.sleep(1000);
        driver.findElement(By.xpath("/html/body/form/div/div/center/div/label")).click();
        driver.findElement(By.xpath("/html[1]/body[1]/form[1]/div[1]/div[1]/center[1]/p[2]/button[1]")).click();

        for (Map.Entry<String, Set<String>> entry : cascadeDataForSigma.entrySet()) {
            Set<String> pipCodes = entry.getValue();
            Set<SigmaData> sigmaData = new HashSet<>();
            for(String pip : pipCodes){
                Thread.sleep(1000);



                WebElement pipTextBox = driver.findElement(By.xpath("/html/body/article/div/div[1]/form/div[1]/div[2]/input"));
                pipTextBox.clear();
                pipTextBox.sendKeys(pip);
                pipTextBox.sendKeys("\n\n");
                Thread.sleep(3000);


                try{
                    String stockClass = driver.findElement(By.xpath("/html/body/article/div/div[3]/div/dl/div/dt")).getAttribute("class");
                    if(!stockClass.equalsIgnoreCase("ng-binding special")){
                        String description = driver.findElement(By.xpath("/html/body/article/div/div[3]/div/dl/div/dt")).getText();
                        System.out.println("Desc"+description);
                        String price = driver.findElement(By.xpath("/html/body/article/div/div[3]/div/dl/div/dd/span[3]")).getText();
                        String availability = stockAvailability(stockClass);
                        System.out.println("description:"+description+"; pip:"+pip+"; availability:"+availability + "; price"+price);
                        SigmaData s = SigmaData.builder().description(description).price(price!=null? Double.valueOf(price.replaceAll("£","")):null)
                                .status(availability).code(pip).build();
                        sigmaData.add(s);
                        // breaking as i am not going to search for other products
                        break;
                    }
                }catch (Exception e){
                    System.out.println(e.getMessage());
                    System.out.println("No results for the pip "+ pip);
                }
            }
            sigmaProcessedData.put(entry.getKey(), sigmaData);
        }

        driver.findElement(By.xpath("/html[1]/body[1]/nav[1]/ul[1]/li[2]/a[1]")).click();
        return sigmaProcessedData;

    }

    private String stockAvailability(String stockAvailablityClass){
        switch (stockAvailablityClass){
            case "ng-binding no_stock":
                return "Not Available";
            case "ng-binding":
                return "Available";
            case "ng-binding low_stock":
                return "Low Stock";
            default:
                return null;

        }
    }




}