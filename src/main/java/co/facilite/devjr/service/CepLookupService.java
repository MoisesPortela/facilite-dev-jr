package co.facilite.devjr.service;

import co.facilite.devjr.service.dto.AddressDTO;

public interface CepLookupService {
    String normalizeCep(String raw);
    AddressDTO lookup(String cep);
}
