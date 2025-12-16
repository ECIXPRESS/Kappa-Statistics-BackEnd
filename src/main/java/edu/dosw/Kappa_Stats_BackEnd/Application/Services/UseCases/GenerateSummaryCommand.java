package edu.dosw.Kappa_Stats_BackEnd.Application.Services.UseCases;

public record GenerateSummaryCommand(
        String storeId
) {
    public GenerateSummaryCommand {
        if (storeId == null || storeId.isBlank()) {
            throw new IllegalArgumentException("Store ID cannot be null or empty");
        }
    }
}