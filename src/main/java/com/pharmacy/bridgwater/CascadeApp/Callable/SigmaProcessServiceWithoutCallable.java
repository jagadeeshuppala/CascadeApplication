package com.pharmacy.bridgwater.CascadeApp.Callable;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.pharmacy.bridgwater.CascadeApp.model.ActualSupplierData;
import com.pharmacy.bridgwater.CascadeApp.service.CascadeService;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


public class SigmaProcessServiceWithoutCallable  {


    public static void main(String args[]) throws InterruptedException,  JsonProcessingException {
        Long startTime = System.currentTimeMillis();
        Map<String,Set<String>> cascadeDataForSigma = new LinkedHashMap<>();

        SigmaProcessServiceWithoutCallable process = new SigmaProcessServiceWithoutCallable();
        CascadeService cascade = new CascadeService();
        Map<String, Set<ActualSupplierData>> cascadeResultsMap = cascade.getCascadeResults();
        for (Map.Entry<String, Set<ActualSupplierData>> entry : cascadeResultsMap.entrySet()) {
            String key = entry.getKey();
            Set<String> pipSet = entry.getValue().stream().filter(cs ->null!=cs.getCode())
                    .map(cs -> cs.getCode())
                    .collect(Collectors.toSet());
            cascadeDataForSigma.put(key, pipSet);
        }

        Map<String,Set<ActualSupplierData>> processedSigmaData = process.process(cascadeResultsMap);
        System.out.println(processedSigmaData);
        Long endTime = System.currentTimeMillis();
        System.out.println("Total time taken for the whole process is "+ (endTime-startTime)/1000 +" seconds");
    }



    public Map<String,Set<ActualSupplierData>> process(Map<String,Set<ActualSupplierData>> cascadeDataForSigma) throws InterruptedException {

        //Map<String,Set<ActualSupplierData>> sigmaProcessedData = new LinkedHashMap<>();

        WebDriverManager.chromedriver().setup();;
        WebDriver driver = new ChromeDriver();
        driver.get("https://www.sigconnect.co.uk/login");

        driver.findElement(By.id("loginform-username")).sendKeys("bridgwater.pharmacy@nhs.net");
        driver.findElement(By.id("loginform-password")).sendKeys("Bridg@8486");
        driver.findElement(By.id("login_btn"))
                .sendKeys(Keys.RETURN);

        Thread.sleep(1000);
        //driver.findElement(By.xpath("/html/body/form/div/div/center/div/label")).click();
        //driver.findElement(By.xpath("/html[1]/body[1]/form[1]/div[1]/div[1]/center[1]/p[2]/button[1]")).click();

        for (Map.Entry<String, Set<ActualSupplierData>> entry : cascadeDataForSigma.entrySet()) {
            //Set<ActualSupplierData> sigmaData = new HashSet<>();
            Set<ActualSupplierData> existingData  = entry.getValue();

            Set<String> pipCodes = existingData.stream()
                    .filter(v ->v.getCode()!=null)
                    .map(ActualSupplierData::getCode).collect(Collectors.toSet());

            for(String pip : pipCodes){
                Thread.sleep(1000);






                try{
                    WebElement pipTextBox = driver.findElement(By.xpath("/html/body/article/div/div[1]/form/div[1]/div[2]/input"));
                    pipTextBox.clear();
                    pipTextBox.sendKeys(pip);
                    pipTextBox.sendKeys("\n\n");
                    Thread.sleep(3000);

                    String stockClass = driver.findElement(By.xpath("/html/body/article/div/div[3]/div/dl/div/dt")).getAttribute("class");
                    if(!stockClass.equalsIgnoreCase("ng-binding special")){
                        String description = driver.findElement(By.xpath("/html/body/article/div/div[3]/div/dl/div/dt")).getText();
                        String price = driver.findElement(By.xpath("/html/body/article/div/div[3]/div/dl/div/dd/span[3]")).getText();
                        String availability = stockAvailability(stockClass);
                        System.out.println("!!!! Sigma !!!! description:"+description+"; pip:"+pip+"; availability:"+availability + "; price"+price);
                        ActualSupplierData s = ActualSupplierData.builder().description(description).supplierPrice(!StringUtils.isEmpty(price)? Double.valueOf(price.replaceAll("£","")):null)
                                .supplier("Sigma")
                                .definitePrice(!StringUtils.isEmpty(price)? Double.valueOf(price.replaceAll("£","")):null)
                                .supplierStatus(availability).code(pip)
                                .definiteStatus(availability).code(pip).build();
                        existingData.add(s);
                        // breaking as i am not going to search for other products
                        break;
                    }
                }catch (Exception e){
                    System.out.println(e.getMessage());
                    System.out.println("!!!!Sigma!!!! No results for the pip "+ pip);
                }
            }
            //sigmaProcessedData.put(entry.getKey(), sigmaData);
        }
        //logoff
        try{
            driver.findElement(By.xpath("/html[1]/body[1]/nav[1]/ul[1]/li[2]/a[1]")).click();
        }catch (Exception e){
            System.out.println("!!!Sigma!!! Failed to loggoff");
        }

        driver.close();
        return cascadeDataForSigma;

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