package edu.dosw.Kappa_Stats_BackEnd.Application.Ports;

import edu.dosw.Kappa_Stats_BackEnd.Domain.Model.OrderRecord;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface OrderRecordRepositoryPort {

    OrderRecord save(OrderRecord orderRecord);

    List<OrderRecord> findAll();

    Optional<OrderRecord> findById(String id);

    List<OrderRecord> findByDate(LocalDate date);

    List<OrderRecord> findByDateBetween(LocalDate startDate, LocalDate endDate);

    List<OrderRecord> findByStore(String store);

    List<OrderRecord> findByStoreAndDate(String store, LocalDate date);

    List<OrderRecord> findByStoreAndDateBetween(String store, LocalDate startDate, LocalDate endDate);

    long count();

    void deleteById(String id);

    boolean existsById(String id);
}