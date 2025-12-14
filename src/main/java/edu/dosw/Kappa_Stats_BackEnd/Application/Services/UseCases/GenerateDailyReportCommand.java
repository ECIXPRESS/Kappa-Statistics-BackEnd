package edu.dosw.Kappa_Stats_BackEnd.Application.Services.UseCases;

import java.time.LocalDate;

public record GenerateDailyReportCommand(
        String storeId,
        LocalDate date
) {
    public GenerateDailyReportCommand {
        if (storeId == null || storeId.isBlank()) {
            throw new IllegalArgumentException("Store ID cannot be null or empty");
        }
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
    }
}