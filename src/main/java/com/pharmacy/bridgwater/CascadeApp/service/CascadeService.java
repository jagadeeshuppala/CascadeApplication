package com.pharmacy.bridgwater.CascadeApp.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.pharmacy.bridgwater.CascadeApp.model.ActualSupplierData;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class CascadeService {



    public static void main(String[] args) throws InterruptedException, JsonProcessingException {

        CascadeService cascade = new CascadeService();
        Map<String, Set<ActualSupplierData>> cascadeResults = cascade.getCascadeResults();
        System.out.println(cascadeResults);

    }
    public Map<String, Set<ActualSupplierData>> getCascadeResults() throws InterruptedException, JsonProcessingException {



        WebDriverManager.chromedriver().setup();
        WebDriver driver = new ChromeDriver();
        driver.get("https://victoria-os.com/UI/home/aahcascade");





        driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[1]/header[1]/div[1]/section[1]/ul[1]/li[2]/a[1]")).sendKeys(Keys.RETURN);
        driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[2]/section[1]/section[1]/form[1]/div[1]/fieldset[1]/div[2]/input[1]")).sendKeys("Bridgwater");
        driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[2]/section[1]/section[1]/form[1]/div[1]/fieldset[1]/div[4]/input[1]")).sendKeys("Brid@8486");

        driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[2]/section[1]/section[1]/form[1]/div[1]/fieldset[1]/div[6]/input[1]"))
                .sendKeys(Keys.RETURN);
        Thread.sleep(3000);

        //cascade order pad click
        driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[1]/header[1]/div[2]/nav[1]/div[2]/ul[1]/li[3]/a[1]")).click();

        String descOfNumberOfItemsDisplayedInBanner = driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[2]/form[1]/div[10]/div[2]/div[1]/div[1]/div[1]/div[1]/div[1]")).getText();
        Pattern pattern = Pattern.compile("of\\s+(\\d+)");
        Matcher matcher = pattern.matcher(descOfNumberOfItemsDisplayedInBanner);
        Integer totalNoOfProducts = 0;
        if (matcher.find()) {
            String number = matcher.group(1);
            totalNoOfProducts = Integer.valueOf(number);
        } else {
            System.out.println("Could not find Items Filtered: 0 of 57");
        }

        Map<String, Set<ActualSupplierData>> cascadeProductList = new LinkedHashMap<>();
        //int actualValue = 0;

        int resetValue = 0;
        int buttonCount = 0;
        for(int actualValue=0; actualValue<totalNoOfProducts; actualValue++){
            resetValue++;

            String descriptionFromWebsite = driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[2]/form[1]/div[10]/div[2]/div[1]/div[1]/div[1]/div[1]/table[1]/tbody[1]/tr["+resetValue+"]/td[2]")).getText();
            //String descriptionFromWebsite = driver.findElement(By.xpath("/html/body/div[1]/div[2]/form/div[10]/div[2]/div/div[1]/div/div/table/tbody/tr["+resetValue+"]/td[2]")).getText();
            System.out.println("Product sno: "+ actualValue+ "; desc:"+descriptionFromWebsite +":");

            // view click
            try{

                JavascriptExecutor js;
                if (driver instanceof JavascriptExecutor) {
                    js = (JavascriptExecutor)driver;
                    WebElement element = driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[2]/form[1]/div[10]/div[2]/div[1]/div[1]/div[1]/div[1]/table[1]/tbody[1]/tr["+resetValue+"]/td[10]/a[2]"));
                    js.executeScript("arguments[0].click();", element);
                }
                List<WebElement> suppliersList = driver.findElements(By.xpath("/html[1]/body[1]/div[1]/div[2]/form[1]/div[2]/div[2]/div[1]/div[1]/div[1]/div[1]/table[1]/tbody[1]/tr"));
                System.out.println("size of internal table :::"+suppliersList.size());

                String quantityFromWebsite =  driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[2]/form[1]/div[1]/div[2]/div[1]/div[1]/div[1]/div[1]/div[2]")).getText().replaceAll("Quantity:","");
                String tariffFromWebsite = driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[2]/form[1]/div[1]/div[2]/div[1]/div[1]/div[1]/div[2]/div[1]")).getText().replaceAll("Tariff:","");
                String tariffAfterDeductionFromWebsite = driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[2]/form[1]/div[1]/div[2]/div[1]/div[1]/div[1]/div[2]/div[2]")).getText().replaceAll("Tariff After Deduction:","");
                String concessionPriceFromWebsite = driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[2]/form[1]/div[1]/div[2]/div[1]/div[1]/div[1]/div[2]/div[3]")).getText().replaceAll("Concession Price:","");
                System.out.println("!!!!!! QuantityFromWebsite:"+quantityFromWebsite+ "; tariff:"+tariffFromWebsite + "; tariffAfterDeduction:"+tariffAfterDeductionFromWebsite+"; concessionPrice:"+concessionPriceFromWebsite+";");

                Set<ActualSupplierData> cascadeSupplierList = new LinkedHashSet<>();
                for(int j=1; j<=suppliersList.size();j++){
                    String supplier =driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[2]/form[1]/div[2]/div[2]/div[1]/div[1]/div[1]/div[1]/table[1]/tbody[1]/tr["+j+"]/td[3]")).getText();
                    String price =driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[2]/form[1]/div[2]/div[2]/div[1]/div[1]/div[1]/div[1]/table[1]/tbody[1]/tr["+j+"]/td[4]")).getText();
                    Double priceInDouble = price!=null?Double.valueOf(price):null;
                    String code =driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[2]/form[1]/div[2]/div[2]/div[1]/div[1]/div[1]/div[1]/table[1]/tbody[1]/tr["+j+"]/td[7]")).getText();
                    String availability = "Not Available";
                    try{
                        availability = driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[2]/form[1]/div[2]/div[2]/div[1]/div[1]/div[1]/div[1]/table[1]/tbody[1]/tr["+j+"]/td[10]/i[1]")).getAttribute("style");
                        if("color: green;".equalsIgnoreCase(availability)){
                            availability = "Available";
                        }else if("color: red;".equalsIgnoreCase(availability)){
                            availability = "Not Available";
                        }else{
                            availability = "please check if there is any change from website on css";
                        }
                    }catch (Exception e){
                        //e.printStackTrace();
                        System.out.println("please check what is the status from website I value is "+ actualValue + "; J value is "+j);
                    }

                    cascadeSupplierList.add(ActualSupplierData.builder()
                            .supplier(supplier).cascadePrice(priceInDouble).code(!StringUtils.isEmpty(code)?code:null).cascadeStatus(availability)
                            .definitePrice(priceInDouble)
                            .definiteStatus(availability)
                            .description(descriptionFromWebsite)
                            .quantity(!StringUtils.isEmpty(quantityFromWebsite)?Integer.valueOf(quantityFromWebsite.trim()):null)
                            .tariff(!StringUtils.isEmpty(tariffFromWebsite)?Double.valueOf(tariffFromWebsite.trim()):null)
                            .tariffAfterDeduction(!StringUtils.isEmpty(tariffAfterDeductionFromWebsite)?Double.valueOf( tariffAfterDeductionFromWebsite.trim()):null)
                            .concession(!StringUtils.isEmpty(concessionPriceFromWebsite)?Double.valueOf(tariffFromWebsite.trim()):null)
                            .build());
                }
                // close button click
                driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[2]/form[1]/div[3]/div[2]/div[1]/div[2]/div[1]/a[1]")).sendKeys(Keys.RETURN);
                cascadeProductList.put(descriptionFromWebsite, cascadeSupplierList);


            }catch (Exception e){
                e.printStackTrace();
            }
            if((actualValue+1)%20 == 0){
                resetValue = 0;
                JavascriptExecutor js;
                if (driver instanceof JavascriptExecutor) {
                    js = (JavascriptExecutor)driver;
                    WebElement element = driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[2]/form[1]/div[10]/div[2]/div[1]/div[1]/div[1]/div[1]/div[2]/div[1]/ul[1]/li[6]/a[1]"));
                    js.executeScript("arguments[0].click();", element);

                    //js.executeScript(element.click(););
                }

            }

        }

        /*while(actualValue< totalNoOfProducts){
            //List<WebElement> numberOfLis = driver.findElements(By.xpath("/html[1]/body[1]/div[1]/div[2]/form[1]/div[10]/div[2]/div[1]/div[1]/div[1]/div[1]/table[1]/tbody[1]/tr"));
            List<WebElement> numberOfLis = driver.findElements(By.xpath("/html/body/div[1]/div[2]/form/div[10]/div[2]/div/div[1]/div/div/table/tbody/tr"));

            System.out.println("size of external table :::"+numberOfLis.size());


            for(int i=1; i<=numberOfLis.size() && actualValue<= totalNoOfProducts; i++){
                actualValue++;

            }

            try{
                JavascriptExecutor js;
                if (driver instanceof JavascriptExecutor) {
                    js = (JavascriptExecutor)driver;
                    WebElement element = driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[2]/form[1]/div[10]/div[2]/div[1]/div[1]/div[1]/div[1]/div[2]/div[1]/ul[1]/li[4]/a[1]"));
                    System.out.println("!!!!!!!!!!!Clicking on next button!!!!!!!!!!!!!!");
                    js.executeScript("arguments[0].click();", element);
                    *//*if(element.isEnabled()){
                        System.out.println("!!!!!!!!!!!Next button is enabled!!!!!!!!!!!!!!");
                        js.executeScript("arguments[0].click();", element);
                    }else{
                        System.out.println("!!!!!!!!!Next button is Not enabled!!!!!!!!!!!!!");
                    }*//*
                }
            }catch (Exception e){
                e.printStackTrace();
                System.out.println("No next button");
            }


        }*/





        System.out.println("==================================================================");
        System.out.println("cascadeProductList"+cascadeProductList);
        System.out.println("==================================================================");



        //logoff
        driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[1]/header[1]/div[1]/section[1]/form[1]/a[1]")).click();

        return cascadeProductList;
    }
}
