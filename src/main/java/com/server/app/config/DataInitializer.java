package com.server.app.config;

import com.server.app.entities.Categoria;
import com.server.app.repositories.CategoriaRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.List;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(CategoriaRepository catRepo) {
        return args -> {
            if (catRepo.count() == 0) {
                Categoria c1 = new Categoria(); c1.setNombre("Salario"); c1.setTipo("Ingreso");
                Categoria c2 = new Categoria(); c2.setNombre("Comida"); c2.setTipo("Egreso");
                Categoria c3 = new Categoria(); c3.setNombre("Transporte"); c3.setTipo("Egreso");
                Categoria c4 = new Categoria(); c4.setNombre("Servicios"); c4.setTipo("Egreso");
                Categoria c5 = new Categoria(); c5.setNombre("Otros Ingresos"); c5.setTipo("Ingreso");

                catRepo.saveAll(List.of(c1, c2, c3, c4, c5));
            }
        };
    }
}