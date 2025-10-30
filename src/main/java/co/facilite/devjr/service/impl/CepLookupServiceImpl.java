package co.facilite.devjr.service.impl;

import co.facilite.devjr.domain.enumeration.Uf;
import co.facilite.devjr.service.CepLookupService;
import co.facilite.devjr.service.dto.AddressDTO;
import co.facilite.devjr.service.dto.ViaCepResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CepLookupServiceImpl implements CepLookupService {

    private final RestTemplate restTemplate;

    public CepLookupServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public String normalizeCep(String cepNaoFormatado) {
        if (cepNaoFormatado == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CEP não pode ser nulo");
        }
        String cepFormatado = cepNaoFormatado.replaceAll("\\D", "");
        if (cepFormatado.length() != 8) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CEP deve ter 8 digitos");
        }
        return cepFormatado;
    }

    @Override
    public AddressDTO lookup(String cep) {
        String url = String.format("https://viacep.com.br/ws/%s/json/", cep);
        try {
            ViaCepResponse response = restTemplate.getForObject(url, ViaCepResponse.class);
            if (response == null || response.getErro()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "CEP não encontrado");
            }
            return mapToAddressDTO(response);
        } catch (RestClientException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "CEP service unavailable");
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
            addressDTO.setUf(Uf.valueOf(response.getUf().toUpperCase()));
        }
        return addressDTO;
    }
}
