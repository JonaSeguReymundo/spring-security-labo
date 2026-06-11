package com.server.app.services;

import com.server.app.entities.Cuenta;
import com.server.app.entities.Movimiento;
import com.server.app.repositories.CuentaRepository;
import com.server.app.repositories.MovimientoRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class FinanzasService {
    private final CuentaRepository cuentaRepo;
    private final MovimientoRepository movRepo;

    @Transactional
    public void registrarMovimiento(Movimiento mov) {
        Cuenta cuenta = mov.getCuenta();
        if ("Egreso".equals(mov.getCategoria().getTipo())) {
            cuenta.setSaldoBase(cuenta.getSaldoBase().subtract(mov.getMonto()));
        } else {
            cuenta.setSaldoBase(cuenta.getSaldoBase().add(mov.getMonto()));
        }
        cuentaRepo.save(cuenta);
        movRepo.save(mov);
    }
}