package com.pharmacy.bridgwater.CascadeApp.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.pharmacy.bridgwater.CascadeApp.model.CascadeSupplier;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;

import java.util.*;
import java.util.stream.Collectors;



public class CascadeService {



    /*public static void main(String[] args) throws InterruptedException, JsonProcessingException {

        Cascade cascade = new Cascade();
        Map<CascadeSupplierKey, List<CascadeSupplier>> cascadeResults = cascade.getCascadeResults();

    }*/
    public Map<String, List<CascadeSupplier>> getCascadeResults() throws InterruptedException, JsonProcessingException {



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



        List<WebElement> numberOfLis = driver.findElements(By.xpath("/html[1]/body[1]/div[1]/div[2]/form[1]/div[10]/div[2]/div[1]/div[1]/div[1]/div[1]/table[1]/tbody[1]/tr"));
        System.out.println("size is :::"+numberOfLis.size());
        Map<String, List<CascadeSupplier>> cascadeProductList = new LinkedHashMap<>();

        for(int i=1; i<=numberOfLis.size();i++){
            String descriptionFromWebsite = driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[2]/form[1]/div[10]/div[2]/div[1]/div[1]/div[1]/div[1]/table[1]/tbody[1]/tr["+i+"]/td[2]")).getText();
            System.out.println("desc:"+descriptionFromWebsite +":");
            /*String quantityFromWebsite =driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[2]/form[1]/div[10]/div[2]/div[1]/div[1]/div[1]/div[1]/table[1]/tbody[1]/tr["+i+"]/td[3]/input[1]")).getAttribute("value");;
            System.out.println("quantity:"+quantityFromWebsite +":");*/

            // view click
            try{

                JavascriptExecutor js;
                if (driver instanceof JavascriptExecutor) {
                    js = (JavascriptExecutor)driver;
                    WebElement element = driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[2]/form[1]/div[10]/div[2]/div[1]/div[1]/div[1]/div[1]/table[1]/tbody[1]/tr["+i+"]/td[10]/a[2]"));
                    js.executeScript("arguments[0].click();", element);
                }
                List<WebElement> suppliersList = driver.findElements(By.xpath("/html[1]/body[1]/div[1]/div[2]/form[1]/div[2]/div[2]/div[1]/div[1]/div[1]/div[1]/table[1]/tbody[1]/tr"));
                System.out.println("size is :::"+suppliersList.size());

                String quantityFromWebsite =  driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[2]/form[1]/div[1]/div[2]/div[1]/div[1]/div[1]/div[1]/div[2]")).getText().replaceAll("Quantity:","");
                String tariffFromWebsite = driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[2]/form[1]/div[1]/div[2]/div[1]/div[1]/div[1]/div[2]/div[1]")).getText().replaceAll("Tariff:","");
                String tariffAfterDeductionFromWebsite = driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[2]/form[1]/div[1]/div[2]/div[1]/div[1]/div[1]/div[2]/div[2]")).getText().replaceAll("Tariff After Deduction:","");
                String concessionPriceFromWebsite = driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[2]/form[1]/div[1]/div[2]/div[1]/div[1]/div[1]/div[2]/div[3]")).getText().replaceAll("Concession Price:","");
                System.out.println("!!!!!! QuantityFromWebsite:"+quantityFromWebsite+ "; tariff:"+tariffFromWebsite + "; tariffAfterDeduction:"+tariffAfterDeductionFromWebsite+"; concessionPrice:"+concessionPriceFromWebsite+";");

                List<CascadeSupplier> cascadeSupplierList = new ArrayList<>();
                for(int j=1; j<=suppliersList.size();j++){
                    String supplier =driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[2]/form[1]/div[2]/div[2]/div[1]/div[1]/div[1]/div[1]/table[1]/tbody[1]/tr["+j+"]/td[3]")).getText();
                    String price =driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[2]/form[1]/div[2]/div[2]/div[1]/div[1]/div[1]/div[1]/table[1]/tbody[1]/tr["+j+"]/td[4]")).getText();
                    Double priceInDouble = price!=null?Double.valueOf(price):null;
                    String code =driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[2]/form[1]/div[2]/div[2]/div[1]/div[1]/div[1]/div[1]/table[1]/tbody[1]/tr["+j+"]/td[7]")).getText();
                    String color = "No Data";
                    try{
                        color = driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[2]/form[1]/div[2]/div[2]/div[1]/div[1]/div[1]/div[1]/table[1]/tbody[1]/tr["+j+"]/td[10]/i[1]")).getAttribute("style");
                        if("color: green;".equalsIgnoreCase(color)){
                            color = "Available";
                        }else if("color: red;".equalsIgnoreCase(color)){
                            color = "Available";
                        }else{
                            color = "please check if there is any change from website on css";
                        }
                    }catch (Exception e){
                        System.out.println("please check what is the status from website I value is "+ i + "; J value is "+j);
                    }

                    //List<CascadeSupplier> supplierListAlreadyAdded = cascadeProductList.get(descriptionFromWebsite);
                    if(!cascadeSupplierList.isEmpty()){
                        List<CascadeSupplier>  supplierExist = cascadeSupplierList.stream()
                                .filter(sl -> supplier.equalsIgnoreCase(sl.getSupplier()))
                                .collect(Collectors.toList());
                        if( !supplierExist.isEmpty()){
                            CascadeSupplier existingSupplierEntry = supplierExist.get(0);
                            if(priceInDouble.compareTo(existingSupplierEntry.getPrice()) < 0){
                                System.out.println("!!!!!!Already cheaper from this supplier is coming,"+existingSupplierEntry.getPrice()+ "so removing the existing one"+price);
                                cascadeSupplierList.remove(existingSupplierEntry);
                            }

                        }
                    }
                    cascadeSupplierList.add(CascadeSupplier.builder().supplier(supplier).price(priceInDouble).code(code).status(color)
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
        }
        System.out.println("==================================================================");
        System.out.println("cascadeProductList"+cascadeProductList);
        System.out.println("==================================================================");

/*        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json = ow.writeValueAsString(cascadeProductList);
        System.out.println(json);*/

        //logoff
        driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[1]/header[1]/div[1]/section[1]/form[1]/a[1]")).click();

        return cascadeProductList;
    }
}
