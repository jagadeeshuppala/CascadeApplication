package com.pharmacy.bridgwater.CascadeApp.service;


import com.pharmacy.bridgwater.CascadeApp.model.ActualSupplierData;
import com.pharmacy.bridgwater.CascadeApp.model.OrderListKey;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import java.util.*;
import java.util.concurrent.Callable;

import static com.pharmacy.bridgwater.CascadeApp.constants.Constants.BNS_PIP_BLANK;
import static com.pharmacy.bridgwater.CascadeApp.constants.Constants.SUPPLIER_BNS;


public class BNSProcessService implements Callable<Map<OrderListKey, Set<ActualSupplierData>>> {
    Set<OrderListKey> orderListBnsPipCodesList;
    public BNSProcessService(Set<OrderListKey> orderListBnsPipCodesList){
        this.orderListBnsPipCodesList = orderListBnsPipCodesList;
    }

    /*public static void main(String[] args) throws InterruptedException {
        Map<OrderListKey,Set<String>> map = new LinkedHashMap<>();
        map.put(OrderListKey.builder().bnsPipCode("1367C").build(), null);
        map.put(OrderListKey.builder().bnsPipCode("0421E").build(), null);
        BNSProcessService bnsProcessService = new BNSProcessService(map);
        Map<OrderListKey, Set<ActualSupplierData>> call = bnsProcessService.call();

    }*/



    @Override
    public Map<OrderListKey,Set<ActualSupplierData>> call() throws InterruptedException {




        Map<OrderListKey,Set<ActualSupplierData>> bnsOnlyProcessedData = new LinkedHashMap<>();
        WebDriverManager.chromedriver().setup();;
        WebDriver driver = new ChromeDriver();
        driver.get("https://www.bnsgroup.co.uk/login.do");

        Thread.sleep(1000);

        driver.findElement(By.id("userName")).sendKeys("bridgwater.pharmacy@nhs.net");
        driver.findElement(By.id("pass")).sendKeys("Brid@8486");
        driver.findElement(By.xpath("/html[1]/body[1]/main[1]/div[1]/div[2]/div[1]/div[1]/div[1]/div[1]/div[2]/form[1]/div[4]/button[1]"))
                .sendKeys(Keys.RETURN);

        Thread.sleep(1000);

        int totalNoOfRecords = orderListBnsPipCodesList.size();

        //for (Map.Entry<OrderListKey, Set<String>> entry : cascadeDataForBns.entrySet()) {
        for (OrderListKey orderListKey: orderListBnsPipCodesList) {
            System.out.println("!!!!!Bns!!!!! still total no of records "+ totalNoOfRecords-- +":");
            Set<ActualSupplierData> bnsOnlyData = new LinkedHashSet<>();

            String bnsPip = orderListKey.getBnsPipCode();
            System.out.println("!!!!!Bns Searching for pip code "+bnsPip+":");
            if(!StringUtils.isEmpty(bnsPip) &&  !BNS_PIP_BLANK.equals(bnsPip)){
                Thread.sleep(1000);

                // search box
                driver.findElement(By.xpath("//html[1]/body[1]/strong[1]/strong[1]/div[2]/div[2]/div[1]/div[1]/input[1]")).clear();
                driver.findElement(By.xpath("//html[1]/body[1]/strong[1]/strong[1]/div[2]/div[2]/div[1]/div[1]/input[1]")).sendKeys(bnsPip);
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
                    System.out.println("PipCode:"+bnsPip+"; descFromWebsite:"+descriptionFromWebsite+ "; priceFromWebsite:"+ priceWithoutPoundSign+ "; availability:"+avialabityToText);

                    ActualSupplierData s = ActualSupplierData.builder().description(descriptionFromWebsite)
                            .price(!StringUtils.isEmpty(priceWithoutPoundSign)? Double.valueOf(priceWithoutPoundSign.replaceAll("£","")):null)
                            .supplier(SUPPLIER_BNS)
                            .status(avialabityToText).code(bnsPip)
                            .build();
                    bnsOnlyData.add(s);

                    bnsOnlyProcessedData.put(orderListKey, bnsOnlyData);

                }catch (Exception e){
                    //e.printStackTrace();
                    System.out.println("Zero products might have resulted back for "+ bnsPip);
                }
            }



            Thread.sleep(1000);


        }
        try {
            driver.findElement(By.xpath("/html[1]/body[1]/div[1]/strong[1]/strong[1]/div[1]/div[1]/div[1]/div[1]/div[1]/a[1]/span[1]")).click();
        }catch (Exception e){
            System.out.println("!!!Bns!!! Failed to logoff");
        }
        driver.quit();
        return bnsOnlyProcessedData;

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