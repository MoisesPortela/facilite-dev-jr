package co.facilite.devjr.web.rest;

import co.facilite.devjr.service.CepLookupService;
import co.facilite.devjr.service.dto.AddressDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class CepLookupResource {

    private final CepLookupService cepLookupService;

    public CepLookupResource(CepLookupService cepLookupService) {
        this.cepLookupService = cepLookupService;
    }

    @GetMapping("/cep/{cep}")
    public ResponseEntity<AddressDTO> getCep(@PathVariable String cep) {
        String normalizedCep = cepLookupService.normalizeCep(cep);
        AddressDTO addressDto = cepLookupService.lookup(normalizedCep);
        return ResponseEntity.ok(addressDto);
    }
}
