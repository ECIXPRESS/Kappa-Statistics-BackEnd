package edu.dosw.Kappa_Stats_BackEnd.Application.Ports;

import edu.dosw.Kappa_Stats_BackEnd.Domain.Model.OrderRecord;
import edu.dosw.Kappa_Stats_BackEnd.Infrastructure.Persistence.OrderRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderRecordRepositoryAdapter implements OrderRecordRepositoryPort {

    private final OrderRecordRepository orderRecordRepository;

    @Override
    public OrderRecord save(OrderRecord orderRecord) {
        log.debug("Guardando OrderRecord: {}", orderRecord);
        return orderRecordRepository.save(orderRecord);
    }

    @Override
    public List<OrderRecord> findAll() {
        return orderRecordRepository.findAll();
    }

    @Override
    public Optional<OrderRecord> findById(String id) {
        return orderRecordRepository.findById(id);
    }

    @Override
    public List<OrderRecord> findByDate(LocalDate date) {
        log.debug("Buscando por fecha: {}", date);
        return orderRecordRepository.findByDate(date);
    }

    @Override
    public List<OrderRecord> findByDateBetween(LocalDate startDate, LocalDate endDate) {
        log.debug("Buscando entre fechas: {} y {}", startDate, endDate);
        return orderRecordRepository.findByDateBetween(startDate, endDate);
    }

    @Override
    public List<OrderRecord> findByStore(String store) {
        log.debug("Buscando por tienda: {}", store);
        return orderRecordRepository.findByStore(store);
    }

    @Override
    public List<OrderRecord> findByStoreAndDateBetween(String store, LocalDate startDate, LocalDate endDate) {
        log.debug("Buscando por tienda {} entre {} y {}", store, startDate, endDate);
        return orderRecordRepository.findByStoreAndDateBetween(store, startDate, endDate);
    }

    @Override
    public List<OrderRecord> findByStoreAndDate(String store, LocalDate date) {
        log.debug("Buscando por tienda {} y fecha: {}", store, date);

        List<OrderRecord> result = orderRecordRepository.findByStoreAndDate(store, date);

        log.debug("Encontrados {} registros para tienda {} en fecha {}",
                result.size(), store, date);

        if (!result.isEmpty()) {
            result.forEach(r ->
                    log.debug("  - {}: {} unidades de {}",
                            r.getDate(), r.getQuantity(), r.getProductName())
            );
        }

        return result;
    }

    @Override
    public long count() {
        return orderRecordRepository.count();
    }

    @Override
    public void deleteById(String id) {
        log.debug("Eliminando OrderRecord con ID: {}", id);
        orderRecordRepository.deleteById(id);
    }

    @Override
    public boolean existsById(String id) {
        return orderRecordRepository.existsById(id);
    }
}