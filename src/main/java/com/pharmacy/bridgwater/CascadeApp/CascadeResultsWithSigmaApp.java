package com.pharmacy.bridgwater.CascadeApp;

import com.pharmacy.bridgwater.CascadeApp.model.ActualSupplierData;
import com.pharmacy.bridgwater.CascadeApp.model.OrderListKey;
import com.pharmacy.bridgwater.CascadeApp.service.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
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


public class CascadeResultsWithSigmaApp {

        public static final int ORDER_LIST_DESC_CELL = 0; public static final int CASCADE_DESC_CELL = 7; public static final int QUANTITY_CELL = 4;
        public static final int FROM_CELL=5; public static final int NOTES_CELL=6;public static final int TARRIF_CELL=8; public static final int TARIFF_AFTER_DEDUCTION_CELL=9;
        public static final int CONCESSION_CELL=10; public static final int ORDER_LIST_PIP_CODE_CELL=11; public static final int AAH_PRICE_CELL=12;public static final int AAH_PIP_CELL=20;
        public static final int BESTWAY_PRICE_CELL=13;public static final int BESTWAY_PIP_CELL=21;public static final int BNS_PRICE_CELL=14;public static final int BNS_PIP_CELL=22;
        public static final int LEXON_PRICE_CELL=15;public static final int LEXON_PIP_CELL=23;public static final int OTC_PRICE_CELL=16;public static final int OTC_PIP_CELL=24;
        public static final int SIGMA_PRICE_CELL = 17; public static final int SIGMA_PIP_CELL=25;
        public static final int TRIDENT_PRICE_CELL=18;public static final int TRIDENT_PIP_CELL=26;public static final int ALLIANCE_PRICE_CELL=19;
        public static final int ALLIANCE_PIP_CELL=27;public static final int LOOKED_UP_AT=28;

        public static final String ORIGINAL_FILE_NAME = "/Users/juppala/MyNewWorkspace/CascadeApplication/src/main/resources/Supp codes.xlsx";
        public static final String COPIED_FILE_NAME = "/Users/juppala/MyNewWorkspace/CascadeApplication/src/main/resources/Supp codes_copy.xlsx";


        public static void main(String[] args) throws Exception{
                Long startTime = System.currentTimeMillis();

               // File original = new File(ORIGINAL_FILE_NAME);
               // File copied = new File(COPIED_FILE_NAME);
                //FileUtils.copyFile(original, copied);

                //updating the file with the results
                Workbook workbook = new XSSFWorkbook(ORIGINAL_FILE_NAME);
                //XSSFSheet my_sheet = workbook.createSheet("Cascade Results"+LocalDateTime.now().getDayOfMonth()+"-"+ LocalDateTime.now().getMonth()+"-"+ LocalDateTime.now().getYear());
                Sheet my_sheet = workbook.getSheetAt(0);

                // normalFont
                CellStyle normalFontStyle = workbook.createCellStyle();
                Font normalFont=workbook.createFont();
                normalFont.setColor(IndexedColors.BLACK.getIndex());
                normalFontStyle.setFont(normalFont);

                // redFont
                CellStyle redFontStyle = workbook.createCellStyle();
                Font redFont=workbook.createFont();
                redFont.setColor(IndexedColors.RED.getIndex());
                redFontStyle.setFont(redFont);

                //Green font
                CellStyle greenFontStyle = workbook.createCellStyle();
                Font greenFont=workbook.createFont();
                greenFont.setColor(IndexedColors.GREEN.getIndex());
                greenFontStyle.setFont(greenFont);

                //Green Bold font
                CellStyle greenBoldFontStyle = workbook.createCellStyle();
                greenBoldFontStyle.setFillForegroundColor(IndexedColors.YELLOW1.getIndex());
                greenBoldFontStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                Font greenBoldFont=workbook.createFont();
                greenBoldFont.setBold(true);
                greenBoldFont.setColor(IndexedColors.GREEN.getIndex());
                greenBoldFontStyle.setFont(greenBoldFont);

                //Orange font
                CellStyle orangeFontStyle = workbook.createCellStyle();
                Font orangeFont=workbook.createFont();
                orangeFont.setColor(IndexedColors.ORANGE.getIndex());
                orangeFontStyle.setFont(orangeFont);


                CascadeServiceFromOrderList cascade = new CascadeServiceFromOrderList();
                Map<OrderListKey, Set<ActualSupplierData>> cascadeResults =  cascade.getCascadeResult();
                Map<OrderListKey, Set<String>> sigmaPipCodes =  new LinkedHashMap<>();
                for (Map.Entry<OrderListKey, Set<ActualSupplierData>> entry : cascadeResults.entrySet()) {
                        sigmaPipCodes.put(entry.getKey(), entry.getValue().stream().filter(v -> !StringUtils.isEmpty(v.getCode())).map(ActualSupplierData::getCode).collect(Collectors.toSet()));
                }



                ExecutorService executor = Executors.newFixedThreadPool(3);


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
                                .filter(v-> v.getCascadePrice()!=null)
                                .min(Comparator.comparing(ActualSupplierData::getCascadePrice)
                                )
                                .orElse(null);


                        ActualSupplierData cheaperBestwayData = value.stream().filter(v-> SUPPLIER_BESTWAY.equalsIgnoreCase(v.getSupplier()))
                                .filter(v-> v.getCascadePrice()!=null)
                                .min(Comparator.comparing(ActualSupplierData::getCascadePrice)
                                )
                                .orElse(null);

                        ActualSupplierData cheaperBnsData = value.stream().filter(v-> SUPPLIER_BNS.equalsIgnoreCase(v.getSupplier()))
                                .filter(v-> v.getCascadePrice()!=null)
                                .min(Comparator.comparing(ActualSupplierData::getCascadePrice)
                                )
                                .orElse(null);

                        ActualSupplierData cheaperLexonData = value.stream().filter(v-> SUPPLIER_LEXON.equalsIgnoreCase(v.getSupplier()))
                                .filter(v-> v.getCascadePrice()!=null)
                                .min(Comparator.comparing(ActualSupplierData::getCascadePrice)
                                )
                                .orElse(null);

                        ActualSupplierData cheaperSigmaData = value.stream().filter(v-> SUPPLIER_SIGMA.equalsIgnoreCase(v.getSupplier()))
                                .filter(v-> v.getCascadePrice()!=null)
                                .min(Comparator.comparing(ActualSupplierData::getCascadePrice)
                                )
                                .orElse(null);

                        ActualSupplierData cheaperTridentData = value.stream()
                                .filter(v-> v.getCascadePrice()!=null)
                                .filter(v-> SUPPLIER_TRIDENT.equalsIgnoreCase(v.getSupplier()))
                                .min(Comparator.comparing(ActualSupplierData::getCascadePrice)
                                )
                                .orElse(null);

                        ActualSupplierData cheaperAllianceData = value.stream().filter(v-> SUPPLIER_ALLIANCE.equalsIgnoreCase(v.getSupplier()))
                                .filter(v-> v.getCascadePrice()!=null)
                                .min(Comparator.comparing(ActualSupplierData::getCascadePrice)
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
                                .min(Comparator.comparing(ActualSupplierData::getCascadePrice))
                                .stream().findAny().orElse(null);

                        Row row = my_sheet.getRow(key.getSno());


                        //Order list desc
                        Cell cell0 = row.createCell(ORDER_LIST_DESC_CELL);
                        cell0.setCellValue(key.getOrderListDesc());
                        cell0.setCellStyle(normalFontStyle);

                        //Cascade Desc
                        Cell cell1 = row.createCell(CASCADE_DESC_CELL);
                        if(cheaperPrice!=null && cheaperPrice.getTariff()!=null){
                                cell1.setCellValue(cheaperPrice.getDescription());
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
                        if(cheaperPrice!=null && cheaperPrice.getTariff()!=null){
                                cell5.setCellValue(cheaperPrice.getTariff());
                        }else{
                                cell5.setCellValue("");
                        }
                        cell5.setCellStyle(normalFontStyle);

                        //TariffAfterDeduction
                        Cell cell6 = row.createCell(TARIFF_AFTER_DEDUCTION_CELL);
                        if(cheaperPrice!=null && cheaperPrice.getTariffAfterDeduction()!=null){
                                cell6.setCellValue(cheaperPrice.getTariffAfterDeduction());
                        }else{
                                cell6.setCellValue("");
                        }
                        cell6.setCellStyle(normalFontStyle);

                        //concession
                        Cell cell7 = row.createCell(CONCESSION_CELL);
                        if(cheaperPrice!=null && cheaperPrice.getConcession()!=null){
                                cell7.setCellValue(cheaperPrice.getConcession());
                        }else {
                                cell7.setCellValue("");
                        }
                        cell7.setCellStyle(normalFontStyle);
                        //ordercode
                        Cell cell8 = row.createCell(ORDER_LIST_PIP_CODE_CELL);
                        cell8.setCellValue(key.getOrderListPipCode());
                        cell8.setCellStyle(normalFontStyle);

                        /*//AAH price
                        Cell cell9 = row.createCell(AAH_PRICE_CELL);
                        //If both are null, ie. its not stocked
                        if(null != cheaperAahData && null != cheaperPrice ){
                                //if cheaper is aah, and available then yellow back with green -- greenBoldFontStyle
                                //if aah stock available then green, if stock not available then red
                                if(null != cheaperAahData.getCascadeStatus() && SUPPLIER_STATUS_AVAILABLE.equalsIgnoreCase(cheaperAahData.getCascadeStatus())
                                        && null != cheaperAahData.getCascadePrice()
                                        && null != cheaperPrice.getCascadePrice()
                                        && cheaperPrice.getCascadePrice().compareTo(cheaperAahData.getCascadePrice()) == 0){
                                        cell9.setCellValue(cheaperAahData.getCascadePrice());
                                        cell9.setCellStyle(greenBoldFontStyle);
                                }else{
                                        if(null != cheaperAahData.getCascadeStatus() && SUPPLIER_STATUS_AVAILABLE.equalsIgnoreCase(cheaperAahData.getCascadeStatus())){
                                                cell9.setCellValue(cheaperAahData.getCascadePrice());
                                                cell9.setCellStyle(greenFontStyle);
                                        }else{
                                                cell9.setCellValue(cheaperAahData.getCascadePrice());
                                                cell9.setCellStyle(redFontStyle);
                                        }

                                }
                        }else{
                                cell9.setCellValue("NS");
                        }*/

                        //AAH price
                        Cell cell9 = row.createCell(AAH_PRICE_CELL);
                        //If both are null, ie. its not stocked
                        if(null != cheaperAahData){
                                if(null != cheaperAahData.getCascadeStatus() && SUPPLIER_STATUS_AVAILABLE.equalsIgnoreCase(cheaperAahData.getCascadeStatus())){
                                        cell9.setCellValue(cheaperAahData.getCascadePrice());
                                        cell9.setCellStyle(greenFontStyle);
                                }else{
                                        cell9.setCellValue(cheaperAahData.getCascadePrice());
                                        cell9.setCellStyle(redFontStyle);
                                }
                        }else{
                                cell9.setCellValue("NS");
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
                                if(null != cheaperBestwayData.getCascadeStatus() && SUPPLIER_STATUS_AVAILABLE.equalsIgnoreCase(cheaperBestwayData.getCascadeStatus())){
                                        cell11.setCellValue(cheaperBestwayData.getCascadePrice());
                                        cell11.setCellStyle(greenFontStyle);
                                }else{
                                        cell11.setCellValue(cheaperBestwayData.getCascadePrice());
                                        cell11.setCellStyle(redFontStyle);
                                }
                        }else{
                                cell11.setCellValue("NS");
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
                                if(null != cheaperBnsData.getCascadeStatus() && SUPPLIER_STATUS_AVAILABLE.equalsIgnoreCase(cheaperBnsData.getCascadeStatus())){
                                        cell13.setCellValue(cheaperBnsData.getCascadePrice());
                                        cell13.setCellStyle(greenFontStyle);
                                }else{
                                        cell13.setCellValue(cheaperBnsData.getCascadePrice());
                                        cell13.setCellStyle(redFontStyle);
                                }
                        }else{
                                cell13.setCellValue("NS");
                        }

                        //BNS pip
                        Cell cell14 = row.createCell(BNS_PIP_CELL);
                        //If both are null, ie. its not stocked
                        if(null != cheaperBnsData  ){
                                //get the cheaper BNS's pip
                                cell14.setCellValue(cheaperBnsData.getCode());
                        }else{
                                cell14.setCellValue("NA");
                        }

                        //Lexon
                        Cell cell15 = row.createCell(LEXON_PRICE_CELL);
                        //If both are null, ie. its not stocked
                        if(null != cheaperLexonData && null != cheaperPrice ){
                                if(null != cheaperLexonData.getCascadeStatus() && SUPPLIER_STATUS_AVAILABLE.equalsIgnoreCase(cheaperLexonData.getCascadeStatus())){
                                        cell15.setCellValue(cheaperLexonData.getCascadePrice());
                                        cell15.setCellStyle(greenFontStyle);
                                }else{
                                        cell15.setCellValue(cheaperLexonData.getCascadePrice());
                                        cell15.setCellStyle(redFontStyle);
                                }
                        }else{
                                cell15.setCellValue("NS");
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
                        Cell cell17 = row.createCell(OTC_PRICE_CELL);
                        cell17.setCellValue("");
                        cell17.setCellStyle(normalFontStyle);

                        //OTC PIP
                        Cell cell18 = row.createCell(OTC_PIP_CELL);
                        cell18.setCellValue("");
                        cell18.setCellStyle(normalFontStyle);

                        //Sigma
                        Cell cell19 = row.createCell(SIGMA_PRICE_CELL);
                        //If both are null, ie. its not stocked
                        if(null != cheaperSigmaData && null != cheaperPrice ){
                                //if cheaper is aah, and available then yellow back with green -- greenBoldFontStyle
                                //if aah stock available then green, if stock not available then red
                                if(null != cheaperSigmaData.getCascadeStatus() && SUPPLIER_STATUS_AVAILABLE.equalsIgnoreCase(cheaperSigmaData.getCascadeStatus())){
                                        cell19.setCellValue(cheaperSigmaData.getCascadePrice());
                                        cell19.setCellStyle(greenFontStyle);
                                }else{
                                        cell19.setCellValue(cheaperSigmaData.getCascadePrice());
                                        cell19.setCellStyle(redFontStyle);
                                }
                        }else{
                                cell19.setCellValue("NS");
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
                                if(null != cheaperTridentData.getCascadeStatus() && SUPPLIER_STATUS_AVAILABLE.equalsIgnoreCase(cheaperTridentData.getCascadeStatus())){
                                        cell21.setCellValue(cheaperTridentData.getCascadePrice());
                                        cell21.setCellStyle(greenFontStyle);
                                }else{
                                        cell21.setCellValue(cheaperTridentData.getCascadePrice());
                                        cell21.setCellStyle(redFontStyle);
                                }
                        }else{
                                cell21.setCellValue("NS");
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
                                if(null != cheaperAllianceData.getCascadeStatus() && SUPPLIER_STATUS_AVAILABLE.equalsIgnoreCase(cheaperAllianceData.getCascadeStatus())){
                                        cell23.setCellValue(cheaperAllianceData.getCascadePrice());
                                        cell23.setCellStyle(greenFontStyle);
                                }else{
                                        cell23.setCellValue(cheaperAllianceData.getCascadePrice());
                                        cell23.setCellStyle(redFontStyle);
                                }
                        }else{
                                cell23.setCellValue("NS");
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

                        Cell cell25 = row.createCell(LOOKED_UP_AT);
                        LocalDateTime now = LocalDateTime.now();
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                        String formattedDateTime = now.format(formatter);
                        cell25.setCellValue(formattedDateTime);
                        cell25.setCellStyle(normalFontStyle);


                }

                /* Write changes to the workbook */
                FileOutputStream out = new FileOutputStream(ORIGINAL_FILE_NAME);
                workbook.write(out);
                out.close();

                Long endTime = System.currentTimeMillis();
                System.out.println("Total time taken ======>"+ (endTime-startTime)/1000 +" Seconds");

        }
}