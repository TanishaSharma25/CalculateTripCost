package utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

public class ExcelUtils {

    private static final String DEFAULT_FILE = "src/test/resources/booking_results.xlsx";
    private static final DateTimeFormatter TS_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void appendTopHotels(List<HotelRow> hotels) {
        appendTopHotels(hotels, DEFAULT_FILE);
    }

    public static void appendTopHotels(List<HotelRow> hotels, String filePath) {
        String[] headers = { "S.No", "Hotel Name", "Price", "Timestamp" };
        saveToExcel(filePath, "TopHotels", headers, hotels);
    }

    public static void appendActivityDetails(String name, String price, String duration, String departure) {
        appendActivityDetails(name, price, duration, departure, DEFAULT_FILE);
    }

    public static void appendActivityDetails(String name, String price, String duration, String departure, String filePath) {
        String[] headers = { "S.No", "Name", "Price", "Duration", "Departure", "Timestamp" };
        ActivityRow row = new ActivityRow(name, price, duration, departure);
        saveToExcel(filePath, "AttractionDetails", headers, Collections.singletonList(row));
    }

    private static synchronized void saveToExcel(String filePath, String sheetName, String[] headers, List<? extends RowConvertible> rows) {
        Workbook workbook;
        File file = new File(filePath);

        // Load existing workbook to preserve other sheets, or create new if file doesn't exist
        try {
            if (file.exists() && file.length() > 0) {
                try (FileInputStream fis = new FileInputStream(file)) {
                    workbook = new XSSFWorkbook(fis);
                }
            } else {
                workbook = new XSSFWorkbook();
            }

            // Overwrite logic: Remove existing sheet and recreate it fresh
            int sheetIndex = workbook.getSheetIndex(sheetName);
            if (sheetIndex != -1) workbook.removeSheetAt(sheetIndex);
            Sheet sheet = workbook.createSheet(sheetName);

            // Create Header
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }

            // Write Data
            int rowNum = 1;
            for (RowConvertible r : rows) {
                Row excelRow = sheet.createRow(rowNum);
                int col = 0;
                excelRow.createCell(col++).setCellValue(rowNum++); // S.No
                String[] cols = r.toColumns();
                for (String val : cols) {
                    excelRow.createCell(col++).setCellValue(val);
                }
                excelRow.createCell(col).setCellValue(LocalDateTime.now().format(TS_FMT));
            }

            for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);

            // Save and Close
            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                workbook.write(fos);
            }
            workbook.close();

        } catch (IOException e) {
            throw new RuntimeException("Error writing to Excel: " + filePath, e);
        }
    }

    /** DTOs and Interface (Kept exactly as original) **/
    public interface RowConvertible {
        String[] toColumns();
    }

    public static class HotelRow implements RowConvertible {
        public final String name;
        public final String price;
        public HotelRow(String name, String price) { this.name = name; this.price = price; }
        @Override public String[] toColumns() { return new String[] { name, price }; }
    }

    public static class ActivityRow implements RowConvertible {
        public final String name, price, duration, departure;
        public ActivityRow(String name, String price, String duration, String departure) {
            this.name = name; this.price = price; this.duration = duration; this.departure = departure;
        }
        @Override public String[] toColumns() { return new String[] { name, price, duration, departure }; }
    }
}