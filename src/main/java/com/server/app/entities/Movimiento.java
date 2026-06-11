package com.server.app.entities;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Movimiento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private BigDecimal monto;
    private String monedaOriginal;
    private BigDecimal tasaCambio;
    private LocalDateTime fecha = LocalDateTime.now();
    private String descripcion;
    @ManyToOne
    @JoinColumn(name = "cuenta_id")
    private Cuenta cuenta;
    @ManyToOne
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;
}