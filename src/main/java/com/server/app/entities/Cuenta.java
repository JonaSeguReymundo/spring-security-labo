package com.server.app.entities;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Cuenta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String alias;
    private String moneda;
    private BigDecimal saldoBase;
    private String tipo;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
