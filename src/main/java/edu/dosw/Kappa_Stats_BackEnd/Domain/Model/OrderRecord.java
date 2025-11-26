package edu.dosw.Kappa_Stats_BackEnd.Domain.Model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "order_records")
public class OrderRecord {

    @Id
    private String id;

    private String orderId;
    private String productId;
    private Integer quantity;
    private BigDecimal totalPrice;
    private LocalDate date;
}
