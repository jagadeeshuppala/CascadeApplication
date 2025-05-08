package com.pharmacy.bridgwater.CascadeApp;

import com.pharmacy.bridgwater.CascadeApp.model.ActualSupplierData;
import com.pharmacy.bridgwater.CascadeApp.service.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
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


        public static void main(String[] args) throws Exception{
                Long startTime = System.currentTimeMillis();


                /* Create Workbook and Worksheet XLSX Format */
                XSSFWorkbook workbook = new XSSFWorkbook();
                XSSFSheet my_sheet = workbook.createSheet("Cascade Results"+LocalDateTime.now().getDayOfMonth()+"-"+ LocalDateTime.now().getMonth()+"-"+ LocalDateTime.now().getYear());

                // normalFont
                XSSFCellStyle normalFontStyle = workbook.createCellStyle();
                XSSFFont normalFont=workbook.createFont();
                normalFont.setColor(IndexedColors.BLACK.getIndex());
                normalFontStyle.setFont(normalFont);

                // redFont
                XSSFCellStyle redFontStyle = workbook.createCellStyle();
                XSSFFont redFont=workbook.createFont();
                redFont.setColor(IndexedColors.RED.getIndex());
                redFontStyle.setFont(redFont);

                //Green font
                XSSFCellStyle greenFontStyle = workbook.createCellStyle();
                XSSFFont greenFont=workbook.createFont();
                greenFont.setColor(IndexedColors.GREEN.getIndex());
                greenFontStyle.setFont(greenFont);

                //Green Bold font
                XSSFCellStyle greenBoldFontStyle = workbook.createCellStyle();
                greenBoldFontStyle.setFillForegroundColor(IndexedColors.YELLOW1.getIndex());
                greenBoldFontStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                XSSFFont greenBoldFont=workbook.createFont();
                greenBoldFont.setBold(true);
                greenBoldFont.setColor(IndexedColors.GREEN.getIndex());
                greenBoldFontStyle.setFont(greenBoldFont);

                //Orange font
                XSSFCellStyle orangeFontStyle = workbook.createCellStyle();
                XSSFFont orangeFont=workbook.createFont();
                orangeFont.setColor(IndexedColors.ORANGE.getIndex());
                orangeFontStyle.setFont(orangeFont);


                CascadeService cascade = new CascadeService();
                Map<String, Set<ActualSupplierData>> cascadeResults =  cascade.getCascadeResults();
                Map<String, Set<String>> sigmaPipCodes =  new LinkedHashMap<>();
                for (Map.Entry<String, Set<ActualSupplierData>> entry : cascadeResults.entrySet()) {
                        sigmaPipCodes.put(entry.getKey(), entry.getValue().stream().filter(v -> !StringUtils.isEmpty(v.getCode())).map(ActualSupplierData::getCode).collect(Collectors.toSet()));
                }



                ExecutorService executor = Executors.newFixedThreadPool(3);


                //Filter aahResults and pass it to Aah service to fetch the aah results
                Map<String, Set<ActualSupplierData>> unprocessedAahResults = new LinkedHashMap<>();
                for (Map.Entry<String, Set<ActualSupplierData>> entry : cascadeResults.entrySet()) {
                        String key = entry.getKey();
                        Set<ActualSupplierData> onlyAahUnprocessedSet = entry.getValue().stream().filter(v -> !StringUtils.isEmpty(v.getCode()))
                                .filter( v -> SUPPLIER_AAH.equalsIgnoreCase(v.getSupplier()))
                                .collect(Collectors.toSet());
                        unprocessedAahResults.put(key, onlyAahUnprocessedSet);
                }

                //Filter tridentResults and pass it to Trident service to fetch the aah results
                Map<String, Set<ActualSupplierData>> unprocessedTridentResults = new LinkedHashMap<>();
                for (Map.Entry<String, Set<ActualSupplierData>> entry : cascadeResults.entrySet()) {
                        String key = entry.getKey();
                        Set<ActualSupplierData> onlyTridentUnprocessedSet = entry.getValue().stream().filter(v -> !StringUtils.isEmpty(v.getCode()))
                                .filter( v -> SUPPLIER_TRIDENT.equalsIgnoreCase(v.getSupplier()))
                                .collect(Collectors.toSet());
                        unprocessedTridentResults.put(key, onlyTridentUnprocessedSet);
                }

                //Filter bestway results and pass it to Bestway service to fetch the bestway results
                Map<String, Set<ActualSupplierData>> unprocessedBestwayResults = new LinkedHashMap<>();
                for (Map.Entry<String, Set<ActualSupplierData>> entry : cascadeResults.entrySet()) {
                        String key = entry.getKey();
                        Set<ActualSupplierData> onlyBestwayUnprocessedSet = entry.getValue().stream().filter(v -> !StringUtils.isEmpty(v.getCode()))
                                .filter( v -> SUPPLIER_BESTWAY.equalsIgnoreCase(v.getSupplier()))
                                .collect(Collectors.toSet());
                        unprocessedBestwayResults.put(key, onlyBestwayUnprocessedSet);
                }

                Callable<Map<String, Set<ActualSupplierData>>> aahWorker = new AahProcessService(unprocessedAahResults);
                Future<Map<String, Set<ActualSupplierData>>> aahFuture = executor.submit(aahWorker);


                Callable<Map<String, Set<ActualSupplierData>>> tridentWorker = new TridentProcessService(unprocessedTridentResults);
                Future<Map<String, Set<ActualSupplierData>>> tridentFuture = executor.submit(tridentWorker);

                Callable<Map<String, Set<ActualSupplierData>>> bestwayWorker = new BestwayProcessService(unprocessedTridentResults);
                Future<Map<String, Set<ActualSupplierData>>> bestwayFuture = executor.submit(bestwayWorker);


                Callable<Map<String, Set<ActualSupplierData>>> sigmaWorker = new SigmaProcessService(sigmaPipCodes);
                Future<Map<String, Set<ActualSupplierData>>>  sigmaFuture = executor.submit(sigmaWorker);


                executor.shutdown();

                Map<String, Set<ActualSupplierData>> aahProcessedResults = aahFuture.get();
                Map<String, Set<ActualSupplierData>> tridentProcessedResults = tridentFuture.get();
                Map<String, Set<ActualSupplierData>> bestwayProcessedResults = bestwayFuture.get();
                Map<String, Set<ActualSupplierData>> sigmaProcessedResults = sigmaFuture.get();








                /*SigmaProcessService sigmaProcessService = new SigmaProcessService(cascadeResults);
                Map<String, Set<ActualSupplierData>> sigmaOnlyResults = sigmaProcessService.call();*/

                System.out.println(cascadeResults);

                //replace cascade results with aahProcessed results
                for (Map.Entry<String, Set<ActualSupplierData>> entry : cascadeResults.entrySet()) {
                        String key = entry.getKey();

                        //remove

                        // add sigma processed results
                        entry.getValue().addAll(sigmaProcessedResults.get(key));
                }

                System.out.println(cascadeResults);

                //Adding sigma results to cascadeResults
                /*for (Map.Entry<String, Set<ActualSupplierData>> entry : cascadeResults.entrySet()) {
                        String key = entry.getKey();
                        Set<ActualSupplierData> sigmaResults = sigmaOnlyResults.get(key);
                        Set<ActualSupplierData> value = entry.getValue();
                        //Adding sigma results
                        value.addAll(sigmaResults);
                }*/

                int i =0;
                Row row0 = my_sheet.createRow(0);

                Cell cell0_1 = row0.createCell(0); cell0_1.setCellValue("Description"); cell0_1.setCellStyle(normalFontStyle);
                Cell cell0_2 = row0.createCell(1); cell0_2.setCellValue("Quantity"); cell0_2.setCellStyle(normalFontStyle);
                Cell cell0_3 = row0.createCell(2); cell0_3.setCellValue("From"); cell0_3.setCellStyle(normalFontStyle);
                Cell cell0_4 = row0.createCell(3); cell0_4.setCellValue("Notes"); cell0_4.setCellStyle(normalFontStyle);
                Cell cell0_5 = row0.createCell(4); cell0_5.setCellValue("Tariff"); cell0_5.setCellStyle(normalFontStyle);
                Cell cell0_6 = row0.createCell(5); cell0_6.setCellValue("DTNet"); cell0_6.setCellStyle(normalFontStyle);
                Cell cell0_7 = row0.createCell(6); cell0_7.setCellValue("Concession"); cell0_6.setCellStyle(normalFontStyle);
                Cell cell0_8 = row0.createCell(7); cell0_8.setCellValue("OrderCode"); cell0_8.setCellStyle(normalFontStyle);
                Cell cell0_9 = row0.createCell(8); cell0_9.setCellValue("AAH"); cell0_9.setCellStyle(normalFontStyle);
                Cell cell0_10 = row0.createCell(9); cell0_10.setCellValue("Bestway"); cell0_10.setCellStyle(normalFontStyle);
                Cell cell0_11 = row0.createCell(10); cell0_11.setCellValue("Bns"); cell0_11.setCellStyle(normalFontStyle);
                Cell cell0_12 = row0.createCell(11); cell0_12.setCellValue("Lexon"); cell0_12.setCellStyle(normalFontStyle);
                Cell cell0_13 = row0.createCell(12); cell0_13.setCellValue("OTC"); cell0_13.setCellStyle(normalFontStyle);
                Cell cell0_14 = row0.createCell(13); cell0_14.setCellValue("Sigma"); cell0_14.setCellStyle(normalFontStyle);
                Cell cell0_15 = row0.createCell(14); cell0_15.setCellValue("Trident"); cell0_15.setCellStyle(normalFontStyle);
                Cell cell0_16 = row0.createCell(15); cell0_16.setCellValue("Alliance"); cell0_16.setCellStyle(normalFontStyle);
                Cell cell0_17 = row0.createCell(16); cell0_17.setCellValue("LookedupAt"); cell0_17.setCellStyle(normalFontStyle);
                Cell cell0_18 = row0.createCell(17); cell0_18.setCellValue("SNo"); cell0_18.setCellStyle(normalFontStyle);


                // iterate through the map to find out which one is least and add to the excel
                for (Map.Entry<String, Set<ActualSupplierData>> entry : cascadeResults.entrySet()) {
                        i++;
                        String key = entry.getKey();
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

                        Row row = my_sheet.createRow(i);
                        //Desc
                        Cell cell0 = row.createCell(0);
                        cell0.setCellValue(key);
                        cell0.setCellStyle(normalFontStyle);

                        //quantity
                        Cell cell1 = row.createCell(1);
                        if(cheaperPrice!=null && cheaperPrice.getQuantity()!=null){
                                cell1.setCellValue(cheaperPrice.getQuantity());
                        }else{
                                cell1.setCellValue("");
                        }
                        cell1.setCellStyle(normalFontStyle);


                        //from
                        Cell cell2 = row.createCell(2);
                        cell2.setCellValue("");
                        cell2.setCellStyle(normalFontStyle);

                        //notes
                        Cell cell3 = row.createCell(3);
                        cell3.setCellValue("");
                        cell3.setCellStyle(normalFontStyle);

                        //Tariff
                        Cell cell4 = row.createCell(4);
                        if(cheaperPrice!=null && cheaperPrice.getTariff()!=null){
                                cell4.setCellValue(cheaperPrice.getTariff());
                        }else{
                                cell4.setCellValue("");
                        }
                        cell4.setCellStyle(normalFontStyle);

                        //TariffAfterDeduction
                        Cell cell5 = row.createCell(5);
                        if(cheaperPrice!=null && cheaperPrice.getTariffAfterDeduction()!=null){
                                cell5.setCellValue(cheaperPrice.getTariffAfterDeduction());
                        }else{
                                cell5.setCellValue("");
                        }
                        cell5.setCellStyle(normalFontStyle);
                        //concession
                        Cell cell6 = row.createCell(6);
                        if(cheaperPrice!=null && cheaperPrice.getConcession()!=null){
                                cell6.setCellValue(cheaperPrice.getConcession());
                        }else {
                                cell6.setCellValue("");
                        }
                        cell6.setCellStyle(normalFontStyle);
                        //ordercode
                        Cell cell7 = row.createCell(7);
                        if(cheaperPrice!=null && cheaperPrice.getCode()!=null){
                                cell7.setCellValue(cheaperPrice.getCode());
                        }else{
                                cell7.setCellValue("");
                        }
                        cell7.setCellStyle(normalFontStyle);

                        //AAH
                        Cell cell8 = row.createCell(8);
                        //If both are null, ie. its not stocked
                        if(null != cheaperAahData && null != cheaperPrice ){
                                //if cheaper is aah, and available then yellow back with green -- greenBoldFontStyle
                                //if aah stock available then green, if stock not available then red
                                if(null != cheaperAahData.getCascadeStatus() && SUPPLIER_STATUS_AVAILABLE.equalsIgnoreCase(cheaperAahData.getCascadeStatus())
                                        && null != cheaperAahData.getCascadePrice()
                                        && null != cheaperPrice.getCascadePrice()
                                        && cheaperPrice.getCascadePrice().compareTo(cheaperAahData.getCascadePrice()) == 0){
                                        cell8.setCellValue(cheaperAahData.getCascadePrice());
                                        cell8.setCellStyle(greenBoldFontStyle);
                                }else{
                                        if(null != cheaperAahData.getCascadeStatus() && SUPPLIER_STATUS_AVAILABLE.equalsIgnoreCase(cheaperAahData.getCascadeStatus())){
                                                cell8.setCellValue(cheaperAahData.getCascadePrice());
                                                cell8.setCellStyle(greenFontStyle);
                                        }else{
                                                cell8.setCellValue(cheaperAahData.getCascadePrice());
                                                cell8.setCellStyle(redFontStyle);
                                        }

                                }
                        }else{
                                cell8.setCellValue("NS");
                        }

                        //Bestway
                        Cell cell9 = row.createCell(9);
                        //If both are null, ie. its not stocked
                        if(null != cheaperBestwayData && null != cheaperPrice ){
                                //if cheaper is aah, and available then yellow back with green -- greenBoldFontStyle
                                //if aah stock available then green, if stock not available then red
                                if(null != cheaperBestwayData.getCascadeStatus() && SUPPLIER_STATUS_AVAILABLE.equalsIgnoreCase(cheaperBestwayData.getCascadeStatus())
                                        && null != cheaperBestwayData.getCascadePrice()
                                        && null != cheaperPrice.getCascadePrice()
                                        && cheaperPrice.getCascadePrice().compareTo(cheaperBestwayData.getCascadePrice()) == 0){
                                        cell9.setCellValue(cheaperBestwayData.getCascadePrice());
                                        cell9.setCellStyle(greenBoldFontStyle);
                                }else{
                                        if(null != cheaperBestwayData.getCascadeStatus() && SUPPLIER_STATUS_AVAILABLE.equalsIgnoreCase(cheaperBestwayData.getCascadeStatus())){
                                                cell9.setCellValue(cheaperBestwayData.getCascadePrice());
                                                cell9.setCellStyle(greenFontStyle);
                                        }else{
                                                cell9.setCellValue(cheaperBestwayData.getCascadePrice());
                                                cell9.setCellStyle(redFontStyle);
                                        }

                                }
                        }else{
                                cell9.setCellValue("NS");
                        }

                        //BNS
                        Cell cell10 = row.createCell(10);
                        //If both are null, ie. its not stocked
                        if(null != cheaperBnsData && null != cheaperPrice ){
                                //if cheaper is aah, and available then yellow back with green -- greenBoldFontStyle
                                //if aah stock available then green, if stock not available then red
                                if(null != cheaperBnsData.getCascadeStatus() && SUPPLIER_STATUS_AVAILABLE.equalsIgnoreCase(cheaperBnsData.getCascadeStatus())
                                        && null != cheaperBnsData.getCascadePrice()
                                        && null != cheaperPrice.getCascadePrice()
                                        && cheaperPrice.getCascadePrice().compareTo(cheaperBnsData.getCascadePrice()) == 0){
                                        cell10.setCellValue(cheaperBnsData.getCascadePrice());
                                        cell10.setCellStyle(greenBoldFontStyle);
                                }else{
                                        if(null != cheaperBnsData.getCascadeStatus() && SUPPLIER_STATUS_AVAILABLE.equalsIgnoreCase(cheaperBnsData.getCascadeStatus())){
                                                cell10.setCellValue(cheaperBnsData.getCascadePrice());
                                                cell10.setCellStyle(greenFontStyle);
                                        }else{
                                                cell10.setCellValue(cheaperBnsData.getCascadePrice());
                                                cell10.setCellStyle(redFontStyle);
                                        }

                                }
                        }else{
                                cell10.setCellValue("NS");
                        }

                        //Lexon
                        Cell cell11 = row.createCell(11);
                        //If both are null, ie. its not stocked
                        if(null != cheaperLexonData && null != cheaperPrice ){
                                //if cheaper is aah, and available then yellow back with green -- greenBoldFontStyle
                                //if aah stock available then green, if stock not available then red
                                if(null != cheaperLexonData.getCascadeStatus() && SUPPLIER_STATUS_AVAILABLE.equalsIgnoreCase(cheaperLexonData.getCascadeStatus())
                                        && null != cheaperLexonData.getCascadePrice()
                                        && null != cheaperPrice.getCascadePrice()
                                        && cheaperPrice.getCascadePrice().compareTo(cheaperLexonData.getCascadePrice()) == 0){
                                        cell11.setCellValue(cheaperLexonData.getCascadePrice());
                                        cell11.setCellStyle(greenBoldFontStyle);
                                }else{
                                        if(null != cheaperLexonData.getCascadeStatus() && SUPPLIER_STATUS_AVAILABLE.equalsIgnoreCase(cheaperLexonData.getCascadeStatus())){
                                                cell11.setCellValue(cheaperLexonData.getCascadePrice());
                                                cell11.setCellStyle(greenFontStyle);
                                        }else{
                                                cell11.setCellValue(cheaperLexonData.getCascadePrice());
                                                cell11.setCellStyle(redFontStyle);
                                        }

                                }
                        }else{
                                cell11.setCellValue("NS");
                        }

                        //OTC
                        Cell cell12 = row.createCell(12);
                        cell12.setCellValue("");
                        cell12.setCellStyle(normalFontStyle);

                        //Sigma
                        Cell cell13 = row.createCell(13);
                        //If both are null, ie. its not stocked
                        if(null != cheaperSigmaData && null != cheaperPrice ){
                                //if cheaper is aah, and available then yellow back with green -- greenBoldFontStyle
                                //if aah stock available then green, if stock not available then red
                                if(null != cheaperSigmaData.getCascadeStatus() && SUPPLIER_STATUS_AVAILABLE.equalsIgnoreCase(cheaperSigmaData.getCascadeStatus())
                                        && null != cheaperSigmaData.getCascadePrice()
                                        && null != cheaperPrice.getCascadePrice()
                                        && cheaperPrice.getCascadePrice().compareTo(cheaperSigmaData.getCascadePrice()) == 0){
                                        cell13.setCellValue(cheaperSigmaData.getCascadePrice());
                                        cell13.setCellStyle(greenBoldFontStyle);
                                }else{
                                        if(null != cheaperSigmaData.getCascadeStatus() && SUPPLIER_STATUS_AVAILABLE.equalsIgnoreCase(cheaperSigmaData.getCascadeStatus())){
                                                cell13.setCellValue(cheaperSigmaData.getCascadePrice());
                                                cell13.setCellStyle(greenFontStyle);
                                        }else{
                                                cell13.setCellValue(cheaperSigmaData.getCascadePrice());
                                                cell13.setCellStyle(redFontStyle);
                                        }

                                }
                        }else{
                                cell13.setCellValue("NS");
                        }

                        //Trident
                        Cell cell14 = row.createCell(14);
                        //If both are null, ie. its not stocked
                        if(null != cheaperTridentData && null != cheaperPrice ){
                                //if cheaper is aah, and available then yellow back with green -- greenBoldFontStyle
                                //if aah stock available then green, if stock not available then red
                                if(null != cheaperTridentData.getCascadeStatus() && SUPPLIER_STATUS_AVAILABLE.equalsIgnoreCase(cheaperTridentData.getCascadeStatus())
                                        && null != cheaperTridentData.getCascadePrice()
                                        && null != cheaperPrice.getCascadePrice()
                                        && cheaperPrice.getCascadePrice().compareTo(cheaperTridentData.getCascadePrice()) == 0){
                                        cell14.setCellValue(cheaperTridentData.getCascadePrice());
                                        cell14.setCellStyle(greenBoldFontStyle);
                                }else{
                                        if(null != cheaperTridentData.getCascadeStatus() && SUPPLIER_STATUS_AVAILABLE.equalsIgnoreCase(cheaperTridentData.getCascadeStatus())){
                                                cell14.setCellValue(cheaperTridentData.getCascadePrice());
                                                cell14.setCellStyle(greenFontStyle);
                                        }else{
                                                cell14.setCellValue(cheaperTridentData.getCascadePrice());
                                                cell14.setCellStyle(redFontStyle);
                                        }

                                }
                        }else{
                                cell14.setCellValue("NS");
                        }

                        //Alliance
                        Cell cell15 = row.createCell(15);
                        //If both are null, ie. its not stocked
                        if(null != cheaperAllianceData && null != cheaperPrice ){
                                //if cheaper is aah, and available then yellow back with green -- greenBoldFontStyle
                                //if aah stock available then green, if stock not available then red
                                if(null != cheaperAllianceData.getCascadeStatus() && SUPPLIER_STATUS_AVAILABLE.equalsIgnoreCase(cheaperAllianceData.getCascadeStatus())
                                        && null != cheaperAllianceData.getCascadePrice()
                                        && null != cheaperPrice.getCascadePrice()
                                        && cheaperPrice.getCascadePrice().compareTo(cheaperAllianceData.getCascadePrice()) == 0){
                                        cell15.setCellValue(cheaperAllianceData.getCascadePrice());
                                        cell15.setCellStyle(greenBoldFontStyle);
                                }else{
                                        if(null != cheaperAllianceData.getCascadeStatus() && SUPPLIER_STATUS_AVAILABLE.equalsIgnoreCase(cheaperAllianceData.getCascadeStatus())){
                                                cell15.setCellValue(cheaperAllianceData.getCascadePrice());
                                                cell15.setCellStyle(greenFontStyle);
                                        }else{
                                                cell15.setCellValue(cheaperAllianceData.getCascadePrice());
                                                cell15.setCellStyle(redFontStyle);
                                        }

                                }
                        }else{
                                cell15.setCellValue("NS");
                        }

                        Cell cell16 = row.createCell(16);
                        LocalDateTime now = LocalDateTime.now();
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                        String formattedDateTime = now.format(formatter);
                        cell16.setCellValue(formattedDateTime);
                        cell16.setCellStyle(normalFontStyle);

                        Cell cell17 = row.createCell(17);
                        cell17.setCellValue(i);
                        cell17.setCellStyle(normalFontStyle);
                }

                /* Write changes to the workbook */
                FileOutputStream out = new FileOutputStream(new File("cascadeResults.xlsx"));
                workbook.write(out);
                out.close();

                Long endTime = System.currentTimeMillis();
                System.out.println("Total time taken ======>"+ (endTime-startTime)/1000 +" Seconds");

        }
}