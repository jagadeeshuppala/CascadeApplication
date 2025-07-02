package com.pharmacy.bridgwater.CascadeApp;

import com.pharmacy.bridgwater.CascadeApp.model.ActualSupplierData;
import com.pharmacy.bridgwater.CascadeApp.model.OrderListKey;
import com.pharmacy.bridgwater.CascadeApp.service.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

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


public class Server {


        //In Pharmacy

        public static final String ORIGINAL_FILE_NAME = "\\\\11701279QSVR\\PSSharedarea\\Bridgwater\\Miscellaneous\\OrderList1.xlsx";
        public static final String WORK_TO_BE_DONE_FILE_NAME = "\\\\11701279QSVR\\PSSharedarea\\Bridgwater\\Miscellaneous\\OrderList1.xlsx";
        public static final String COPIED_FILE_NAME = "\\\\11701279QSVR\\PSSharedarea\\Bridgwater\\Miscellaneous\\OrderList-JagCopy.xlsx";

        //At Home
        /*public static final String ORIGINAL_FILE_NAME = "C:\\Users\\msola\\OneDrive\\Desktop\\OrderList1.xlsx";
        public static final String WORK_TO_BE_DONE_FILE_NAME = "C:\\Users\\msola\\OneDrive\\Desktop\\OrderList1.xlsx";
        public static final String COPIED_FILE_NAME = "C:\\Users\\msola\\OneDrive\\Desktop\\OrderList_Copy_copy.xlsx";*/

        public static final String CASCADE_UPLOAD_FILE_NAME = "upload.csv";
        public static final String CASCADE_UPLOAD_FILE_NAME_WITH_ORDER_LIST_SNO = "mapping.txt";


        public static final int ORDER_LIST_DESC = 0;
        public static final int ORDER_LIST_PIP = 1;
        public static final int ORDER_LIST_QTY = 3;
        public static final int ORDER_LIST_FROM = 4;

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
                Sheet my_sheet = workbook.getSheetAt(0);

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
                Map<OrderListKey, Set<ActualSupplierData>> cascadeResults =  cascade.getCascadeResult(WORK_TO_BE_DONE_FILE_NAME);
                Map<OrderListKey, Set<String>> sigmaPipCodes =  new LinkedHashMap<>();
                for (Map.Entry<OrderListKey, Set<ActualSupplierData>> entry : cascadeResults.entrySet()) {
                        sigmaPipCodes.put(entry.getKey(), entry.getValue().stream().filter(v -> !StringUtils.isEmpty(v.getCode())).map(ActualSupplierData::getCode).collect(Collectors.toSet()));
                }



                ExecutorService executor = Executors.newFixedThreadPool(4);


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


                executor.shutdown();

                Map<OrderListKey, Set<ActualSupplierData>> aahProcessedResults = aahFuture.get();
                Map<OrderListKey, Set<ActualSupplierData>> tridentProcessedResults = tridentFuture.get();
                Map<OrderListKey, Set<ActualSupplierData>> bestwayProcessedResults = bestwayFuture.get();
                Map<OrderListKey, Set<ActualSupplierData>> sigmaProcessedResults = sigmaFuture.get();



                System.out.println(cascadeResults);

                //replace cascade results with aahProcessed results
                for (Map.Entry<OrderListKey, Set<ActualSupplierData>> entry : cascadeResults.entrySet()) {
                        OrderListKey key = entry.getKey();

                        //remove

                        // add sigma processed results
                        entry.getValue().addAll(sigmaProcessedResults.get(key));
                }

                System.out.println(cascadeResults);




                /*Row row0 = my_sheet.createRow(0);
                Cell cell0_1 = row0.createCell(ORDER_LIST_DESC_CELL); cell0_1.setCellValue("OrderList Desc"); cell0_1.setCellStyle(normalFontStyle);
                Cell cell0_2 = row0.createCell(CASCADE_DESC_CELL); cell0_2.setCellValue("cascade Desc"); cell0_2.setCellStyle(normalFontStyle);
                Cell cell0_3 = row0.createCell(QUANTITY_CELL); cell0_3.setCellValue("Quantity"); cell0_3.setCellStyle(normalFontStyle);
                Cell cell0_4 = row0.createCell(FROM_CELL); cell0_4.setCellValue("From"); cell0_4.setCellStyle(normalFontStyle);
                Cell cell0_5 = row0.createCell(NOTES_CELL); cell0_5.setCellValue("Notes"); cell0_5.setCellStyle(normalFontStyle);
                Cell cell0_6 = row0.createCell(TARRIF_CELL); cell0_6.setCellValue("Tariff"); cell0_6.setCellStyle(normalFontStyle);
                Cell cell0_7 = row0.createCell(TARIFF_AFTER_DEDUCTION_CELL); cell0_7.setCellValue("DTnet"); cell0_6.setCellStyle(normalFontStyle);
                Cell cell0_8 = row0.createCell(CONCESSION_CELL); cell0_8.setCellValue("Concession"); cell0_8.setCellStyle(normalFontStyle);
                Cell cell0_9 = row0.createCell(ORDER_LIST_PIP_CODE_CELL); cell0_9.setCellValue("Order list pip"); cell0_9.setCellStyle(normalFontStyle);
                Cell cell0_10 = row0.createCell(AAH_PRICE_CELL); cell0_10.setCellValue("AAH "); cell0_10.setCellStyle(normalFontStyle);
                Cell cell0_11 = row0.createCell(AAH_PIP_CELL); cell0_11.setCellValue("AAH Pip"); cell0_11.setCellStyle(normalFontStyle);
                Cell cell0_12 = row0.createCell(BESTWAY_PRICE_CELL); cell0_12.setCellValue("Bestway"); cell0_12.setCellStyle(normalFontStyle);
                Cell cell0_13 = row0.createCell(BESTWAY_PIP_CELL); cell0_13.setCellValue("Bestway pip"); cell0_13.setCellStyle(normalFontStyle);
                Cell cell0_14 = row0.createCell(BNS_PRICE_CELL); cell0_14.setCellValue("BNS"); cell0_14.setCellStyle(normalFontStyle);
                Cell cell0_15 = row0.createCell(BNS_PIP_CELL); cell0_15.setCellValue("BNS Pip"); cell0_15.setCellStyle(normalFontStyle);
                Cell cell0_16 = row0.createCell(LEXON_PRICE_CELL); cell0_16.setCellValue("Lexon"); cell0_16.setCellStyle(normalFontStyle);
                Cell cell0_17 = row0.createCell(LEXON_PIP_CELL); cell0_17.setCellValue("Lexon Pip"); cell0_17.setCellStyle(normalFontStyle);
                Cell cell0_18 = row0.createCell(OTC_PRICE_CELL); cell0_18.setCellValue("OTC"); cell0_18.setCellStyle(normalFontStyle);
                Cell cell0_19 = row0.createCell(OTC_PIP_CELL); cell0_19.setCellValue("OTC Pip"); cell0_19.setCellStyle(normalFontStyle);
                Cell cell0_20 = row0.createCell(SIGMA_PRICE_CELL); cell0_20.setCellValue("Sigma"); cell0_20.setCellStyle(normalFontStyle);
                Cell cell0_21 = row0.createCell(SIGMA_PIP_CELL); cell0_21.setCellValue("Sigma Pip"); cell0_21.setCellStyle(normalFontStyle);
                Cell cell0_22 = row0.createCell(TRIDENT_PRICE_CELL); cell0_22.setCellValue("Trident Pip"); cell0_22.setCellStyle(normalFontStyle);
                Cell cell0_23 = row0.createCell(TRIDENT_PIP_CELL); cell0_23.setCellValue("Trident Pip"); cell0_23.setCellStyle(normalFontStyle);
                Cell cell0_24 = row0.createCell(ALLIANCE_PRICE_CELL); cell0_24.setCellValue("Alliance Price"); cell0_24.setCellStyle(normalFontStyle);
                Cell cell0_25 = row0.createCell(ALLIANCE_PIP_CELL); cell0_25.setCellValue("Alliance Pip"); cell0_25.setCellStyle(normalFontStyle);
                Cell cell0_26 = row0.createCell(LOOKED_UP_AT); cell0_26.setCellValue("Lookedup At"); cell0_26.setCellStyle(normalFontStyle);*/


                // iterate through the map to find out which one is least and add to the excel
                for (Map.Entry<OrderListKey, Set<ActualSupplierData>> entry : cascadeResults.entrySet()) {
                        OrderListKey key = entry.getKey();
                        Set<ActualSupplierData> value = entry.getValue();

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



                        Row row = my_sheet.getRow(key.getSno());


                        //Order list desc
                        Cell cell0 = row.createCell(ORDER_LIST_DESC_CELL);
                        cell0.setCellValue(key.getOrderListDesc());
                        cell0.setCellStyle(normalFontStyle);

                        //Cascade Desc
                        Cell cell1 = row.createCell(CASCADE_DESC_CELL);
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
                        Cell cell5 = row.createCell(TARRIF_CELL);
                        if(cascadeDataForProductDesc!=null && cascadeDataForProductDesc.getTariff()!=null){
                                cell5.setCellValue(cascadeDataForProductDesc.getTariff());
                        }else{
                                cell5.setCellValue("");
                        }
                        cell5.setCellStyle(normalFontStyle);

                        //TariffAfterDeduction
                        Cell cell6 = row.createCell(TARIFF_AFTER_DEDUCTION_CELL);
                        if(cascadeDataForProductDesc!=null && cascadeDataForProductDesc.getTariffAfterDeduction()!=null){
                                cell6.setCellValue(cascadeDataForProductDesc.getTariffAfterDeduction());
                        }else{
                                cell6.setCellValue("");
                        }
                        cell6.setCellStyle(normalFontStyle);

                        //concession
                        Cell cell7 = row.createCell(CONCESSION_CELL);
                        if(cascadeDataForProductDesc!=null && cascadeDataForProductDesc.getConcession()!=null){
                                cell7.setCellValue(cascadeDataForProductDesc.getConcession());
                        }else {
                                cell7.setCellValue("");
                        }
                        cell7.setCellStyle(normalFontStyle);
                        //ordercode
                        Cell cell8 = row.createCell(ORDER_LIST_PIP_CODE_CELL);
                        cell8.setCellValue(key.getOrderListPipCode());
                        cell8.setCellStyle(normalFontStyle);


                        //AAH price
                        Cell cell9 = row.createCell(AAH_PRICE_CELL);
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
                        }

                        //AAH pip
                        Cell cell10 = row.createCell(AAH_PIP_CELL);
                        //If both are null, ie. its not stocked
                        if(null != cheaperAahData  ){
                                //get the cheaper AAH's pip
                                cell10.setCellValue(cheaperAahData.getCode());
                        }else{
                                cell10.setCellValue("NA");
                        }

                        //Bestway
                        Cell cell11 = row.createCell(BESTWAY_PRICE_CELL);
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
                        }

                        //Bestway pip
                        Cell cell12 = row.createCell(BESTWAY_PIP_CELL);
                        //If both are null, ie. its not stocked
                        if(null != cheaperBestwayData  ){
                                //get the cheaper AAH's pip
                                cell12.setCellValue(cheaperBestwayData.getCode());
                        }else{
                                cell12.setCellValue("NA");
                        }

                        //BNS
                        Cell cell13 = row.createCell(BNS_PRICE_CELL);
                        //If both are null, ie. its not stocked
                        if(null != cheaperBnsData && null != cheaperPrice ){
                                //if cheaper is bns, and available then yellow back with green -- greenBoldFontStyle
                                //if bns stock available then green, if stock not available then red
                                if(null != cheaperBnsData.getStatus() && SUPPLIER_STATUS_AVAILABLE.equalsIgnoreCase(cheaperBnsData.getStatus())
                                        && null != cheaperBnsData.getPrice()
                                        && null != cheaperPrice.getPrice()
                                        && cheaperPrice.getPrice().compareTo(cheaperBnsData.getPrice()) == 0){
                                        cell13.setCellValue(cheaperBnsData.getPrice());
                                        cell13.setCellStyle(greenBoldFontStyle);
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
                        }

                        //BNS pip
                        Cell cell14 = row.createCell(BNS_PIP_CELL);
                        //If both are null, ie. its not stocked
                        if(null != cheaperBnsData  ){
                                //get the cheaper BNS's pip
                                cell14.setCellValue(cheaperBnsData.getCode());
                        }else{
                                //cell14.setCellValue("NA");
                        }

                        //Lexon
                        Cell cell15 = row.createCell(LEXON_PRICE_CELL);
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
                        }

                        //Lexon pip
                        Cell cell16 = row.createCell(LEXON_PIP_CELL);
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
                        Cell cell18 = row.createCell(OTC_PIP_CELL);
                        cell18.setCellValue("");
                        cell18.setCellStyle(normalFontStyle);

                        //Sigma
                        Cell cell19 = row.createCell(SIGMA_PRICE_CELL);
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
                        }

                        //Sigma pip
                        Cell cell20 = row.createCell(SIGMA_PIP_CELL);
                        //If both are null, ie. its not stocked
                        if(null != cheaperSigmaData  ){
                                //get the cheaper Sigma's pip
                                cell20.setCellValue(cheaperSigmaData.getCode());
                        }else{
                                cell20.setCellValue("NA");
                        }

                        //Trident
                        Cell cell21 = row.createCell(TRIDENT_PRICE_CELL);
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
                        }

                        //Trident pip
                        Cell cell22 = row.createCell(TRIDENT_PIP_CELL);
                        //If both are null, ie. its not stocked
                        if(null != cheaperTridentData  ){
                                //get the cheaper Trident's pip
                                cell22.setCellValue(cheaperTridentData.getCode());
                        }else{
                                cell22.setCellValue("NA");
                        }

                        //Alliance
                        Cell cell23 = row.createCell(ALLIANCE_PRICE_CELL);
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
                        }

                        //Alliance pip
                        Cell cell24 = row.createCell(ALLIANCE_PIP_CELL);
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
                        Cell cell26 = row.createCell(AAH_CASCADE_PRICE_CELL);
                        if(null !=cheaperAahData && null != cheaperAahData.getCascadePrice()){
                                cell26.setCellValue(cheaperAahData.getCascadePrice());
                                /*cell26.setCellStyle(accountingFontStyle);*/
                        }

                        //AAH Cascade status
                        Cell cell27 = row.createCell(AAH_CASCADE_STATUS_CELL);
                        if(null !=cheaperAahData && null != cheaperAahData.getCascadeStatus()){
                                cell27.setCellValue(cheaperAahData.getCascadeStatus());
                        }

                        //AAH Cascade PIP
                        Cell cell28 = row.createCell(AAH_CASCADE_PIP_CELL);
                        if(null !=cheaperAahData && null != cheaperAahData.getCascadeCode()){
                                cell28.setCellValue(cheaperAahData.getCascadeCode());
                        }

                        //
                        //Bestway Cascade price
                        Cell cell29 = row.createCell(BESTWAY_CASCADE_PRICE_CELL);
                        if(null !=cheaperBestwayData && null != cheaperBestwayData.getCascadePrice()){
                                cell29.setCellValue(cheaperBestwayData.getCascadePrice());
                        }

                        //Bestway Cascade status
                        Cell cell30 = row.createCell(BESTWAY_CASCADE_STATUS_CELL);
                        if(null !=cheaperBestwayData && null != cheaperBestwayData.getCascadeStatus()){
                                cell30.setCellValue(cheaperBestwayData.getCascadeStatus());
                        }

                        //Bestway Cascade PIP
                        Cell cell31 = row.createCell(BESTWAY_CASCADE_PIP_CELL);
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
                        Cell cell35 = row.createCell(TRIDENT_CASCADE_PRICE_CELL);
                        if(null !=cheaperTridentData && null != cheaperTridentData.getCascadePrice()){
                                cell35.setCellValue(cheaperTridentData.getCascadePrice());
                        }

                        //Trident Cascade status
                        Cell cell36 = row.createCell(TRIDENT_CASCADE_STATUS_CELL);
                        if(null !=cheaperTridentData && null != cheaperTridentData.getCascadeStatus()){
                                cell36.setCellValue(cheaperTridentData.getCascadeStatus());
                        }

                        //Trident Cascade PIP
                        Cell cell37 = row.createCell(TRIDENT_CASCADE_PIP_CELL);
                        if(null !=cheaperTridentData && null != cheaperTridentData.getCascadeCode()){
                                cell37.setCellValue(cheaperTridentData.getCascadeCode());
                        }

                        Cell cell25 = row.createCell(LOOKED_UP_AT);
                        LocalDateTime now = LocalDateTime.now();
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM HH:mm");
                        String formattedDateTime = now.format(formatter);
                        cell25.setCellValue(formattedDateTime);
                        cell25.setCellStyle(normalFontStyle);


                }

                /* Write changes to the workbook */
                FileOutputStream out = new FileOutputStream(WORK_TO_BE_DONE_FILE_NAME);
                workbook.write(out);
                out.close();

                Long endTime = System.currentTimeMillis();
                System.out.println("Total time taken ======>"+ (endTime-startTime)/1000 +" Seconds");

        }
}