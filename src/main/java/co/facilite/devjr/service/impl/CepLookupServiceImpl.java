package co.facilite.devjr.service.impl;

import co.facilite.devjr.domain.enumeration.Uf;
import co.facilite.devjr.service.CepLookupService;
import co.facilite.devjr.service.dto.AddressDTO;
import co.facilite.devjr.service.dto.ViaCepResponse;
import co.facilite.devjr.web.rest.errors.BadRequestAlertException;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class CepLookupServiceImpl implements CepLookupService {

    private final RestTemplate restTemplate;
    private static final String VIACEP_URL = "https://viacep.com.br/ws/%s/json/";

    public CepLookupServiceImpl(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    @Override
    public String normalizeCep(String raw) {
        if (raw == null) {
            throw new BadRequestAlertException("CEP não pode ser nulo", "cepLookup", "cepinvalido");
        }
        return raw.replaceAll("\\D", "");
    }

    @Override
    public AddressDTO lookup(String cep) {
        String cepLimpo = normalizeCep(cep);
        if (cepLimpo == null || cepLimpo.length() != 8) {
            throw new BadRequestAlertException("CEP inválido", "cepLookup", "cepinvalido");
        }
        try {
            String url = String.format(VIACEP_URL, cepLimpo);
            ViaCepResponse response = restTemplate.getForObject(url, ViaCepResponse.class);
            if (response == null || response.getCep() == null) {
                throw new IllegalArgumentException("CEP não encontrado");
            }
            return mapToAddressDTO(response);
        } catch (HttpClientErrorException e) {
            throw new IllegalArgumentException("Erro ao consultar o CEP: " + e.getMessage());
        }
    }

    private AddressDTO mapToAddressDTO(ViaCepResponse response) {
        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setCep(response.getCep());
        addressDTO.setStreet(response.getLogradouro());
        addressDTO.setComplement(response.getComplemento());
        addressDTO.setDistrict(response.getBairro());
        addressDTO.setCity(response.getLocalidade());
        if (response.getUf() != null) {
            addressDTO.setUf(Uf.valueOf(response.getUf()));
        }
        return addressDTO;
    }
}
