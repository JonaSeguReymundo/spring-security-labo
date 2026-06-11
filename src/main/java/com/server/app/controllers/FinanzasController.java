package com.server.app.controllers;

import com.server.app.entities.Categoria;
import com.server.app.entities.Cuenta;
import com.server.app.entities.Movimiento;
import com.server.app.entities.User;
import com.server.app.repositories.CategoriaRepository;
import com.server.app.repositories.CuentaRepository;
import com.server.app.repositories.MovimientoRepository;
import com.server.app.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/finanzas")
@AllArgsConstructor
public class FinanzasController {
    private final CuentaRepository cuentaRepo;
    private final MovimientoRepository movRepo;
    private final CategoriaRepository catRepo;
    private final UserRepository userRepo;

    @GetMapping("/cuentas")
    public ResponseEntity<?> listarCuentas() {
        return ResponseEntity.ok(cuentaRepo.findByUserId(getAuthenticatedUserId()));
    }

    @PostMapping("/cuentas")
    public ResponseEntity<?> crearCuenta(@RequestBody Cuenta cuenta) {
        User usuarioActual = userRepo.findById(getAuthenticatedUserId()).orElseThrow();
        cuenta.setUser(usuarioActual);
        return ResponseEntity.ok(cuentaRepo.save(cuenta));
    }

    @GetMapping("/movimientos")
    public ResponseEntity<?> listarMovimientos(Pageable pageable) {
        // En lugar de buscar por CuentaUserId directamente,
        // primero busca las cuentas del usuario y luego los movimientos.
        // O mejor aún, usa un método de repositorio que busque movimientos
        // donde la cuenta.user.id sea igual al usuario logueado.

        return ResponseEntity.ok(movRepo.findByCuentaUserId(getAuthenticatedUserId(), pageable));
    }

    @Transactional
    @PostMapping("/transferencias")
    public ResponseEntity<?> realizarTransferencia(@RequestBody Map<String, Object> body) {
        // Extraer datos del JSON
        int idOrigen = (int) body.get("cuentaOrigenId");
        int idDestino = (int) body.get("cuentaDestinoId");
        BigDecimal monto = new BigDecimal(body.get("monto").toString());
        int categoriaId = (int) body.get("categoriaId"); // Nueva línea para recibir la categoría

        // 1. Validar existencia
        Cuenta origen = cuentaRepo.findById(idOrigen).orElseThrow();
        Cuenta destino = cuentaRepo.findById(idDestino).orElseThrow();
        Categoria cat = catRepo.findById(categoriaId)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

        // 2. Validar fondos
        if (origen.getSaldoBase().compareTo(monto) < 0) {
            return ResponseEntity.badRequest().body("Fondos insuficientes");
        }

        // 3. Actualizar saldos
        origen.setSaldoBase(origen.getSaldoBase().subtract(monto));
        destino.setSaldoBase(destino.getSaldoBase().add(monto));
        cuentaRepo.save(origen);
        cuentaRepo.save(destino);

        // 4. Registrar movimiento con la categoría elegida
        Movimiento mov = new Movimiento();
        mov.setMonto(monto);
        mov.setDescripcion("Transferencia de cuenta " + idOrigen + " a " + idDestino);
        mov.setCuenta(origen);
        mov.setCategoria(cat); // Asignamos la categoría seleccionada por el usuario

        movRepo.save(mov);

        return ResponseEntity.ok(Map.of("message", "Transferencia realizada exitosamente con categoría: " + cat.getNombre()));
    }

    @GetMapping("/categorias")
    public ResponseEntity<?> listarCategorias() {
        return ResponseEntity.ok(catRepo.findAll());
    }

    private int getAuthenticatedUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof User) {
            return ((User) principal).getId();
        }

        try {
            return Integer.parseInt(principal.toString());
        } catch (NumberFormatException e) {
            String str = principal.toString();
            String idPart = str.split("id=")[1].split("[,)]")[0];
            return Integer.parseInt(idPart);
        }
    }
}