package cl.duoc.bancoxyz.ms.validaciones.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.bancoxyz.ms.validaciones.dtos.EstadisticasDTO;
import cl.duoc.bancoxyz.ms.validaciones.services.ValidacionService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/validaciones")
@RequiredArgsConstructor
public class EstadisticasController {

    private final ValidacionService validacionService;

    @GetMapping("/estadisticas")
    public EstadisticasDTO estadisticas() {
        return validacionService.estadisticas();
    }
}
