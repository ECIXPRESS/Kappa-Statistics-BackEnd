package edu.dosw.Kappa_Stats_BackEnd.Application.Services.UseCases;

public record GenerateMonthlyReportCommand(
        String storeId,
        int year,
        int month
) {
    public GenerateMonthlyReportCommand {
        if (storeId == null || storeId.isBlank()) {
            throw new IllegalArgumentException("Store ID cannot be null or empty");
        }
        if (year < 2023 || year > 2100) {
            throw new IllegalArgumentException("Invalid year. Must be between 2023 and 2100");
        }
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Invalid month. Must be between 1 and 12");
        }
    }
}