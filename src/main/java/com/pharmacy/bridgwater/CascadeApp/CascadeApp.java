package com.pharmacy.bridgwater.CascadeApp;

import com.pharmacy.bridgwater.CascadeApp.model.CascadeSupplier;
import com.pharmacy.bridgwater.CascadeApp.model.SigmaData;
import com.pharmacy.bridgwater.CascadeApp.service.CascadeService;
import com.pharmacy.bridgwater.CascadeApp.service.SigmaProcessService;
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
                Map<String, List<CascadeSupplier>> cascadeResults =  cascade.getCascadeResults();

                Map<String, Set<String>> sigmaDataSet =  new LinkedHashMap<>();
                for (Map.Entry<String, List<CascadeSupplier>> entry : cascadeResults.entrySet()) {
                        Set<String> pipCodes = new HashSet<>();
                        if(entry.getValue()!=null && !entry.getValue().isEmpty()){
                                pipCodes = entry.getValue().stream()
                                        .filter(cs ->!"".equalsIgnoreCase(cs.getCode()))
                                        .map(v ->v.getCode()).collect(Collectors.toSet());
                        }
                        sigmaDataSet.put(entry.getKey(), pipCodes);
                }
                SigmaProcessService sigmaProcess = new SigmaProcessService();
                Map<String,Set<SigmaData>> sigmaDataSetProcessed = sigmaProcess.processSigma(sigmaDataSet);

                // add the sigma Data into cascade
                for(Map.Entry<String,Set<SigmaData>> entry: sigmaDataSetProcessed.entrySet()){
                        String key = entry.getKey();
                        List<CascadeSupplier> sigmaData = entry.getValue().stream().map(sigma -> CascadeSupplier.builder().supplier("Sigma")
                                .code(sigma.getCode()).price(sigma.getPrice()).status(sigma.getStatus()).build()).collect(Collectors.toList());
                        cascadeResults.get(key).addAll(sigmaData);
                }




                int i =0;
                Row row0 = my_sheet.createRow(0);
                Cell cell0_0 = row0.createCell(0); cell0_0.setCellValue("SNo"); cell0_0.setCellStyle(normalFontStyle);
                Cell cell0_1 = row0.createCell(1); cell0_1.setCellValue("Description"); cell0_1.setCellStyle(normalFontStyle);
                Cell cell0_2 = row0.createCell(2); cell0_2.setCellValue("Quantity"); cell0_2.setCellStyle(normalFontStyle);
                Cell cell0_3 = row0.createCell(3); cell0_3.setCellValue("From"); cell0_3.setCellStyle(normalFontStyle);
                Cell cell0_4 = row0.createCell(4); cell0_4.setCellValue("Notes"); cell0_4.setCellStyle(normalFontStyle);
                Cell cell0_5 = row0.createCell(5); cell0_5.setCellValue("Tariff"); cell0_5.setCellStyle(normalFontStyle);
                Cell cell0_6 = row0.createCell(6); cell0_6.setCellValue("DTNet"); cell0_6.setCellStyle(normalFontStyle);
                Cell cell0_7 = row0.createCell(7); cell0_7.setCellValue("Concession"); cell0_6.setCellStyle(normalFontStyle);
                Cell cell0_8 = row0.createCell(8); cell0_8.setCellValue("OrderCode"); cell0_8.setCellStyle(normalFontStyle);



                Cell cell0_9 = row0.createCell(9); cell0_9.setCellValue("AAH"); cell0_9.setCellStyle(normalFontStyle);
                Cell cell0_10 = row0.createCell(10); cell0_10.setCellValue("Bns"); cell0_10.setCellStyle(normalFontStyle);
                Cell cell0_11 = row0.createCell(11); cell0_11.setCellValue("Bestway"); cell0_11.setCellStyle(normalFontStyle);
                Cell cell0_12 = row0.createCell(12); cell0_12.setCellValue("Lexon"); cell0_12.setCellStyle(normalFontStyle);
                Cell cell0_13 = row0.createCell(13); cell0_13.setCellValue("Trident"); cell0_13.setCellStyle(normalFontStyle);
                Cell cell0_14 = row0.createCell(14); cell0_14.setCellValue("Alliance"); cell0_14.setCellStyle(normalFontStyle);
                Cell cell0_15 = row0.createCell(15); cell0_15.setCellValue("Sigma"); cell0_15.setCellStyle(normalFontStyle);
                Cell cell0_16 = row0.createCell(16); cell0_16.setCellValue("LookedupAt"); cell0_16.setCellStyle(normalFontStyle);

                for (Map.Entry<String, List<CascadeSupplier>> entry : cascadeResults.entrySet()) {
                        i++;
                        String key = entry.getKey();
                        List<CascadeSupplier> supplierList = entry.getValue();
                        CascadeSupplier aahSupplier = supplierList.stream().filter(s -> "AAH Pharmaceuticals".equalsIgnoreCase(s.getSupplier()))
                                .findAny().orElse(null);
                        CascadeSupplier bns = supplierList.stream().filter(s -> "B&S Colorama".equalsIgnoreCase(s.getSupplier()))
                                .findAny().orElse(null);
                        CascadeSupplier bestway = supplierList.stream().filter(s -> "Bestway MedHub".equalsIgnoreCase(s.getSupplier()))
                                .findAny().orElse(null);
                        CascadeSupplier lexon = supplierList.stream().filter(s -> "Lexon UK".equalsIgnoreCase(s.getSupplier()))
                                .findAny().orElse(null);
                        CascadeSupplier trident = supplierList.stream().filter(s -> "Trident Pharmaceuticals".equalsIgnoreCase(s.getSupplier()))
                                .findAny().orElse(null);
                        CascadeSupplier alliance = supplierList.stream().filter(s -> "Alliance Healthcare".equalsIgnoreCase(s.getSupplier()))
                                .findAny().orElse(null);
                        CascadeSupplier sigma = supplierList.stream().filter(s -> "Sigma".equalsIgnoreCase(s.getSupplier()))
                                .findAny().orElse(null);

                        CascadeSupplier leastPrice = supplierList
                                .stream()
                                .min(Comparator.comparing(CascadeSupplier::getPrice)
                                        )
                                .get();

                        // other than sigma
                        CascadeSupplier otherThanSigma = supplierList.stream()
                                .filter(v ->!v.getSupplier().equalsIgnoreCase("Sigma"))
                                .findAny().orElse(null);

                        Row row = my_sheet.createRow(i);

                        Cell cell0 = row.createCell(0);
                        cell0.setCellValue(i);
                        cell0.setCellStyle(normalFontStyle);

                        Cell cell1 = row.createCell(1);
                        cell1.setCellValue(key);
                        cell1.setCellStyle(normalFontStyle);

                        Cell cell2 = row.createCell(2);
                        if(otherThanSigma!=null && otherThanSigma.getQuantity()!=null){
                                cell2.setCellValue(otherThanSigma.getQuantity());
                        }else{
                                cell2.setCellValue("");
                        }
                        cell2.setCellStyle(normalFontStyle);

                        Cell cell3 = row.createCell(3);
                        cell3.setCellValue("");
                        cell3.setCellStyle(normalFontStyle);

                        Cell cell4 = row.createCell(4);
                        cell4.setCellValue("");
                        cell4.setCellStyle(normalFontStyle);

                        //Tariff
                        Cell cell5 = row.createCell(5);
                        if(otherThanSigma!=null && otherThanSigma.getTariff()!=null){
                                cell5.setCellValue(otherThanSigma.getTariff());
                        }else{
                                cell5.setCellValue("");
                        }

                        cell5.setCellStyle(normalFontStyle);

                        //TariffAfterDeduction
                        Cell cell6 = row.createCell(6);
                        if(otherThanSigma!=null && otherThanSigma.getTariffAfterDeduction()!=null){
                                cell6.setCellValue(otherThanSigma.getTariffAfterDeduction());
                        }else{
                                cell6.setCellValue("");
                        }

                        cell6.setCellStyle(normalFontStyle);
                        //concession
                        Cell cell7 = row.createCell(7);
                        if(otherThanSigma!=null && otherThanSigma.getConcession()!=null){
                                cell7.setCellValue(otherThanSigma.getConcession());
                        }else {
                                cell7.setCellValue("");
                        }
                        cell7.setCellStyle(normalFontStyle);
                        //ordercode
                        Cell cell8 = row.createCell(8);
                        if(otherThanSigma!=null && otherThanSigma.getCode()!=null){
                                cell8.setCellValue(otherThanSigma.getCode());
                        }else{
                                cell8.setCellValue("");
                        }
                        cell8.setCellStyle(normalFontStyle);

                        /// ////
                        Cell cell9 = row.createCell(9);
                        if(aahSupplier == null){
                                cell9.setCellValue("NS");
                        }else if(aahSupplier!=null && "Available".equalsIgnoreCase(aahSupplier.getStatus())){
                                if(leastPrice.getPrice().equals(aahSupplier.getPrice())){
                                        cell9.setCellValue(aahSupplier.getPrice());
                                        cell9.setCellStyle(greenBoldFontStyle);
                                }else {
                                        cell9.setCellValue(aahSupplier.getPrice());
                                        cell9.setCellStyle(greenFontStyle);
                                }
                        }else if(aahSupplier!=null && "Not Available".equalsIgnoreCase(aahSupplier.getStatus())){
                                cell9.setCellValue(aahSupplier.getPrice());
                                cell9.setCellStyle(redFontStyle);
                        }else{
                                cell9.setCellValue(aahSupplier.getPrice());
                                cell9.setCellStyle(orangeFontStyle);
                        }
                        /// //

                        /// ////
                        Cell cell10 = row.createCell(10);
                        if(bns == null){
                                cell10.setCellValue("NS");
                        }else if(bns!=null && "Available".equalsIgnoreCase(bns.getStatus())){
                                if(leastPrice.getPrice().equals(bns.getPrice())){
                                        cell10.setCellValue(bns.getPrice());
                                        cell10.setCellStyle(greenBoldFontStyle);
                                }else {
                                        cell10.setCellValue(bns.getPrice());
                                        cell10.setCellStyle(greenFontStyle);
                                }
                        }else if(bns!=null && "Not Available".equalsIgnoreCase(bns.getStatus())){
                                cell10.setCellValue(bns.getPrice());
                                cell10.setCellStyle(redFontStyle);
                        }else{
                                cell10.setCellValue(bns.getPrice());
                                cell10.setCellStyle(orangeFontStyle);
                        }
                        /// //

                        /// ////
                        Cell cell11 = row.createCell(11);
                        if(bestway == null){
                                cell11.setCellValue("NS");
                        }else if(bestway!=null && "Available".equalsIgnoreCase(bestway.getStatus())){
                                if(leastPrice.getPrice().equals(bestway.getPrice())){
                                        cell11.setCellValue(bestway.getPrice());
                                        cell11.setCellStyle(greenBoldFontStyle);
                                }else{
                                        cell11.setCellValue(bestway.getPrice());
                                        cell11.setCellStyle(greenFontStyle);
                                }

                        }else if(bestway!=null && "Not Available".equalsIgnoreCase(bestway.getStatus())){
                                cell11.setCellValue(bestway.getPrice());
                                cell11.setCellStyle(redFontStyle);
                        }else{
                                cell11.setCellValue(bestway.getPrice());
                                cell11.setCellStyle(orangeFontStyle);
                        }
                        /// //

                        /// ////
                        Cell cell12 = row.createCell(12);
                        if(lexon == null){
                                cell12.setCellValue("NS");
                        }else if(lexon!=null && "Available".equalsIgnoreCase(lexon.getStatus())){
                                if(leastPrice.getPrice().equals(lexon.getPrice())){
                                        cell12.setCellValue(lexon.getPrice());
                                        cell12.setCellStyle(greenBoldFontStyle);
                                }else{
                                        cell12.setCellValue(lexon.getPrice());
                                        cell12.setCellStyle(greenFontStyle);
                                }

                        }else if(lexon!=null && "Not Available".equalsIgnoreCase(lexon.getStatus())){
                                cell12.setCellValue(lexon.getPrice());
                                cell12.setCellStyle(redFontStyle);
                        }else{
                                cell12.setCellValue(lexon.getPrice());
                                cell12.setCellStyle(orangeFontStyle);
                        }
                        /// //

                        /// ////
                        Cell cell13 = row.createCell(13);
                        if(trident == null){
                                cell13.setCellValue("NS");
                        }else if(trident!=null && "Available".equalsIgnoreCase(trident.getStatus())){
                                if(leastPrice.getPrice().equals(trident.getPrice())){
                                        cell13.setCellValue(trident.getPrice());
                                        cell13.setCellStyle(greenBoldFontStyle);
                                }else{
                                        cell13.setCellValue(trident.getPrice());
                                        cell13.setCellStyle(greenFontStyle);
                                }

                        }else if(trident!=null && "Not Available".equalsIgnoreCase(trident.getStatus())){
                                cell13.setCellValue(trident.getPrice());
                                cell13.setCellStyle(redFontStyle);
                        }else{
                                cell13.setCellValue(trident.getPrice());
                                cell13.setCellStyle(orangeFontStyle);
                        }
                        /// //

                        /// ////
                        Cell cell14 = row.createCell(14);
                        if(alliance == null){
                                cell14.setCellValue("NS");
                        }else if(alliance!=null && "Available".equalsIgnoreCase(alliance.getStatus())){
                                if(leastPrice.getPrice().equals(alliance.getPrice())){
                                        cell14.setCellValue(alliance.getPrice());
                                        cell14.setCellStyle(greenBoldFontStyle);
                                }else{
                                        cell14.setCellValue(alliance.getPrice());
                                        cell14.setCellStyle(greenFontStyle);
                                }

                        }else if(alliance!=null && "Not Available".equalsIgnoreCase(alliance.getStatus())){
                                cell14.setCellValue(alliance.getPrice());
                                cell14.setCellStyle(redFontStyle);
                        }else{
                                cell14.setCellValue(alliance.getPrice());
                                cell14.setCellStyle(orangeFontStyle);
                        }
                        /// //

                        /// ////
                        Cell cell15 = row.createCell(15);
                        if(sigma == null){
                                cell15.setCellValue("NS");
                        }else if(sigma!=null && "Available".equalsIgnoreCase(sigma.getStatus())){
                                if(leastPrice.getPrice().equals(sigma.getPrice())){
                                        cell15.setCellValue(sigma.getPrice());
                                        cell15.setCellStyle(greenBoldFontStyle);
                                }else{
                                        cell15.setCellValue(sigma.getPrice());
                                        cell15.setCellStyle(greenFontStyle);
                                }

                        }else if(sigma!=null && "Not Available".equalsIgnoreCase(sigma.getStatus())){
                                cell15.setCellValue(sigma.getPrice());
                                cell15.setCellStyle(redFontStyle);
                        }else{
                                cell15.setCellValue(sigma.getPrice());
                                cell15.setCellStyle(orangeFontStyle);
                        }
                        /// //

                        Cell cell16 = row.createCell(16);
                        LocalDateTime now = LocalDateTime.now();
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                        String formattedDateTime = now.format(formatter);
                        cell16.setCellValue(formattedDateTime);
                        cell16.setCellStyle(normalFontStyle);



                }

                /* Write changes to the workbook */
                FileOutputStream out = new FileOutputStream(new File("cascadeResults.xlsx"));
                workbook.write(out);
                out.close();

                Long endTime = System.currentTimeMillis();
                System.out.println("Total time taken ======>"+ (endTime-startTime)/1000 +" Seconds");
                
        }
}