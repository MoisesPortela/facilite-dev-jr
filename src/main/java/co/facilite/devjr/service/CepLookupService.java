package co.facilite.devjr.service;

import co.facilite.devjr.service.dto.AddressDTO;

/**
 * Service interface for CEP lookup. Implementation lives in
 * co.facilite.devjr.service.impl.CepLookupServiceImpl
 */
public interface CepLookupService {
    String normalizeCep(String raw);

    AddressDTO lookup(String cep);
}
