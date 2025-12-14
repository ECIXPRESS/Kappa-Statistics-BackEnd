package edu.dosw.Kappa_Stats_BackEnd.Application.Services.UseCases;

import java.time.LocalDate;

public record GenerateWeeklyReportCommand(
        String storeId,
        LocalDate anyDateInWeek
) {
    public GenerateWeeklyReportCommand {
        if (storeId == null || storeId.isBlank()) {
            throw new IllegalArgumentException("Store ID cannot be null or empty");
        }
        if (anyDateInWeek == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
    }
}