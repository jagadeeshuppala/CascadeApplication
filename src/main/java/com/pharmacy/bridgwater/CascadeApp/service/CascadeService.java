package com.pharmacy.bridgwater.CascadeApp.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.pharmacy.bridgwater.CascadeApp.model.ActualSupplierData;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


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
        Thread.sleep(1000);



        //cascade order pad click
        driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[1]/header[1]/div[2]/nav[1]/div[2]/ul[1]/li[3]/a[1]")).click();

        //click on view to get the orderId from alternate product
        String orderId = null;

        if (driver instanceof JavascriptExecutor) {
            try{
                JavascriptExecutor js = (JavascriptExecutor)driver;
                WebElement element = driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[2]/form[1]/div[10]/div[2]/div[1]/div[1]/div[1]/div[1]/table[1]/tbody[1]/tr[1]/td[10]/a[2]"));
                js.executeScript("arguments[0].click();", element);
                Thread.sleep(2000);

                String href = driver.findElement(By.xpath("/html/body/div/div[2]/form/div[1]/div[2]/div/div/div[2]/div[1]/div/a[1]")).getAttribute("href");
                //https://victoria-os.com/UI/VicOrdering/AlternativeProduct?orderId=11138&lineNo=1
                Pattern p = Pattern.compile("orderId=(.*?)&lineNo");
                Matcher m = p.matcher(href);


                if (m.find()) {
                    orderId = m.group(1);
                } else {
                    System.out.println("Could not derive orderId from the alternate button");
                }
                // close button click
                driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[2]/form[1]/div[3]/div[2]/div[1]/div[2]/div[1]/a[1]")).sendKeys(Keys.RETURN);

            }catch (Exception e){
                System.out.println("Please check why this has failed!!!!!!!");
                e.printStackTrace();
            }

        }

        String descOfNumberOfItemsDisplayedInBanner = driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[2]/form[1]/div[10]/div[2]/div[1]/div[1]/div[1]/div[1]/div[1]")).getText();
        Pattern pattern = Pattern.compile("of\\s+(\\d+)");
        Matcher matcher = pattern.matcher(descOfNumberOfItemsDisplayedInBanner);
        int totalNoOfProducts = 0;

        if (matcher.find()) {
            String number = matcher.group(1);
            totalNoOfProducts = Integer.valueOf(number);
        } else {
            System.out.println("Could not find Items Filtered: 0 of 57");
        }


        Map<String, Set<ActualSupplierData>> cascadeProductList = new LinkedHashMap<>();
        int noOfProducts  = totalNoOfProducts;
        for(int actualValue=1; actualValue<=totalNoOfProducts; actualValue++){
            System.out.println("!!!! Sno: " + actualValue+ " Still remaining products: " + --noOfProducts);
            String url = "https://victoria-os.com/UI/VicOrdering/Expanded?orderId="+orderId+"&lineNo="+actualValue;
            System.out.println("URL : "+ url);
            driver.get(url);
            Thread.sleep(1000);

            try{
                List<WebElement> suppliersList = driver.findElements(By.xpath("/html[1]/body[1]/div[1]/div[2]/form[1]/div[2]/div[2]/div[1]/div[1]/div[1]/div[1]/table[1]/tbody[1]/tr"));
                System.out.println("size of internal table :::"+suppliersList.size());

                String quantityFromWebsite =  driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[2]/form[1]/div[1]/div[2]/div[1]/div[1]/div[1]/div[1]/div[2]")).getText().replaceAll("Quantity:","");
                String tariffFromWebsite = driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[2]/form[1]/div[1]/div[2]/div[1]/div[1]/div[1]/div[2]/div[1]")).getText().replaceAll("Tariff:","");
                String tariffAfterDeductionFromWebsite = driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[2]/form[1]/div[1]/div[2]/div[1]/div[1]/div[1]/div[2]/div[2]")).getText().replaceAll("Tariff After Deduction:","");
                String concessionPriceFromWebsite = driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[2]/form[1]/div[1]/div[2]/div[1]/div[1]/div[1]/div[2]/div[3]")).getText().replaceAll("Concession Price:","");
                System.out.println("!!!!!! QuantityFromWebsite:"+quantityFromWebsite+ "; tariff:"+tariffFromWebsite + "; tariffAfterDeduction:"+tariffAfterDeductionFromWebsite+"; concessionPrice:"+concessionPriceFromWebsite+";");

                Set<ActualSupplierData> cascadeSupplierList = new LinkedHashSet<>();
                String descriptionFromWebsite = null;
                for(int j=1; j<=suppliersList.size();j++){
                    descriptionFromWebsite = driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[2]/form[1]/div[2]/div[2]/div[1]/div[1]/div[1]/div[1]/table[1]/tbody[1]/tr["+j+"]/td[2]")).getText();
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

                    if(!StringUtils.isEmpty(code)){
                        cascadeSupplierList.add(ActualSupplierData.builder()
                                .supplier(supplier).price(priceInDouble).code(code).status(availability)
                                //.definitePrice(priceInDouble)
                                //.definiteStatus(availability)
                                .description(descriptionFromWebsite)
                                .quantity(!StringUtils.isEmpty(quantityFromWebsite)?Integer.valueOf(quantityFromWebsite.trim()):null)
                                .tariff(!StringUtils.isEmpty(tariffFromWebsite)?Double.valueOf(tariffFromWebsite.trim()):null)
                                .tariffAfterDeduction(!StringUtils.isEmpty(tariffAfterDeductionFromWebsite)?Double.valueOf( tariffAfterDeductionFromWebsite.trim()):null)
                                .concession(!StringUtils.isEmpty(concessionPriceFromWebsite)?Double.valueOf(tariffFromWebsite.trim()):null)
                                .build());
                    }

                }
                if(descriptionFromWebsite!=null){
                    cascadeProductList.put(descriptionFromWebsite, cascadeSupplierList);
                }


            }catch (Exception e){
                e.printStackTrace();
            }
        }

        //logoff
        driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[1]/header[1]/div[1]/section[1]/form[1]/a[1]")).click();

        driver.quit();


        System.out.println("==================================================================");
        System.out.println("cascadeProductList"+cascadeProductList);
        System.out.println("==================================================================");



        //logoff
        //driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[1]/header[1]/div[1]/section[1]/form[1]/a[1]")).click();

        return cascadeProductList;
    }
}
