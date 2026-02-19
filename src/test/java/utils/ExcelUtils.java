package utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

public class ExcelUtils {

    // Default output at project root (adjust if needed)
    private static final String DEFAULT_FILE = "src/test/resources/booking_results.xlsx";;
    private static final DateTimeFormatter TS_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** Public convenience methods **/
    public static void appendTopHotels(List<HotelRow> hotels) {
        appendTopHotels(hotels, DEFAULT_FILE);
    }

    public static void appendTopHotels(List<HotelRow> hotels, String filePath) {
        String[] headers = new String[] { "S.No", "Hotel Name", "Price", "Timestamp" };
        appendRows(filePath, "TopHotels", headers, hotels);
    }


    public static void resetFile(String filePath) {
        File file = new File(filePath);
        // Option A: delete file if it exists (simplest)
        if (file.exists()) {
            if (!file.delete()) {
                throw new RuntimeException("Failed to delete existing Excel file: " + filePath);
            }
        }
    }


    public static void appendActivityDetails(String name, String price, String duration) {
        appendActivityDetails(name, price, duration, DEFAULT_FILE);
    }

    public static void appendActivityDetails(String name, String price, String duration, String filePath) {
        String[] headers = new String[] { "S.No", "Name", "Price", "Duration", "Timestamp" };
        ActivityRow row = new ActivityRow(name, price, duration);
        appendRows(filePath, "AttractionDetails", headers, Collections.<RowConvertible>singletonList(row));
    }

    /** Internal append logic (thread-safe) **/
    private static synchronized void appendRows(String filePath, String sheetName, String[] headers, List<? extends RowConvertible> rows) {
        if (rows == null || rows.isEmpty()) return;

        Workbook workbook = null;
        FileInputStream fis = null;
        try {
            File file = new File(filePath);
            if (file.exists()) {
                fis = new FileInputStream(file);
                workbook = new XSSFWorkbook(fis);
            } else {
                workbook = new XSSFWorkbook();
            }

            Sheet sheet = getOrCreateSheet(workbook, sheetName);
            ensureHeaderIfEmpty(sheet, headers);

            // If header at row 0 exists, data starts at row 1
            int startRow = sheet.getPhysicalNumberOfRows();
            if (startRow == 0) startRow = 1; // ensure data starts after header

            // Serial number (S.No) = current row index, starting at 1 for first data row
            int serial = startRow;

            for (RowConvertible r : rows) {
                org.apache.poi.ss.usermodel.Row excelRow = sheet.createRow(startRow++);
                int col = 0;
                excelRow.createCell(col++).setCellValue(serial++); // S.No
                String[] cols = r.toColumns();
                for (int i = 0; i < cols.length; i++) {
                    excelRow.createCell(col++).setCellValue(cols[i]);
                }
                excelRow.createCell(col).setCellValue(LocalDateTime.now().format(TS_FMT)); // Timestamp
            }

            // Auto-size columns for readability
            for (int c = 0; c < headers.length; c++) {
                sheet.autoSizeColumn(c);
            }

            // Write out
            closeQuietly(fis);
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(filePath, false); // overwrite file with updated workbook
                workbook.write(fos);
            } finally {
                closeQuietly(fos);
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to write Excel: " + filePath, e);
        } finally {
            closeQuietly(fis);
            closeQuietly(workbook);
        }
    }

    private static Sheet getOrCreateSheet(Workbook workbook, String sheetName) {
        Sheet sheet = workbook.getSheet(sheetName);
        if (sheet == null) {
            sheet = workbook.createSheet(sheetName);
        }
        return sheet;
    }

    private static void ensureHeaderIfEmpty(Sheet sheet, String[] headers) {
        if (sheet.getPhysicalNumberOfRows() == 0) {
            org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }
        }
    }

    private static void closeQuietly(Closeable c) {
        if (c != null) {
            try { c.close(); } catch (IOException ignored) {}
        }
    }

    private static void closeQuietly(Workbook wb) {
        if (wb != null) {
            try { wb.close(); } catch (IOException ignored) {}
        }
    }

    /** DTOs + interface to convert to string columns **/
    public interface RowConvertible {
        String[] toColumns();
    }

    public static class HotelRow implements RowConvertible {
        public final String name;
        public final String price;

        public HotelRow(String name, String price) {
            this.name = name;
            this.price = price;
        }

        @Override
        public String[] toColumns() {
            return new String[] { name, price };
        }
    }

    public static class ActivityRow implements RowConvertible {
        public final String name, price, duration;
        public ActivityRow(String name, String price, String duration) {
            this.name = name;
            this.price = price;
            this.duration = duration;
        }
        @Override
        public String[] toColumns() {
            return new String[] { name, price, duration };
        }
    }
}