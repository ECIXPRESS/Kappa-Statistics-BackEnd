package edu.dosw.Kappa_Stats_BackEnd.Application.Ports;

import edu.dosw.Kappa_Stats_BackEnd.Application.Ports.OrderRecordRepositoryPort;
import edu.dosw.Kappa_Stats_BackEnd.Domain.Model.OrderRecord;
import edu.dosw.Kappa_Stats_BackEnd.Infrastructure.Persistence.OrderRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OrderRecordRepositoryAdapter implements OrderRecordRepositoryPort {

    private final OrderRecordRepository orderRecordRepository;

    @Override
    public OrderRecord save(OrderRecord orderRecord) {
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
        return orderRecordRepository.findByDate(date);
    }

    @Override
    public List<OrderRecord> findByDateBetween(LocalDate startDate, LocalDate endDate) {
        return orderRecordRepository.findByDateBetween(startDate, endDate);
    }

    @Override
    public List<OrderRecord> findByStore(String store) {
        return orderRecordRepository.findAll().stream()
                .filter(r -> r.getStore() != null && r.getStore().equals(store))
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderRecord> findByStoreAndDateBetween(String store, LocalDate startDate, LocalDate endDate) {
        return orderRecordRepository.findByDateBetween(startDate, endDate).stream()
                .filter(r -> r.getStore() != null && r.getStore().equals(store))
                .collect(Collectors.toList());
    }

    @Override
    public long count() {
        return orderRecordRepository.count();
    }

    @Override
    public void deleteById(String id) {
        orderRecordRepository.deleteById(id);
    }

    @Override
    public boolean existsById(String id) {
        return orderRecordRepository.existsById(id);
    }
}