package edu.dosw.Kappa_Stats_BackEnd.Application.Services.StatsServices;

import edu.dosw.Kappa_Stats_BackEnd.Application.Dtos.*;
import edu.dosw.Kappa_Stats_BackEnd.Application.Services.UseCases.*;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExcelAllStatisticsService {

    private final GenerateDailySalesUseCase dailyUseCase;
    private final GenerateWeeklySalesUseCase weeklyUseCase;
    private final GenerateMonthlySalesUseCase monthlyUseCase;
    private final GenerateSummaryUseCase summaryUseCase;
    private final GenerateProductRankingUseCase topProductsUseCase;

    public byte[] generateAllStatistics(String storeId) {
        try {
            LocalDate today = LocalDate.now();


            DailySalesReport dailyReport = dailyUseCase.execute(
                    new GenerateDailyReportCommand(storeId, today)
            );

            WeeklySalesReport weeklyReport = weeklyUseCase.execute(
                    new GenerateWeeklyReportCommand(storeId, today)
            );

            MonthlySalesReport monthlyReport = monthlyUseCase.execute(
                    new GenerateMonthlyReportCommand(storeId, today.getYear(), today.getMonthValue())
            );

            SummaryReport summaryReport = summaryUseCase.execute(
                    new GenerateSummaryCommand(storeId)
            );

            List<ProductSalesReport> productRanking = topProductsUseCase.execute(
                    new GenerateTopProductsCommand(storeId)
            );

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
                throw new RuntimeException("Error generating Excel workbook", e);
            }

        } catch (Exception e) {
            throw new RuntimeException("Error generating all statistics for store: " + storeId, e);
        }
    }

    private void writeDailyReport(DailySalesReport r, Sheet sheet) {
        sheet.createRow(0).createCell(0).setCellValue("Daily Sales Report");
        int i = 2;
        sheet.createRow(i).createCell(0).setCellValue("Store ID");
        sheet.getRow(i++).createCell(1).setCellValue(r.store());

        sheet.createRow(i).createCell(0).setCellValue("Date");
        sheet.getRow(i++).createCell(1).setCellValue(r.date());

        sheet.createRow(i).createCell(0).setCellValue("Total Orders");
        sheet.getRow(i++).createCell(1).setCellValue(r.totalOrders());

        sheet.createRow(i).createCell(0).setCellValue("Products Sold");
        sheet.getRow(i++).createCell(1).setCellValue(r.totalProductsSold());

        sheet.createRow(i).createCell(0).setCellValue("Revenue");
        sheet.getRow(i).createCell(1).setCellValue(r.totalRevenue().doubleValue());

        CellStyle currencyStyle = sheet.getWorkbook().createCellStyle();
        currencyStyle.setDataFormat(
                sheet.getWorkbook().createDataFormat().getFormat("$#,##0.00")
        );
        sheet.getRow(i).getCell(1).setCellStyle(currencyStyle);

        autoSize(sheet);
    }

    private void writeWeeklyReport(WeeklySalesReport r, Sheet sheet) {
        sheet.createRow(0).createCell(0).setCellValue("Weekly Sales Report");
        int i = 2;
        sheet.createRow(i).createCell(0).setCellValue("Store ID");
        sheet.getRow(i++).createCell(1).setCellValue(r.store());

        sheet.createRow(i).createCell(0).setCellValue("Week Start");
        sheet.getRow(i++).createCell(1).setCellValue(r.startDate().toString());

        sheet.createRow(i).createCell(0).setCellValue("Week End");
        sheet.getRow(i++).createCell(1).setCellValue(r.endDate().toString());

        sheet.createRow(i).createCell(0).setCellValue("Total Orders");
        sheet.getRow(i++).createCell(1).setCellValue(r.totalOrders());

        sheet.createRow(i).createCell(0).setCellValue("Products Sold");
        sheet.getRow(i++).createCell(1).setCellValue(r.totalProductsSold());

        sheet.createRow(i).createCell(0).setCellValue("Revenue");
        sheet.getRow(i).createCell(1).setCellValue(r.totalRevenue().doubleValue());

        CellStyle currencyStyle = sheet.getWorkbook().createCellStyle();
        currencyStyle.setDataFormat(
                sheet.getWorkbook().createDataFormat().getFormat("$#,##0.00")
        );
        sheet.getRow(i).getCell(1).setCellStyle(currencyStyle);

        autoSize(sheet);
    }

    private void writeMonthlyReport(MonthlySalesReport r, Sheet sheet) {
        sheet.createRow(0).createCell(0).setCellValue("Monthly Sales Report");
        int i = 2;
        sheet.createRow(i).createCell(0).setCellValue("Store ID");
        sheet.getRow(i++).createCell(1).setCellValue(r.store());

        sheet.createRow(i).createCell(0).setCellValue("Year");
        sheet.getRow(i++).createCell(1).setCellValue(r.year());

        sheet.createRow(i).createCell(0).setCellValue("Month");
        sheet.getRow(i++).createCell(1).setCellValue(getMonthName(r.month()));

        sheet.createRow(i).createCell(0).setCellValue("Total Orders");
        sheet.getRow(i++).createCell(1).setCellValue(r.totalOrders());

        sheet.createRow(i).createCell(0).setCellValue("Products Sold");
        sheet.getRow(i++).createCell(1).setCellValue(r.totalProductsSold());

        sheet.createRow(i).createCell(0).setCellValue("Revenue");
        sheet.getRow(i).createCell(1).setCellValue(r.totalRevenue().doubleValue());

        CellStyle currencyStyle = sheet.getWorkbook().createCellStyle();
        currencyStyle.setDataFormat(
                sheet.getWorkbook().createDataFormat().getFormat("$#,##0.00")
        );
        sheet.getRow(i).getCell(1).setCellStyle(currencyStyle);

        autoSize(sheet);
    }

    private void writeSummaryReport(SummaryReport r, Sheet sheet) {
        sheet.createRow(0).createCell(0).setCellValue("Summary Report");
        int i = 2;
        sheet.createRow(i).createCell(0).setCellValue("Store ID");
        sheet.getRow(i++).createCell(1).setCellValue(r.store());

        sheet.createRow(i).createCell(0).setCellValue("Total Orders");
        sheet.getRow(i++).createCell(1).setCellValue(r.totalOrders());

        sheet.createRow(i).createCell(0).setCellValue("Products Sold");
        sheet.getRow(i++).createCell(1).setCellValue(r.totalProductsSold());

        sheet.createRow(i).createCell(0).setCellValue("Revenue");
        sheet.getRow(i).createCell(1).setCellValue(r.totalRevenue().doubleValue());

        CellStyle currencyStyle = sheet.getWorkbook().createCellStyle();
        currencyStyle.setDataFormat(
                sheet.getWorkbook().createDataFormat().getFormat("$#,##0.00")
        );
        sheet.getRow(i).getCell(1).setCellStyle(currencyStyle);

        autoSize(sheet);
    }

    private void writeTopProducts(List<ProductSalesReport> list, Sheet sheet) {
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Product ID");
        header.createCell(1).setCellValue("Product Name");
        header.createCell(2).setCellValue("Quantity Sold");
        header.createCell(3).setCellValue("Revenue");

        CellStyle headerStyle = sheet.getWorkbook().createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);

        for (int j = 0; j < 4; j++) {
            header.getCell(j).setCellStyle(headerStyle);
        }

        int row = 1;
        CellStyle currencyStyle = sheet.getWorkbook().createCellStyle();
        currencyStyle.setDataFormat(
                sheet.getWorkbook().createDataFormat().getFormat("$#,##0.00")
        );

        for (ProductSalesReport p : list) {
            Row r = sheet.createRow(row++);
            r.createCell(0).setCellValue(p.productId());
            r.createCell(1).setCellValue(p.productName());
            r.createCell(2).setCellValue(p.totalSold());

            Cell revenueCell = r.createCell(3);
            revenueCell.setCellValue(p.totalRevenue().doubleValue());
            revenueCell.setCellStyle(currencyStyle);
        }

        autoSize(sheet);
    }

    private void autoSize(Sheet s) {
        for (int i = 0; i < 6; i++) {
            try {
                s.autoSizeColumn(i);
            } catch (Exception ignored) {
                // Ignorar errores de autoSize
            }
        }
    }

    private String getMonthName(int month) {
        String[] monthNames = {
                "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
                "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
        };
        return month >= 1 && month <= 12 ? monthNames[month - 1] : "Mes " + month;
    }
}