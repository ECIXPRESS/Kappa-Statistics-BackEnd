package edu.dosw.Kappa_Stats_BackEnd.Application.Services.UseCases;


public record GenerateTopProductsCommand(
        String storeId
) {
    public GenerateTopProductsCommand {
        if (storeId == null || storeId.isBlank()) {
            throw new IllegalArgumentException("Store ID cannot be null or empty");
        }
    }
}