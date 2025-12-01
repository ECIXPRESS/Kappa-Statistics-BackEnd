package edu.dosw.Kappa_Stats_BackEnd.Application.Services.StatsServices;

import edu.dosw.Kappa_Stats_BackEnd.Application.Dtos.*;
import edu.dosw.Kappa_Stats_BackEnd.Application.Services.UseCases.*;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.DayOfWeek;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExcelAllStatisticsService {

    private final GenerateDailySalesUseCase daily;
    private final GenerateWeeklySalesUseCase weekly;
    private final GenerateMonthlySalesUseCase monthly;
    private final GenerateSummaryUseCase summary;
    private final GenerateProductRankingUseCase topProducts;

    public byte[] generateAllStatistics(String storeId) {

        LocalDate today = LocalDate.now();

        DailySalesReport dailyReport = daily.generate(today, storeId);
        WeeklySalesReport weeklyReport = weekly.generateWeekly(today, storeId);
        MonthlySalesReport monthlyReport = monthly.generateMonthlySalesReport(today.getYear(), today.getMonthValue(), storeId);
        SummaryReport summaryReport = summary.generate(storeId);
        List<ProductSalesReport> productRanking = topProducts.generateTopProducts(storeId);

        try (Workbook workbook = new XSSFWorkbook()) {

            Sheet dailySheet = workbook.createSheet("Daily Report");
            writeDailyReport(dailyReport, dailySheet);


            Sheet weeklySheet = workbook.createSheet("Weekly Report");
            writeWeeklyReport(weeklyReport, weeklySheet);

            Sheet monthlySheet = workbook.createSheet("Monthly Report");
            writeMonthlyReport(monthlyReport, monthlySheet);

            Sheet summarySheet = workbook.createSheet("Summary");
            writeSummaryReport(summaryReport, summarySheet);

            Sheet topProductsSheet = workbook.createSheet("Top Products");
            writeTopProducts(productRanking, topProductsSheet);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error generating Excel", e);
        }
    }


    private void writeDailyReport(DailySalesReport r, Sheet sheet) {
        sheet.createRow(0).createCell(0).setCellValue("Daily Sales Report");
        int i = 2;
        sheet.createRow(i).createCell(0).setCellValue("Store ID");     sheet.getRow(i++).createCell(1).setCellValue(r.store());
        sheet.createRow(i).createCell(0).setCellValue("Date");         sheet.getRow(i++).createCell(1).setCellValue(r.date());
        sheet.createRow(i).createCell(0).setCellValue("Total Orders"); sheet.getRow(i++).createCell(1).setCellValue(r.totalOrders());
        sheet.createRow(i).createCell(0).setCellValue("Products Sold");sheet.getRow(i++).createCell(1).setCellValue(r.totalProductsSold());
        sheet.createRow(i).createCell(0).setCellValue("Revenue");      sheet.getRow(i).createCell(1).setCellValue((RichTextString) r.totalRevenue());
        autoSize(sheet);
    }

    private void writeWeeklyReport(WeeklySalesReport r, Sheet sheet) {
        sheet.createRow(0).createCell(0).setCellValue("Weekly Sales Report");
        int i = 2;
        sheet.createRow(i).createCell(0).setCellValue("Store ID");     sheet.getRow(i++).createCell(1).setCellValue(r.store());
        sheet.createRow(i).createCell(0).setCellValue("Week Start");   sheet.getRow(i++).createCell(1).setCellValue(r.startDate().toString());
        sheet.createRow(i).createCell(0).setCellValue("Week End");     sheet.getRow(i++).createCell(1).setCellValue(r.endDate().toString());
        sheet.createRow(i).createCell(0).setCellValue("Total Orders"); sheet.getRow(i++).createCell(1).setCellValue(r.totalOrders());
        sheet.createRow(i).createCell(0).setCellValue("Products Sold");sheet.getRow(i++).createCell(1).setCellValue(r.totalProductsSold());
        sheet.createRow(i).createCell(0).setCellValue("Revenue");      sheet.getRow(i).createCell(1).setCellValue((RichTextString) r.totalRevenue());
        autoSize(sheet);
    }

    private void writeMonthlyReport(MonthlySalesReport r, Sheet sheet) {
        sheet.createRow(0).createCell(0).setCellValue("Monthly Sales Report");
        int i = 2;
        sheet.createRow(i).createCell(0).setCellValue("Store ID");     sheet.getRow(i++).createCell(1).setCellValue(r.store());
        sheet.createRow(i).createCell(0).setCellValue("Year");         sheet.getRow(i++).createCell(1).setCellValue(r.year());
        sheet.createRow(i).createCell(0).setCellValue("Month");        sheet.getRow(i++).createCell(1).setCellValue(r.month());
        sheet.createRow(i).createCell(0).setCellValue("Total Orders"); sheet.getRow(i++).createCell(1).setCellValue(r.totalOrders());
        sheet.createRow(i).createCell(0).setCellValue("Products Sold");sheet.getRow(i++).createCell(1).setCellValue(r.totalProductsSold());
        sheet.createRow(i).createCell(0).setCellValue("Revenue");      sheet.getRow(i).createCell(1).setCellValue((RichTextString) r.totalRevenue());
        autoSize(sheet);
    }

    private void writeSummaryReport(SummaryReport r, Sheet sheet) {
        sheet.createRow(0).createCell(0).setCellValue("Summary Report");
        int i = 2;
        sheet.createRow(i).createCell(0).setCellValue("Store ID");     sheet.getRow(i++).createCell(1).setCellValue(r.store());
        sheet.createRow(i).createCell(0).setCellValue("Total Orders"); sheet.getRow(i++).createCell(1).setCellValue(r.totalOrders());
        sheet.createRow(i).createCell(0).setCellValue("Products Sold");sheet.getRow(i++).createCell(1).setCellValue(r.totalProductsSold());
        sheet.createRow(i).createCell(0).setCellValue("Revenue");      sheet.getRow(i).createCell(1).setCellValue((RichTextString) r.totalRevenue());
        autoSize(sheet);
    }

    private void writeTopProducts(List<ProductSalesReport> list, Sheet sheet) {
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Product ID");
        header.createCell(1).setCellValue("Product Name");
        header.createCell(2).setCellValue("Quantity Sold");
        header.createCell(3).setCellValue("Revenue");

        int row = 1;
        for (ProductSalesReport p : list) {
            Row r = sheet.createRow(row++);
            r.createCell(0).setCellValue(p.productId());
            r.createCell(1).setCellValue(p.productName());
            r.createCell(2).setCellValue(p.totalSold());
            r.createCell(3).setCellValue((RichTextString) p.totalRevenue());
        }
        autoSize(sheet);
    }

    private void autoSize(Sheet s) {
        for (int i = 0; i < 6; i++) {
            try { s.autoSizeColumn(i); } catch (Exception ignored) {}
        }
    }
}

