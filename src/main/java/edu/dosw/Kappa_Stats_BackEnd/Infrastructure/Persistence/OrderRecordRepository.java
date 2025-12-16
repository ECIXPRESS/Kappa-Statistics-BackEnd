package edu.dosw.Kappa_Stats_BackEnd.Infrastructure.Persistence;

import edu.dosw.Kappa_Stats_BackEnd.Domain.Model.OrderRecord;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface OrderRecordRepository extends MongoRepository<OrderRecord, String> {

    List<OrderRecord> findByDate(LocalDate date);

    List<OrderRecord> findByDateBetween(LocalDate startDate, LocalDate endDate);

    List<OrderRecord> findByStore(String store);

    @Query("{'store': ?0, 'date': {$gte: ?1, $lt: ?2}}")  // $lt excluye el final
    List<OrderRecord> findByStoreAndDateBetween(String store, LocalDate startDate, LocalDate endDate);

    @Query("{'store': ?0, 'date': {$gte: ?1, $lt: ?2}}")
    List<OrderRecord> findByStoreAndDateRange(String store, LocalDate startDate, LocalDate endDate);

    default List<OrderRecord> findByStoreAndDate(String store, LocalDate date) {
        return findByStoreAndDateRange(store, date, date.plusDays(1));
    }

    @Query("{'productId': ?0, 'store': ?1}")
    List<OrderRecord> findByProductIdAndStore(String productId, String store);
}