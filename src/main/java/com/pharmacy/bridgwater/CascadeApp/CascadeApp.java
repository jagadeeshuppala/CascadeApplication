package com.pharmacy.bridgwater.CascadeApp;

import com.pharmacy.bridgwater.CascadeApp.model.ActualSupplierData;
import com.pharmacy.bridgwater.CascadeApp.service.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;


public class CascadeApp {
        public static void main(String[] args) throws Exception{
                Long startTime = System.currentTimeMillis();


                /* Create Workbook and Worksheet XLSX Format */
                XSSFWorkbook workbook = new XSSFWorkbook();
                XSSFSheet my_sheet = workbook.createSheet("Cascade Results");

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

                Map<String, Set<ActualSupplierData>> aahResults = new LinkedHashMap<>();
                for (Map.Entry<String, Set<ActualSupplierData>> entry : cascadeResults.entrySet()) {
                        String key = entry.getKey();
                        Set<ActualSupplierData> onlyAahValues = entry.getValue().stream()
                                .filter(v ->"AAH Pharmaceuticals".equalsIgnoreCase(v.getSupplier()))
                                .collect(Collectors.toSet());
                        aahResults.put(key, onlyAahValues);
                }

                Map<String, Set<ActualSupplierData>> tridentResults = new LinkedHashMap<>();
                for (Map.Entry<String, Set<ActualSupplierData>> entry : cascadeResults.entrySet()) {
                        String key = entry.getKey();
                        Set<ActualSupplierData> onlyTridentValues = entry.getValue().stream()
                                .filter(v ->"Trident Pharmaceuticals".equalsIgnoreCase(v.getSupplier()))
                                .collect(Collectors.toSet());
                        tridentResults.put(key, onlyTridentValues);
                }

                Map<String, Set<ActualSupplierData>> bnsResults = new LinkedHashMap<>();
                for (Map.Entry<String, Set<ActualSupplierData>> entry : cascadeResults.entrySet()) {
                        String key = entry.getKey();
                        Set<ActualSupplierData> onlyBnsValues = entry.getValue().stream()
                                .filter(v ->"B&S Colorama".equalsIgnoreCase(v.getSupplier()))
                                .collect(Collectors.toSet());
                        bnsResults.put(key, onlyBnsValues);
                }

                Map<String, Set<ActualSupplierData>> sigmaResults = new LinkedHashMap<>();
                for (Map.Entry<String, Set<ActualSupplierData>> entry : cascadeResults.entrySet()) {
                        String key = entry.getKey();
                        Set<ActualSupplierData> onlySigmaValues = entry.getValue().stream()
                                .filter(v ->"Sigma".equalsIgnoreCase(v.getSupplier()))
                                .collect(Collectors.toSet());
                        sigmaResults.put(key, onlySigmaValues);
                }

                Map<String, Set<ActualSupplierData>> bestwayResults = new LinkedHashMap<>();
                for (Map.Entry<String, Set<ActualSupplierData>> entry : cascadeResults.entrySet()) {
                        String key = entry.getKey();
                        Set<ActualSupplierData> onlyBestwayValues = entry.getValue().stream()
                                .filter(v ->"Bestway MedHub".equalsIgnoreCase(v.getSupplier()))
                                .collect(Collectors.toSet());
                        bestwayResults.put(key, onlyBestwayValues);
                }

                Map<String, Set<ActualSupplierData>> lexonResults = new LinkedHashMap<>();
                for (Map.Entry<String, Set<ActualSupplierData>> entry : cascadeResults.entrySet()) {
                        String key = entry.getKey();
                        Set<ActualSupplierData> onlyLexonValues = entry.getValue().stream()
                                .filter(v ->"Lexon UK".equalsIgnoreCase(v.getSupplier()))
                                .collect(Collectors.toSet());
                        lexonResults.put(key, onlyLexonValues);
                }

                Map<String, Set<ActualSupplierData>> allianceResults = new LinkedHashMap<>();
                for (Map.Entry<String, Set<ActualSupplierData>> entry : cascadeResults.entrySet()) {
                        String key = entry.getKey();
                        Set<ActualSupplierData> onlyAllianceValues = entry.getValue().stream()
                                .filter(v ->"Alliance Healthcare".equalsIgnoreCase(v.getSupplier()))
                                .collect(Collectors.toSet());
                        allianceResults.put(key, onlyAllianceValues);
                }


                ExecutorService executor = Executors.newFixedThreadPool(4);
                Callable aahWorker = new AahProcessService(aahResults);
                Callable tridentWorker = new TridentProcessService(tridentResults);
                Callable bestwayWorker = new BestwayProcessService(bestwayResults);
                Callable sigmaWorker = new SigmaProcessService(sigmaResults);
                Future<Map<String, Set<ActualSupplierData>>> aahFuture = executor.submit(aahWorker);
                Future<Map<String, Set<ActualSupplierData>>> tridentFuture = executor.submit(tridentWorker);
                Future<Map<String, Set<ActualSupplierData>>> bestwayFuture = executor.submit(bestwayWorker);
                Future<Map<String, Set<ActualSupplierData>>> sigmaFuture = executor.submit(sigmaWorker);

                Map<String, Set<ActualSupplierData>> aahProcessedResults = aahFuture.get();
                Map<String, Set<ActualSupplierData>> tridentProcessedResults = tridentFuture.get();
                Map<String, Set<ActualSupplierData>> bestwayProcessedResults = bestwayFuture.get();
                Map<String, Set<ActualSupplierData>> sigmaProcessedResults = sigmaFuture.get();

                executor.shutdown();

                System.out.println("!!!Fetching of the rates done!!!");
                Map<String, Set<ActualSupplierData>> supplierProcessedResults  =  new LinkedHashMap<>();
                for (Map.Entry<String, Set<ActualSupplierData>> entry : cascadeResults.entrySet()) {
                        String key = entry.getKey();
                        Set<ActualSupplierData> values = new LinkedHashSet<>();

                        //Aah processedResults
                        values.addAll(aahProcessedResults.get(key));
                        //Trident processedResults
                        values.addAll(tridentProcessedResults.get(key));
                        //Bestway processedResults
                        values.addAll(bestwayProcessedResults.get(key));
                        //Sigma processedResults
                        values.addAll(sigmaProcessedResults.get(key));
                        //Bns results
                        values.addAll(bnsResults.get(key));
                        //Lexon results
                        values.addAll(lexonResults.get(key));
                        //Alliance results
                        values.addAll(allianceResults.get(key));
                        //finally add values to the map
                        supplierProcessedResults.put(key, values);
                }



                // add the sigma Data into cascade
                /*for(Map.Entry<String,Set<ActualSupplierData>> entry: sigmaDataSetProcessed.entrySet()){
                        String key = entry.getKey();
                        Set<ActualSupplierData> sigmaData = entry.getValue().stream().map(sigma -> ActualSupplierData.builder().supplier("Sigma")
                                .code(sigma.getCode()).supplierPrice(sigma.getSupplierPrice()).supplierStatus(sigma.getSupplierStatus()).build()).collect(Collectors.toSet());
                        sigmaDataSetProcessed.get(key).addAll(sigmaData);
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

                for (Map.Entry<String, Set<ActualSupplierData>> entry : supplierProcessedResults.entrySet()) {
                        i++;
                        String key = entry.getKey();
                        Set<ActualSupplierData> supplierList = entry.getValue();

                        ActualSupplierData leastPrice = supplierList
                                .stream()
                                .min(Comparator.comparing(ActualSupplierData::getDefinitePrice)
                                )
                                .orElse(null);

                        ActualSupplierData availableLeastPrice = supplierList
                                .stream()
                                .filter(v->"Available".equalsIgnoreCase(v.getDefiniteStatus()))
                                .min(Comparator.comparing(ActualSupplierData::getDefinitePrice)
                                )
                                .orElse(null);


                        Row row = my_sheet.createRow(i);



                        Cell cell0 = row.createCell(0);
                        cell0.setCellValue(key);
                        cell0.setCellStyle(normalFontStyle);

                        Cell cell1 = row.createCell(1);
                        if(leastPrice!=null && leastPrice.getQuantity()!=null){
                                cell1.setCellValue(leastPrice.getQuantity());
                        }else{
                                cell1.setCellValue("");
                        }
                        cell1.setCellStyle(normalFontStyle);

                        Cell cell2 = row.createCell(2);
                        cell2.setCellValue("");
                        cell2.setCellStyle(normalFontStyle);

                        Cell cell3 = row.createCell(3);
                        cell3.setCellValue("");
                        cell3.setCellStyle(normalFontStyle);

                        //Tariff
                        Cell cell4 = row.createCell(4);
                        if(leastPrice!=null && leastPrice.getTariff()!=null){
                                cell4.setCellValue(leastPrice.getTariff());
                        }else{
                                cell4.setCellValue("");
                        }
                        cell4.setCellStyle(normalFontStyle);

                        //TariffAfterDeduction
                        Cell cell5 = row.createCell(5);
                        if(leastPrice!=null && leastPrice.getTariffAfterDeduction()!=null){
                                cell5.setCellValue(leastPrice.getTariffAfterDeduction());
                        }else{
                                cell5.setCellValue("");
                        }
                        cell5.setCellStyle(normalFontStyle);
                        //concession
                        Cell cell6 = row.createCell(6);
                        if(leastPrice!=null && leastPrice.getConcession()!=null){
                                cell6.setCellValue(leastPrice.getConcession());
                        }else {
                                cell6.setCellValue("");
                        }
                        cell6.setCellStyle(normalFontStyle);
                        //ordercode
                        Cell cell7 = row.createCell(7);
                        if(leastPrice!=null && leastPrice.getCode()!=null){
                                cell7.setCellValue(leastPrice.getCode());
                        }else{
                                cell7.setCellValue("");
                        }
                        cell7.setCellStyle(normalFontStyle);

                        /// AAH Start////
                        Cell cell8 = row.createCell(8);
                        ActualSupplierData leastAvailableAahSupplier = aahProcessedResults.get(key).stream()
                                .filter(v ->v.getSupplierPrice()!=null)
                                .filter(v ->availableLeastPrice!= null && availableLeastPrice.getDefiniteStatus().equalsIgnoreCase(v.getDefiniteStatus() )
                                && availableLeastPrice!=null && availableLeastPrice.getDefinitePrice().compareTo(v.getDefinitePrice()) == 0)
                                .findAny().orElse(null);

                        // AAH supplier
                        ActualSupplierData leastAahSupplier = aahProcessedResults.get(key).stream()
                                .filter(v ->v.getSupplierPrice()!=null)
                                .min(Comparator.comparing(ActualSupplierData::getDefinitePrice)
                                )
                                .orElse(null);

                        if(leastAvailableAahSupplier == null && leastAahSupplier == null){
                                //both null means no results
                                cell8.setCellValue("NS");
                        }else if(leastAvailableAahSupplier != null){
                                // if least Available then mark it green and yellow background
                                if(leastAvailableAahSupplier.getSupplierPrice() == null);{
                                        System.out.println("please print the whole ::::"+leastAvailableAahSupplier);
                                }
                                cell8.setCellValue(leastAvailableAahSupplier.getSupplierPrice());
                                cell8.setCellStyle(greenBoldFontStyle);
                        }else {
                                // least aah supplier is not null
                                if("Available".equalsIgnoreCase(leastAahSupplier.getSupplierStatus())){
                                        cell8.setCellValue(leastAahSupplier.getSupplierPrice());
                                        cell8.setCellStyle(greenFontStyle);
                                }else if("Not Available".equalsIgnoreCase(leastAahSupplier.getSupplierStatus())){
                                        cell8.setCellValue(leastAahSupplier.getSupplierPrice());
                                        cell8.setCellStyle(redFontStyle);
                                }else{
                                        cell8.setCellValue("NS");
                                }
                        }
                        /// AAH END

                        /// Bestway Start////
                        Cell cell9 = row.createCell(9);
                        ActualSupplierData leastAvailableBestwaySupplier = bestwayProcessedResults.get(key).stream()
                                .filter(v ->v.getSupplierPrice()!=null)
                                .filter(v ->availableLeastPrice!= null && availableLeastPrice.getDefiniteStatus().equalsIgnoreCase(v.getDefiniteStatus() )
                                        && availableLeastPrice!=null && availableLeastPrice.getDefinitePrice().compareTo(v.getDefinitePrice()) == 0)
                                .findAny().orElse(null);

                        // Bestway supplier
                        ActualSupplierData leastBestwaySupplier = bestwayProcessedResults.get(key).stream()
                                .filter(v ->v.getSupplierPrice()!=null)
                                .min(Comparator.comparing(ActualSupplierData::getDefinitePrice)
                                )
                                .orElse(null);

                        if(leastAvailableBestwaySupplier == null &&  leastBestwaySupplier== null){
                                //both null means no results
                                cell9.setCellValue("NS");
                        }else if( leastAvailableBestwaySupplier!= null){
                                // if least Available then mark it green and yellow background
                                if(leastAvailableBestwaySupplier.getSupplierPrice()== null){
                                        System.out.println("leastAvailableBestwaySupplier!!!!"+leastAvailableBestwaySupplier);
                                }
                                cell9.setCellValue(leastAvailableBestwaySupplier.getSupplierPrice());
                                cell9.setCellStyle(greenBoldFontStyle);
                        }else {
                                // least aah supplier is not null
                                if("Available".equalsIgnoreCase(leastBestwaySupplier.getSupplierStatus())){
                                        cell9.setCellValue(leastBestwaySupplier.getSupplierPrice());
                                        cell9.setCellStyle(greenFontStyle);
                                }else if("Not Available".equalsIgnoreCase(leastBestwaySupplier.getSupplierStatus())){
                                        cell9.setCellValue(leastBestwaySupplier.getSupplierPrice());
                                        cell9.setCellStyle(redFontStyle);
                                }else {
                                        cell9.setCellValue("NS");
                                }
                        }
                        /// Bestway END

                        /// BNS Start////
                        Cell cell10 = row.createCell(10);
                        ActualSupplierData leastAvailableBnsSupplier = bnsResults.get(key).stream()
                                .filter(v ->v.getCascadePrice()!=null)
                                .filter(v ->availableLeastPrice!= null && availableLeastPrice.getDefiniteStatus().equalsIgnoreCase(v.getDefiniteStatus() )
                                        && availableLeastPrice!=null && availableLeastPrice.getDefinitePrice().compareTo(v.getDefinitePrice()) == 0)
                                .findAny().orElse(null);

                        // Bns supplier
                        ActualSupplierData leastBnsSupplier = bnsResults.get(key).stream()
                                .filter(v ->v.getCascadePrice()!=null)
                                .min(Comparator.comparing(ActualSupplierData::getDefinitePrice)
                                )
                                .orElse(null);

                        if( leastAvailableBnsSupplier== null &&  leastBnsSupplier== null){
                                //both null means no results
                                cell10.setCellValue("NS");
                        }else if( leastAvailableBnsSupplier!= null){
                                // if least Available then mark it green and yellow background
                                cell10.setCellValue(leastAvailableBnsSupplier.getCascadePrice());
                                cell10.setCellStyle(greenBoldFontStyle);
                        }else {
                                // least aah supplier is not null
                                if("Available".equalsIgnoreCase(leastBnsSupplier.getCascadeStatus())){
                                        cell10.setCellValue(leastBnsSupplier.getCascadePrice());
                                        cell10.setCellStyle(greenFontStyle);
                                }else if("Not Available".equalsIgnoreCase(leastBnsSupplier.getCascadeStatus())){
                                        cell10.setCellValue(leastBnsSupplier.getCascadePrice());
                                        cell10.setCellStyle(redFontStyle);
                                }else {
                                        cell10.setCellValue("NS");
                                }
                        }
                        /// Bns END

                        /// Lexon Start////
                        Cell cell11 = row.createCell(11);
                        ActualSupplierData leastAvailableLexonSupplier = lexonResults.get(key).stream()
                                .filter(v ->v.getCascadePrice()!=null)
                                .filter(v ->availableLeastPrice!= null && availableLeastPrice.getDefiniteStatus().equalsIgnoreCase(v.getDefiniteStatus() )
                                        && availableLeastPrice!=null && availableLeastPrice.getDefinitePrice().compareTo(v.getDefinitePrice()) == 0)
                                .findAny().orElse(null);

                        // Lexon supplier
                        ActualSupplierData leastLexonSupplier = lexonResults.get(key).stream()
                                .filter(v ->v.getCascadePrice()!=null)
                                .min(Comparator.comparing(ActualSupplierData::getDefinitePrice)
                                )
                                .orElse(null);

                        if( leastAvailableLexonSupplier== null &&  leastLexonSupplier== null){
                                //both null means no results
                                cell11.setCellValue("NS");
                        }else if( leastAvailableLexonSupplier!= null){
                                // if least Available then mark it green and yellow background
                                cell11.setCellValue(leastAvailableLexonSupplier.getCascadePrice());
                                cell11.setCellStyle(greenBoldFontStyle);
                        }else {
                                // least aah supplier is not null
                                if("Available".equalsIgnoreCase(leastLexonSupplier.getCascadeStatus())){
                                        cell11.setCellValue(leastLexonSupplier.getCascadePrice());
                                        cell11.setCellStyle(greenFontStyle);
                                }else if("Not Available".equalsIgnoreCase(leastLexonSupplier.getCascadeStatus())){
                                        cell11.setCellValue(leastLexonSupplier.getCascadePrice());
                                        cell11.setCellStyle(redFontStyle);
                                }else {
                                        cell11.setCellValue("NS");
                                }
                        }
                        /// Lexon END

                        // OTC should be left blank
                        Cell cell12 = row.createCell(12);
                        cell12.setCellValue("");
                        cell12.setCellStyle(normalFontStyle);

                        /// Sigma Start////
                        Cell cell13 = row.createCell(13);
                        ActualSupplierData leastAvailableSigmaSupplier = sigmaProcessedResults.get(key).stream()
                                .filter(v ->v.getSupplierPrice()!=null)
                                .filter(v ->availableLeastPrice!= null && availableLeastPrice.getDefiniteStatus().equalsIgnoreCase(v.getDefiniteStatus() )
                                        && availableLeastPrice!=null && availableLeastPrice.getDefinitePrice().compareTo(v.getDefinitePrice()) == 0)
                                .findAny().orElse(null);

                        // Sigma supplier
                        ActualSupplierData leastSigmaSupplier = sigmaProcessedResults.get(key).stream()
                                .filter(v ->v.getSupplierPrice()!=null)
                                .min(Comparator.comparing(ActualSupplierData::getDefinitePrice)
                                )
                                .orElse(null);

                        if( leastAvailableSigmaSupplier== null &&  leastSigmaSupplier== null){
                                //both null means no results
                                cell13.setCellValue("NS");
                        }else if( leastAvailableSigmaSupplier!= null){
                                // if least Available then mark it green and yellow background
                                cell13.setCellValue(leastAvailableSigmaSupplier.getSupplierPrice());
                                cell13.setCellStyle(greenBoldFontStyle);
                        }else {
                                // least aah supplier is not null
                                if("Available".equalsIgnoreCase(leastSigmaSupplier.getSupplierStatus())){
                                        cell13.setCellValue(leastSigmaSupplier.getSupplierPrice());
                                        cell13.setCellStyle(greenFontStyle);
                                }else if("Not Available".equalsIgnoreCase(leastSigmaSupplier.getSupplierStatus())){
                                        cell13.setCellValue(leastSigmaSupplier.getSupplierPrice());
                                        cell13.setCellStyle(redFontStyle);
                                }else {
                                        cell13.setCellValue("NS");
                                }
                        }
                        /// Sigma END

                        /// Trident Start////
                        Cell cell14 = row.createCell(14);
                        ActualSupplierData leastAvailableTridentSupplier = tridentProcessedResults.get(key).stream()
                                .filter(v ->v.getSupplierPrice()!=null)
                                .filter(v ->availableLeastPrice!= null && availableLeastPrice.getDefiniteStatus().equalsIgnoreCase(v.getDefiniteStatus() )
                                        && availableLeastPrice!=null && availableLeastPrice.getDefinitePrice().compareTo(v.getDefinitePrice()) == 0)
                                .findAny().orElse(null);

                        // Trident supplier
                        ActualSupplierData leastTridentSupplier = tridentProcessedResults.get(key).stream()
                                .filter(v ->v.getSupplierPrice()!=null)
                                .min(Comparator.comparing(ActualSupplierData::getDefinitePrice)
                                )
                                .orElse(null);

                        if( leastAvailableTridentSupplier== null &&  leastTridentSupplier== null){
                                //both null means no results
                                cell14.setCellValue("NS");
                        }else if( leastAvailableTridentSupplier!= null){
                                // if least Available then mark it green and yellow background
                                cell14.setCellValue(leastAvailableTridentSupplier.getSupplierPrice());
                                cell14.setCellStyle(greenBoldFontStyle);
                        }else {
                                // least aah supplier is not null
                                if("Available".equalsIgnoreCase(leastTridentSupplier.getSupplierStatus())){
                                        cell14.setCellValue(leastTridentSupplier.getSupplierPrice());
                                        cell14.setCellStyle(greenFontStyle);
                                }else if("Not Available".equalsIgnoreCase(leastTridentSupplier.getSupplierStatus())){
                                        cell14.setCellValue(leastTridentSupplier.getSupplierPrice());
                                        cell14.setCellStyle(redFontStyle);
                                }else {
                                        cell14.setCellValue("NS");
                                }
                        }
                        /// Trident END

                        /// Alliance Start////
                        Cell cell15 = row.createCell(15);
                        ActualSupplierData leastAvailableAllianceSupplier = allianceResults.get(key).stream()
                                .filter(v ->v.getCascadePrice()!=null)
                                .filter(v ->availableLeastPrice!= null && availableLeastPrice.getDefiniteStatus().equalsIgnoreCase(v.getDefiniteStatus() )
                                        && availableLeastPrice!=null && availableLeastPrice.getDefinitePrice().compareTo(v.getDefinitePrice()) == 0)
                                .findAny().orElse(null);

                        // Alliance supplier
                        ActualSupplierData leastAllianceSupplier = allianceResults.get(key).stream()
                                .filter(v ->v.getCascadePrice()!=null)
                                .min(Comparator.comparing(ActualSupplierData::getDefinitePrice)
                                )
                                .orElse(null);

                        if( leastAvailableAllianceSupplier== null &&  leastAllianceSupplier== null){
                                //both null means no results
                                cell15.setCellValue("NS");
                        }else if( leastAvailableAllianceSupplier!= null){
                                // if least Available then mark it green and yellow background
                                cell15.setCellValue(leastAvailableAllianceSupplier.getCascadePrice());
                                cell15.setCellStyle(greenBoldFontStyle);
                        }else {
                                // least aah supplier is not null
                                if("Available".equalsIgnoreCase(leastAllianceSupplier.getCascadeStatus())){
                                        cell15.setCellValue(leastAllianceSupplier.getCascadePrice());
                                        cell15.setCellStyle(greenFontStyle);
                                }else if("Not Available".equalsIgnoreCase(leastAllianceSupplier.getCascadeStatus())){
                                        cell15.setCellValue(leastAllianceSupplier.getCascadePrice());
                                        cell15.setCellStyle(redFontStyle);
                                }else {
                                        cell15.setCellValue("NS");
                                }
                        }
                        /// Alliance END


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