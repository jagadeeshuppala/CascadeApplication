package com.pharmacy.bridgwater.CascadeApp;

import com.pharmacy.bridgwater.CascadeApp.model.ActualSupplierData;
import com.pharmacy.bridgwater.CascadeApp.model.OrderListKey;
import com.pharmacy.bridgwater.CascadeApp.service.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static com.pharmacy.bridgwater.CascadeApp.constants.Constants.*;


public class Desktop {

        //In Pharmacy

        /*public static final String ORIGINAL_FILE_NAME = "\\\\11701279QSVR\\PSSharedarea\\Bridgwater\\Miscellaneous\\OrderList1.xlsx";
        public static final String WORK_TO_BE_DONE_FILE_NAME = "\\\\11701279QSVR\\PSSharedarea\\Bridgwater\\Miscellaneous\\OrderList1.xlsx";
        public static final String COPIED_FILE_NAME = "\\\\11701279QSVR\\PSSharedarea\\Bridgwater\\Miscellaneous\\OrderList-JagCopy.xlsx";*/

        //At Home
        public static final String ORIGINAL_FILE_NAME = "C:\\Users\\msola\\OneDrive\\Desktop\\OL.xlsx";
        public static final String WORK_TO_BE_DONE_FILE_NAME = "C:\\Users\\msola\\OneDrive\\Desktop\\OL.xlsx";
        public static final String COPIED_FILE_NAME = "C:\\Users\\msola\\OneDrive\\Desktop\\OL_Backup.xlsx";

        /*public static final String ORIGINAL_FILE_NAME = "C:\\Users\\uppal\\Desktop\\OrderList1.xlsx";
        public static final String WORK_TO_BE_DONE_FILE_NAME = "C:\\Users\\uppal\\Desktop\\OrderList1.xlsx";
        public static final String COPIED_FILE_NAME = "C:\\Users\\uppal\\Desktop\\OrderList_Copy_copy.xlsx";*/

        public static final String CASCADE_UPLOAD_FILE_NAME = "upload.csv";
        public static final String CASCADE_UPLOAD_FILE_NAME_WITH_ORDER_LIST_SNO = "mapping.txt";




        public static final String SIGMA_ORDERING_TEXT_IN_NOTES = "s";

        //
        static String currentDir = System.getProperty("user.dir");
       // System.out.println("Current dir using System:" + currentDir);
        public static final String CASCADE_UPLOAD_FILE_BASE_LOCATION = currentDir;



        public static void main(String[] args) throws Exception{
                Long startTime = System.currentTimeMillis();

                File original = new File(ORIGINAL_FILE_NAME);
                File copied = new File(COPIED_FILE_NAME);
                FileUtils.copyFile(original, copied);

                //updating the file with the results
                FileInputStream file = new FileInputStream(WORK_TO_BE_DONE_FILE_NAME);
                Workbook workbook = new XSSFWorkbook(file);
                Sheet sheet0 = workbook.getSheetAt(0);

                FileInputStream copiedFile = new FileInputStream(COPIED_FILE_NAME);
                Workbook bakupFileWorkbook = new XSSFWorkbook(copiedFile);
                Sheet copiedSheet = bakupFileWorkbook.getSheetAt(0);



                // normalFont
                CellStyle normalFontStyle = workbook.createCellStyle();
                Font normalFont=workbook.createFont();
                normalFont.setColor(IndexedColors.BLACK.getIndex());
                normalFont.setFontHeightInPoints((short) 10);
                normalFont.setFontName("Arial");
                normalFontStyle.setFont(normalFont);

                //accounting font
                /*CellStyle accountingFontStyle = workbook.createCellStyle();
                //accountingFontStyle.setDataFormat((short)8);
                //accountingFontStyle.setDataFormat("##.##_");
                Font accountingFont = workbook.createFont();
                accountingFont.setFontHeightInPoints((short) 10);
                accountingFont.setFontName("Arial");
                accountingFont.setColor(IndexedColors.BLACK.getIndex());
                accountingFontStyle.setFont(accountingFont);*/


                // redFont
                CellStyle redFontStyle = workbook.createCellStyle();
                Font redFont=workbook.createFont();
                redFont.setFontHeightInPoints((short) 10);
                redFont.setFontName("Arial");
                redFont.setColor(IndexedColors.RED.getIndex());
                redFontStyle.setFont(redFont);

                //Green font
                CellStyle greenFontStyle = workbook.createCellStyle();
                Font greenFont=workbook.createFont();
                greenFont.setFontHeightInPoints((short) 10);
                greenFont.setFontName("Arial");
                greenFont.setColor(IndexedColors.GREEN.getIndex());
                greenFontStyle.setFont(greenFont);

                //Green Bold font
                CellStyle greenBoldFontStyle = workbook.createCellStyle();
                greenBoldFontStyle.setFillForegroundColor(IndexedColors.YELLOW1.getIndex());
                greenBoldFontStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                //greenBoldFontStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("#,##0.00 "));
                Font greenBoldFont=workbook.createFont();
                greenBoldFont.setFontHeightInPoints((short) 10);
                greenBoldFont.setFontName("Arial");
                greenBoldFont.setBold(true);
                greenBoldFont.setColor(IndexedColors.GREEN.getIndex());
                greenBoldFontStyle.setFont(greenBoldFont);

                //Orange font
                CellStyle orangeFontStyle = workbook.createCellStyle();
                Font orangeFont=workbook.createFont();
                orangeFont.setColor(IndexedColors.ORANGE.getIndex());
                orangeFontStyle.setFont(orangeFont);


                CascadeServiceFromOrderList cascade = new CascadeServiceFromOrderList();
                Map<OrderListKey, Set<ActualSupplierData>> cascadeResults =  new HashMap<>();
                try{
                    cascadeResults =  cascade.getCascadeResult(WORK_TO_BE_DONE_FILE_NAME);
                }catch (Exception e){
                    System.out.println("Error in getting cascade results "+ e.getMessage());
                }
                Map<OrderListKey, Set<String>> sigmaPipCodes =  new LinkedHashMap<>();
                for (Map.Entry<OrderListKey, Set<ActualSupplierData>> entry : cascadeResults.entrySet()) {
                        sigmaPipCodes.put(entry.getKey(), entry.getValue().stream().filter(v -> !StringUtils.isEmpty(v.getCode())).map(ActualSupplierData::getCode).collect(Collectors.toSet()));
                }





            ExecutorService executor = Executors.newFixedThreadPool(5);


                //Filter aahResults and pass it to Aah service to fetch the aah results
                Map<OrderListKey, Set<ActualSupplierData>> unprocessedAahResults = new LinkedHashMap<>();
                for (Map.Entry<OrderListKey, Set<ActualSupplierData>> entry : cascadeResults.entrySet()) {
                        OrderListKey key = entry.getKey();
                        Set<ActualSupplierData> onlyAahUnprocessedSet = entry.getValue().stream().filter(v -> !StringUtils.isEmpty(v.getCode()))
                                .filter( v -> SUPPLIER_AAH.equalsIgnoreCase(v.getSupplier()))
                                .collect(Collectors.toSet());
                        unprocessedAahResults.put(key, onlyAahUnprocessedSet);
                }

                //Filter tridentResults and pass it to Trident service to fetch the aah results
                Map<OrderListKey, Set<ActualSupplierData>> unprocessedTridentResults = new LinkedHashMap<>();
                for (Map.Entry<OrderListKey, Set<ActualSupplierData>> entry : cascadeResults.entrySet()) {
                        OrderListKey key = entry.getKey();
                        Set<ActualSupplierData> onlyTridentUnprocessedSet = entry.getValue().stream().filter(v -> !StringUtils.isEmpty(v.getCode()))
                                .filter( v -> SUPPLIER_TRIDENT.equalsIgnoreCase(v.getSupplier()))
                                .collect(Collectors.toSet());
                        unprocessedTridentResults.put(key, onlyTridentUnprocessedSet);
                }

                //Filter bestway results and pass it to Bestway service to fetch the bestway results
                Map<OrderListKey, Set<ActualSupplierData>> unprocessedBestwayResults = new LinkedHashMap<>();
                for (Map.Entry<OrderListKey, Set<ActualSupplierData>> entry : cascadeResults.entrySet()) {
                        OrderListKey key = entry.getKey();
                        Set<ActualSupplierData> onlyBestwayUnprocessedSet = entry.getValue().stream().filter(v -> !StringUtils.isEmpty(v.getCode()))
                                .filter( v -> SUPPLIER_BESTWAY.equalsIgnoreCase(v.getSupplier()))
                                .collect(Collectors.toSet());
                        unprocessedBestwayResults.put(key, onlyBestwayUnprocessedSet);
                }

                Callable<Map<OrderListKey, Set<ActualSupplierData>>> aahWorker = new AahProcessService(unprocessedAahResults);
                Future<Map<OrderListKey, Set<ActualSupplierData>>> aahFuture = executor.submit(aahWorker);


                Callable<Map<OrderListKey, Set<ActualSupplierData>>> tridentWorker = new TridentProcessService(unprocessedTridentResults);
                Future<Map<OrderListKey, Set<ActualSupplierData>>> tridentFuture = executor.submit(tridentWorker);

                Callable<Map<OrderListKey, Set<ActualSupplierData>>> bestwayWorker = new BestwayProcessService(unprocessedBestwayResults);
                Future<Map<OrderListKey, Set<ActualSupplierData>>> bestwayFuture = executor.submit(bestwayWorker);


                Callable<Map<OrderListKey, Set<ActualSupplierData>>> sigmaWorker = new SigmaProcessService(sigmaPipCodes);
                Future<Map<OrderListKey, Set<ActualSupplierData>>>  sigmaFuture = executor.submit(sigmaWorker);

                Callable<Map<OrderListKey, Set<ActualSupplierData>>> bnsWorker = new BNSProcessService(cascadeResults.keySet());
                Future<Map<OrderListKey, Set<ActualSupplierData>>>  bnsFuture = executor.submit(bnsWorker);

                executor.shutdown();

            Map<OrderListKey, Set<ActualSupplierData>> aahProcessedResults = new HashMap<>();
            Map<OrderListKey, Set<ActualSupplierData>> tridentProcessedResults = new HashMap<>();
            Map<OrderListKey, Set<ActualSupplierData>> bestwayProcessedResults = new HashMap<>();
            Map<OrderListKey, Set<ActualSupplierData>> sigmaProcessedResults = new HashMap<>();
            Map<OrderListKey, Set<ActualSupplierData>> bnsProcessedResults = new HashMap<>();

            try{
                aahProcessedResults = aahFuture.get();
            }catch (Exception e){
                System.out.println("Failed in aah "+ e.getMessage());
            }
            try{
                tridentProcessedResults = tridentFuture.get();
            }catch (Exception e){
                System.out.println("Failed in Trident "+ e.getMessage());
            }
            try{
                bestwayProcessedResults = bestwayFuture.get();
            }catch (Exception e){
                System.out.println("Failed in Bestway "+ e.getMessage());
            }
            try{
                sigmaProcessedResults = sigmaFuture.get();
            }catch (Exception e){
                System.out.println("Failed in sigma "+ e.getMessage());
            }
            try{
                bnsProcessedResults = bnsFuture.get();
            }catch (Exception e){
                System.out.println("Failed in BNS "+ e.getMessage());
            }



                System.out.println(cascadeResults);

                //replace cascade results with aahProcessed results
                for (Map.Entry<OrderListKey, Set<ActualSupplierData>> entry : cascadeResults.entrySet()) {
                        OrderListKey key = entry.getKey();

                        //remove

                        // add sigma processed results
                        entry.getValue().addAll(sigmaProcessedResults.get(key));

                        //add Bns Processed results
                        Set<ActualSupplierData> actualSupplierData = bnsProcessedResults.get(key);
                        if(actualSupplierData!=null){
                            entry.getValue().addAll(bnsProcessedResults.get(key));
                        }

                }

                System.out.println(cascadeResults);





                // iterate through the map to find out which one is least and add to the excel
                for (Map.Entry<OrderListKey, Set<ActualSupplierData>> entry : cascadeResults.entrySet()) {
                        OrderListKey key = entry.getKey();
                        Set<ActualSupplierData> value = entry.getValue();
                        if(value.isEmpty()){
                            continue;
                        }
                        ActualSupplierData cheaperAahData = value.stream()
                                .filter(v-> SUPPLIER_AAH.equalsIgnoreCase(v.getSupplier()))
                                .filter(v-> v.getPrice()!=null)
                                .min(Comparator.comparing(ActualSupplierData::getPrice)
                                )
                                .orElse(null);


                        ActualSupplierData cheaperBestwayData = value.stream().filter(v-> SUPPLIER_BESTWAY.equalsIgnoreCase(v.getSupplier()))
                                .filter(v-> v.getPrice()!=null)
                                .min(Comparator.comparing(ActualSupplierData::getPrice)
                                )
                                .orElse(null);

                        ActualSupplierData cheaperBnsData = value.stream().filter(v-> SUPPLIER_BNS.equalsIgnoreCase(v.getSupplier()))
                                .filter(v-> v.getPrice()!=null)
                                .min(Comparator.comparing(ActualSupplierData::getPrice)
                                )
                                .orElse(null);

                        ActualSupplierData cheaperLexonData = value.stream().filter(v-> SUPPLIER_LEXON.equalsIgnoreCase(v.getSupplier()))
                                .filter(v-> v.getPrice()!=null)
                                .min(Comparator.comparing(ActualSupplierData::getPrice)
                                )
                                .orElse(null);

                        ActualSupplierData cheaperSigmaData = value.stream().filter(v-> SUPPLIER_SIGMA.equalsIgnoreCase(v.getSupplier()))
                                .filter(v-> v.getPrice()!=null)
                                .min(Comparator.comparing(ActualSupplierData::getPrice)
                                )
                                .orElse(null);

                        ActualSupplierData cheaperTridentData = value.stream()
                                .filter(v-> v.getPrice()!=null)
                                .filter(v-> SUPPLIER_TRIDENT.equalsIgnoreCase(v.getSupplier()))
                                .min(Comparator.comparing(ActualSupplierData::getPrice)
                                )
                                .orElse(null);

                        ActualSupplierData cheaperAllianceData = value.stream().filter(v-> SUPPLIER_ALLIANCE.equalsIgnoreCase(v.getSupplier()))
                                .filter(v-> v.getPrice()!=null)
                                .min(Comparator.comparing(ActualSupplierData::getPrice)
                                )
                                .orElse(null);

                        Set<ActualSupplierData> s = new HashSet<>();
                        if(cheaperAahData!=null){
                               s.add(cheaperAahData) ;
                        }
                        if(cheaperBestwayData!=null){
                                s.add(cheaperBestwayData) ;
                        }
                        if(cheaperBnsData!=null){
                                s.add(cheaperBnsData) ;
                        }
                        if(cheaperLexonData!=null){
                                s.add(cheaperLexonData) ;
                        }
                        if(cheaperSigmaData!=null){
                                s.add(cheaperSigmaData) ;
                        }
                        if(cheaperTridentData!=null){
                                s.add(cheaperTridentData) ;
                        }
                        if(cheaperAllianceData!=null){
                                s.add(cheaperAllianceData) ;
                        }

                        ActualSupplierData cheaperPrice = //Stream.of(cheaperAahData, cheaperBestwayData, cheaperBnsData, cheaperLexonData, cheaperSigmaData, cheaperTridentData, cheaperAllianceData)
                                s.stream()
                                .min(Comparator.comparing(ActualSupplierData::getPrice))
                                .stream().findAny().orElse(null);

                        ActualSupplierData cascadeDataForProductDesc = //Stream.of(cheaperAahData, cheaperBestwayData, cheaperBnsData, cheaperLexonData, cheaperSigmaData, cheaperTridentData, cheaperAllianceData)
                                s.stream()
                                        .filter( v-> !SUPPLIER_SIGMA.equalsIgnoreCase(v.getSupplier()))
                                        .findAny().orElse(null);



                        Row sheet0Row = sheet0.getRow(key.getSno());
                        Row copiedFileRow = copiedSheet.getRow(key.getSno());


                        //Order list desc
                        /*Cell cell0 = sheet0Row.createCell(ORDER_LIST_DESC_CELL);
                        cell0.setCellValue(key.getOrderListDesc());
                        cell0.setCellStyle(normalFontStyle);*/

                        //Cascade Desc
                       Cell cell1 = sheet0Row.createCell(CASCADE_DESC_CELL);
                        if(cascadeDataForProductDesc!=null && cascadeDataForProductDesc.getTariff()!=null){
                                cell1.setCellValue(cascadeDataForProductDesc.getDescription());
                        }else{
                                cell1.setCellValue("");
                        }
                        cell1.setCellStyle(normalFontStyle);

                        //quantity
                        /*Cell cell2 = row.createCell(QUANTITY_CELL);
                        if(cheaperPrice!=null && cheaperPrice.getQuantity()!=null){
                                cell2.setCellValue(cheaperPrice.getQuantity());
                        }else{
                                cell2.setCellValue("");
                        }
                        cell2.setCellStyle(normalFontStyle);*/

                        //from
                        /*Cell cell3 = row.createCell(FROM_CELL);
                        cell3.setCellValue("");
                        cell3.setCellStyle(normalFontStyle);*/

                        //notes
                        /*Cell cell4 = row.createCell(NOTES_CELL);
                        cell4.setCellValue("");
                        cell4.setCellStyle(normalFontStyle);*/

                        //Tariff
                        Cell cell5 = sheet0Row.createCell(TARRIF_CELL);
                        if(cascadeDataForProductDesc!=null && cascadeDataForProductDesc.getTariff()!=null){
                                cell5.setCellValue(cascadeDataForProductDesc.getTariff());
                        }else{
                                cell5.setCellValue("");
                        }
                        cell5.setCellStyle(normalFontStyle);

                        //TariffAfterDeduction
                        Cell cell6 = sheet0Row.createCell(TARIFF_AFTER_DEDUCTION_CELL);
                        if(cascadeDataForProductDesc!=null && cascadeDataForProductDesc.getTariffAfterDeduction()!=null){
                                cell6.setCellValue(cascadeDataForProductDesc.getTariffAfterDeduction());
                        }else{
                                cell6.setCellValue("");
                        }
                        cell6.setCellStyle(normalFontStyle);

                        //concession
                        Cell cell7 = sheet0Row.createCell(CONCESSION_CELL);
                        if(cascadeDataForProductDesc!=null && cascadeDataForProductDesc.getConcession()!=null){
                                cell7.setCellValue(cascadeDataForProductDesc.getConcession());
                        }else {
                                cell7.setCellValue("");
                        }
                        cell7.setCellStyle(normalFontStyle);
                        //ordercode
                        Cell cell8 = sheet0Row.createCell(ORDER_LIST_PIP_CODE_CELL);
                        cell8.setCellValue(key.getOrderListPipCode());
                        cell8.setCellStyle(normalFontStyle);


                        //AAH price
                        Cell cell9 = sheet0Row.createCell(AAH_PRICE_CELL);
                        cell9.getCellStyle().setDataFormat(workbook.createDataFormat().getFormat("0.00"));
                        //If both are null, ie. its not stocked
                        if(null != cheaperAahData){
                                //if cheaper is aah, and available then yellow back with green -- greenBoldFontStyle
                                //if aah stock available then green, if stock not available then red
                                if(null != cheaperAahData.getStatus() && SUPPLIER_STATUS_AVAILABLE.equalsIgnoreCase(cheaperAahData.getStatus())
                                        && null != cheaperAahData.getPrice()
                                        && null != cheaperPrice.getPrice()
                                        && cheaperPrice.getPrice().compareTo(cheaperAahData.getPrice()) == 0){
                                        cell9.setCellValue(cheaperAahData.getPrice());
                                        cell9.setCellStyle(greenBoldFontStyle);
                                }else{
                                        if(null != cheaperAahData.getStatus() && SUPPLIER_STATUS_AVAILABLE.equalsIgnoreCase(cheaperAahData.getStatus())){
                                                cell9.setCellValue(cheaperAahData.getPrice());
                                                cell9.setCellStyle(greenFontStyle);
                                        }else{
                                                cell9.setCellValue(cheaperAahData.getPrice());
                                                cell9.setCellStyle(redFontStyle);
                                        }

                                }
                        }else{
                                //cell9.setCellValue("NS");
                            cell9.setCellValue(copiedFileRow.getCell(AAH_PRICE_CELL).getNumericCellValue());
                        }

                        //AAH pip
                        Cell cell10 = sheet0Row.createCell(AAH_PIP_CELL);
                        //If both are null, ie. its not stocked
                        if(null != cheaperAahData  ){
                                //get the cheaper AAH's pip
                                cell10.setCellValue(cheaperAahData.getCode());
                        }else{
                                cell10.setCellValue("NA");
                        }

                        //Bestway
                        Cell cell11 = sheet0Row.createCell(BESTWAY_PRICE_CELL);
                        //If both are null, ie. its not stocked
                        if(null != cheaperBestwayData && null != cheaperPrice ){
                                //if cheaper is bestway, and available then yellow back with green -- greenBoldFontStyle
                                //if bestway stock available then green, if stock not available then red
                                if(null != cheaperBestwayData.getStatus() && SUPPLIER_STATUS_AVAILABLE.equalsIgnoreCase(cheaperBestwayData.getStatus())
                                        && null != cheaperBestwayData.getPrice()
                                        && null != cheaperPrice.getPrice()
                                        && cheaperPrice.getPrice().compareTo(cheaperBestwayData.getPrice()) == 0){
                                        cell11.setCellValue(cheaperBestwayData.getPrice());
                                        cell11.setCellStyle(greenBoldFontStyle);
                                }else{
                                        if(null != cheaperBestwayData.getStatus() && SUPPLIER_STATUS_AVAILABLE.equalsIgnoreCase(cheaperBestwayData.getStatus())){
                                                cell11.setCellValue(cheaperBestwayData.getPrice());
                                                cell11.setCellStyle(greenFontStyle);
                                        }else{
                                                cell11.setCellValue(cheaperBestwayData.getPrice());
                                                cell11.setCellStyle(redFontStyle);
                                        }

                                }
                        }else{
                                //cell11.setCellValue("NS");
                            cell11.setCellValue(copiedFileRow.getCell(BESTWAY_PRICE_CELL).getNumericCellValue());
                        }

                        //Bestway pip
                        Cell cell12 = sheet0Row.createCell(BESTWAY_PIP_CELL);
                        //If both are null, ie. its not stocked
                        if(null != cheaperBestwayData  ){
                                //get the cheaper AAH's pip
                                cell12.setCellValue(cheaperBestwayData.getCode());
                        }else{
                                cell12.setCellValue("NA");
                        }

                        //BNS
                        Cell cell13 = sheet0Row.createCell(BNS_PRICE_CELL);
                        //If both are null, ie. its not stocked
                        if(null != cheaperBnsData && null != cheaperPrice ){
                                //if cheaper is bns, and available then yellow back with green -- greenBoldFontStyle
                                //if bns stock available then green, if stock not available then red
                            if(null != cheaperBnsData.getStatus() && SUPPLIER_STATUS_AVAILABLE.equalsIgnoreCase(cheaperBnsData.getStatus())
                                        && null != cheaperBnsData.getPrice()
                                        && null != cheaperPrice.getPrice()
                                        && cheaperPrice.getPrice().compareTo(cheaperBnsData.getPrice()) == 0){

                                    if(key.getBnsPhonePrice().compareTo(cheaperBnsData.getPrice()) >0){
                                        cell13.setCellValue(cheaperBnsData.getPrice());
                                        cell13.setCellStyle(greenBoldFontStyle);
                                    }else{
                                        cell13.setCellValue(key.getBnsPhonePrice());
                                        //cell13.setCellStyle(greenBoldFontStyle);
                                    }


                                }else{
                                        if(null != cheaperBnsData.getStatus() && SUPPLIER_STATUS_AVAILABLE.equalsIgnoreCase(cheaperBnsData.getStatus())){
                                                cell13.setCellValue(cheaperBnsData.getPrice());
                                                cell13.setCellStyle(greenFontStyle);
                                        }else{
                                                cell13.setCellValue(cheaperBnsData.getPrice());
                                                cell13.setCellStyle(redFontStyle);
                                        }

                                }
                        }else{
                                //cell13.setCellValue("NS");
                            cell13.setCellValue(copiedFileRow.getCell(BNS_PRICE_CELL).getNumericCellValue());
                        }

                        //BNS pip
                        Cell cell14 = sheet0Row.createCell(BNS_PIP_CELL);
                        //If both are null, ie. its not stocked
                        if(null != cheaperBnsData  ){
                                //get the cheaper BNS's pip
                                cell14.setCellValue(cheaperBnsData.getCode());
                        }else{
                                //cell14.setCellValue("NA");
                        }

                        //Lexon
                        Cell cell15 = sheet0Row.createCell(LEXON_PRICE_CELL);
                        //If both are null, ie. its not stocked
                        if(null != cheaperLexonData && null != cheaperPrice ){
                                //if cheaper is Lexon, and available then yellow back with green -- greenBoldFontStyle
                                //if Lexon stock available then green, if stock not available then red
                                if(null != cheaperLexonData.getStatus() && SUPPLIER_STATUS_AVAILABLE.equalsIgnoreCase(cheaperLexonData.getStatus())
                                        && null != cheaperLexonData.getPrice()
                                        && null != cheaperPrice.getPrice()
                                        && cheaperPrice.getPrice().compareTo(cheaperLexonData.getPrice()) == 0){
                                        cell15.setCellValue(cheaperLexonData.getPrice());
                                        cell15.setCellStyle(greenBoldFontStyle);
                                }else{
                                        if(null != cheaperLexonData.getStatus() && SUPPLIER_STATUS_AVAILABLE.equalsIgnoreCase(cheaperLexonData.getStatus())){
                                                cell15.setCellValue(cheaperLexonData.getPrice());
                                                cell15.setCellStyle(greenFontStyle);
                                        }else{
                                                cell15.setCellValue(cheaperLexonData.getPrice());
                                                cell15.setCellStyle(redFontStyle);
                                        }

                                }
                        }else{
                                //cell15.setCellValue("NS");
                            cell15.setCellValue(copiedFileRow.getCell(LEXON_PRICE_CELL).getNumericCellValue());
                        }

                        //Lexon pip
                        Cell cell16 = sheet0Row.createCell(LEXON_PIP_CELL);
                        //If both are null, ie. its not stocked
                        if(null != cheaperLexonData  ){
                                //get the cheaper BNS's pip
                                cell16.setCellValue(cheaperLexonData.getCode());
                        }else{
                                cell16.setCellValue("NA");
                        }

                        //OTC
                        /*Cell cell17 = row.createCell(OTC_PRICE_CELL);
                        cell17.setCellValue("");
                        cell17.setCellStyle(normalFontStyle);*/

                        //OTC PIP
                        Cell cell18 = sheet0Row.createCell(OTC_PIP_CELL);
                        cell18.setCellValue("");
                        cell18.setCellStyle(normalFontStyle);

                        //Sigma
                        Cell cell19 = sheet0Row.createCell(SIGMA_PRICE_CELL);
                        //If both are null, ie. its not stocked
                        if(null != cheaperSigmaData && null != cheaperPrice ){
                                //if cheaper is Sigma, and available then yellow back with green -- greenBoldFontStyle
                                //if Sigma stock available then green, if stock not available then red
                                if(null != cheaperSigmaData.getStatus() && SUPPLIER_STATUS_AVAILABLE.equalsIgnoreCase(cheaperSigmaData.getStatus())
                                        && null != cheaperSigmaData.getPrice()
                                        && null != cheaperPrice.getPrice()
                                        && cheaperPrice.getPrice().compareTo(cheaperSigmaData.getPrice()) == 0){
                                        cell19.setCellValue(cheaperSigmaData.getPrice());
                                        cell19.setCellStyle(greenBoldFontStyle);
                                }else{
                                        if(null != cheaperSigmaData.getStatus() && SUPPLIER_STATUS_AVAILABLE.equalsIgnoreCase(cheaperSigmaData.getStatus())){
                                                cell19.setCellValue(cheaperSigmaData.getPrice());
                                                cell19.setCellStyle(greenFontStyle);
                                        }else{
                                                cell19.setCellValue(cheaperSigmaData.getPrice());
                                                cell19.setCellStyle(redFontStyle);
                                        }

                                }
                        }else{
                                //cell19.setCellValue("NS");
                            cell19.setCellValue(copiedFileRow.getCell(SIGMA_PRICE_CELL).getNumericCellValue());
                        }

                        //Sigma pip
                        Cell cell20 = sheet0Row.createCell(SIGMA_PIP_CELL);
                        //If both are null, ie. its not stocked
                        if(null != cheaperSigmaData  ){
                                //get the cheaper Sigma's pip
                                cell20.setCellValue(cheaperSigmaData.getCode());
                        }else{
                                cell20.setCellValue("NA");
                        }

                        //Trident
                        Cell cell21 = sheet0Row.createCell(TRIDENT_PRICE_CELL);
                        //If both are null, ie. its not stocked
                        if(null != cheaperTridentData && null != cheaperPrice ){
                                //if cheaper is Trident, and available then yellow back with green -- greenBoldFontStyle
                                //if Trident stock available then green, if stock not available then red
                                if(null != cheaperTridentData.getStatus() && SUPPLIER_STATUS_AVAILABLE.equalsIgnoreCase(cheaperTridentData.getStatus())
                                        && null != cheaperTridentData.getPrice()
                                        && null != cheaperPrice.getPrice()
                                        && cheaperPrice.getPrice().compareTo(cheaperTridentData.getPrice()) == 0){
                                        cell21.setCellValue(cheaperTridentData.getPrice());
                                        cell21.setCellStyle(greenBoldFontStyle);
                                }else{
                                        if(null != cheaperTridentData.getStatus() && SUPPLIER_STATUS_AVAILABLE.equalsIgnoreCase(cheaperTridentData.getStatus())){
                                                cell21.setCellValue(cheaperTridentData.getPrice());
                                                cell21.setCellStyle(greenFontStyle);
                                        }else{
                                                cell21.setCellValue(cheaperTridentData.getPrice());
                                                cell21.setCellStyle(redFontStyle);
                                        }

                                }
                        }else{
                                //cell21.setCellValue("NS");
                            cell21.setCellValue(copiedFileRow.getCell(TRIDENT_PRICE_CELL).getNumericCellValue());
                        }

                        //Trident pip
                        Cell cell22 = sheet0Row.createCell(TRIDENT_PIP_CELL);
                        //If both are null, ie. its not stocked
                        if(null != cheaperTridentData  ){
                                //get the cheaper Trident's pip
                                cell22.setCellValue(cheaperTridentData.getCode());
                        }else{
                                cell22.setCellValue("NA");
                        }

                        //Alliance
                        Cell cell23 = sheet0Row.createCell(ALLIANCE_PRICE_CELL);
                        //If both are null, ie. its not stocked
                        if(null != cheaperAllianceData && null != cheaperPrice ){
                                //if cheaper is Alliance, and available then yellow back with green -- greenBoldFontStyle
                                //if Alliance stock available then green, if stock not available then red
                                if(null != cheaperAllianceData.getStatus() && SUPPLIER_STATUS_AVAILABLE.equalsIgnoreCase(cheaperAllianceData.getStatus())
                                        && null != cheaperAllianceData.getPrice()
                                        && null != cheaperPrice.getPrice()
                                        && cheaperPrice.getPrice().compareTo(cheaperAllianceData.getPrice()) == 0){
                                        cell23.setCellValue(cheaperAllianceData.getPrice());
                                        cell23.setCellStyle(greenBoldFontStyle);
                                }else{
                                        if(null != cheaperAllianceData.getStatus() && SUPPLIER_STATUS_AVAILABLE.equalsIgnoreCase(cheaperAllianceData.getStatus())){
                                                cell23.setCellValue(cheaperAllianceData.getPrice());
                                                cell23.setCellStyle(greenFontStyle);
                                        }else{
                                                cell23.setCellValue(cheaperAllianceData.getPrice());
                                                cell23.setCellStyle(redFontStyle);
                                        }

                                }
                        }else{
                                //cell23.setCellValue("NS");
                            cell23.setCellValue(copiedFileRow.getCell(ALLIANCE_PRICE_CELL).getNumericCellValue());
                        }

                        //Alliance pip
                        Cell cell24 = sheet0Row.createCell(ALLIANCE_PIP_CELL);
                        //If both are null, ie. its not stocked
                        if(null != cheaperAllianceData  ){
                                //get the cheaper Trident's pip
                                cell24.setCellValue(cheaperAllianceData.getCode());
                        }else{
                                cell24.setCellValue("NA");
                        }

                      /*  public static final int AAH_CASCADE_PRICE_CELL=41; public static final int AAH_CASCADE_STATUS_CELL=42; public static final int AAH_CASCADE_PIP_CELL=53;
        public static final int BESTWAY_CASCADE_PRICE_CELL=44;public static final int BESTWAY_CASCADE_STATUS_CELL = 45 ;public static final int BESTWAY_CASCADE_PIP_CELL=46;
        public static final int SIGMA_CASCADE_PRICE_CELL=47;public static final int SIGMA_CASCADE_STATUS_CELL=48; public static final int SIGMA_CASCADE_PIP_CELL=49;
        public static final int TRIDENT_CASCADE_PRICE_CELL=50;public static final int TRIDENT_CASCADE_STATUS_CELL=51; public static final int TRIDENT_CASCADE_PIP_CELL=52;
        */

                        //AAH Cascade price
                        Cell cell26 = sheet0Row.createCell(AAH_CASCADE_PRICE_CELL);
                        if(null !=cheaperAahData && null != cheaperAahData.getCascadePrice()){
                                cell26.setCellValue(cheaperAahData.getCascadePrice());
                                /*cell26.setCellStyle(accountingFontStyle);*/
                        }

                        //AAH Cascade status
                        Cell cell27 = sheet0Row.createCell(AAH_CASCADE_STATUS_CELL);
                        if(null !=cheaperAahData && null != cheaperAahData.getCascadeStatus()){
                                cell27.setCellValue(cheaperAahData.getCascadeStatus());
                        }

                        //AAH Cascade PIP
                        Cell cell28 = sheet0Row.createCell(AAH_CASCADE_PIP_CELL);
                        if(null !=cheaperAahData && null != cheaperAahData.getCascadeCode()){
                                cell28.setCellValue(cheaperAahData.getCascadeCode());
                        }

                        //
                        //Bestway Cascade price
                        Cell cell29 = sheet0Row.createCell(BESTWAY_CASCADE_PRICE_CELL);
                        if(null !=cheaperBestwayData && null != cheaperBestwayData.getCascadePrice()){
                                cell29.setCellValue(cheaperBestwayData.getCascadePrice());
                        }

                        //Bestway Cascade status
                        Cell cell30 = sheet0Row.createCell(BESTWAY_CASCADE_STATUS_CELL);
                        if(null !=cheaperBestwayData && null != cheaperBestwayData.getCascadeStatus()){
                                cell30.setCellValue(cheaperBestwayData.getCascadeStatus());
                        }

                        //Bestway Cascade PIP
                        Cell cell31 = sheet0Row.createCell(BESTWAY_CASCADE_PIP_CELL);
                        if(null !=cheaperBestwayData && null != cheaperBestwayData.getCascadeCode()){
                                cell31.setCellValue(cheaperBestwayData.getCascadeCode());
                        }

                        //Sigma Cascade price
                        /*Cell cell32 = row.createCell(SIGMA_CASCADE_PRICE_CELL);
                        if(null !=cheaperSigmaData && null != cheaperSigmaData.getCascadePrice()){
                                cell32.setCellValue(cheaperSigmaData.getCascadePrice());
                        }*/

                        //Sigma Cascade status
                        /*Cell cell33 = row.createCell(SIGMA_CASCADE_STATUS_CELL);
                        if(null !=cheaperSigmaData && null != cheaperSigmaData.getCascadeStatus()){
                                cell33.setCellValue(cheaperSigmaData.getCascadeStatus());
                        }*/

                        //Sigma Cascade PIP
                        /*Cell cell34 = row.createCell(SIGMA_CASCADE_PIP_CELL);
                        if(null !=cheaperSigmaData && null != cheaperSigmaData.getCascadeCode()){
                                cell34.setCellValue(cheaperSigmaData.getCascadeCode());
                        }*/

                        //Trident Cascade price
                        Cell cell35 = sheet0Row.createCell(TRIDENT_CASCADE_PRICE_CELL);
                        if(null !=cheaperTridentData && null != cheaperTridentData.getCascadePrice()){
                                cell35.setCellValue(cheaperTridentData.getCascadePrice());
                        }

                        //Trident Cascade status
                        Cell cell36 = sheet0Row.createCell(TRIDENT_CASCADE_STATUS_CELL);
                        if(null !=cheaperTridentData && null != cheaperTridentData.getCascadeStatus()){
                                cell36.setCellValue(cheaperTridentData.getCascadeStatus());
                        }

                        //Trident Cascade PIP
                        Cell cell37 = sheet0Row.createCell(TRIDENT_CASCADE_PIP_CELL);
                        if(null !=cheaperTridentData && null != cheaperTridentData.getCascadeCode()){
                                cell37.setCellValue(cheaperTridentData.getCascadeCode());
                        }

                        Cell cell25 = sheet0Row.createCell(LOOKED_UP_AT);
                        LocalDateTime now = LocalDateTime.now();
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM HH:mm");
                        String formattedDateTime = now.format(formatter);
                        cell25.setCellValue(formattedDateTime);
                        cell25.setCellStyle(normalFontStyle);


                }

            makeSound();
            Scanner input = new Scanner(System.in);
            System.out.print("Can you please check if the order list file is open on any other clients, if yes can you please save and close that file and press enter to continue");
            String nextLine = input.nextLine();

                /* Write changes to the workbook */
                FileOutputStream out = new FileOutputStream(WORK_TO_BE_DONE_FILE_NAME);
                workbook.write(out);
                out.close();

                Long endTime = System.currentTimeMillis();
                System.out.println("Total time taken ======>"+ (endTime-startTime)/1000 +" Seconds");

        }



    public static void makeSound() throws LineUnavailableException {
        System.out.println("Make sound");
        byte[] buf = new byte[2];
        int frequency = 44100; //44100 sample points per 1 second
        AudioFormat af = new AudioFormat((float) frequency, 16, 1, true, false);
        SourceDataLine sdl = AudioSystem.getSourceDataLine(af);
        sdl.open();
        sdl.start();
        int durationMs = 5000;
        int numberOfTimesFullSinFuncPerSec = 441; //number of times in 1sec sin function repeats
        for (int i = 0; i < durationMs * (float) 44100 / 1000; i++) { //1000 ms in 1 second
            float numberOfSamplesToRepresentFullSin= (float) frequency / numberOfTimesFullSinFuncPerSec;
            double angle = i / (numberOfSamplesToRepresentFullSin/ 2.0) * Math.PI;  // /divide with 2 since sin goes 0PI to 2PI
            short a = (short) (Math.sin(angle) * 32767);  //32767 - max value for sample to take (-32767 to 32767)
            buf[0] = (byte) (a & 0xFF); //write 8bits ________WWWWWWWW out of 16
            buf[1] = (byte) (a >> 8); //write 8bits WWWWWWWW________ out of 16
            sdl.write(buf, 0, 2);
        }
        sdl.drain();
        sdl.stop();
    }
}