package co.facilite.devjr.service;

import co.facilite.devjr.service.dto.AddressDTO;
import co.facilite.devjr.service.dto.ViaCepResponse;
import co.facilite.devjr.domain.enumeration.Uf;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;

@Service
public class CepLookupService {
    private final RestTemplate restTemplate;
    private static final String VIACEP_URL = "https://viacep.com.br/ws/{cep}/json/";

    public CepLookupService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = new RestTemplate();
    }

    public String limpadorCaracterCep(String enderecoBruto) {
        if(enderecoBruto == null) {
            return  throw new BadRequestAlertException("CEP não pode ser nulo", "cepLookup", "cepinvalido");
        }
        return enderecoBruto.replaceAll("\\D", "");
    }
    AddressDTO lookup(String cep){
        String cepLimpo = limpadorCaracterCep(cep);
        if(cepLimpo == null || cepLimpo.length() != 8){
            throw new BadRequestAlertException("CEP inválido", "cepLookup", "cepinvalido");
        }
        try{
            String url = String.format (VIACEP_URL,limpadorCaracterCep);
            ViaCepResponse response = restTemplate.getForObject(url, ViaCepResponse.class);
            if ( response == null || response.getCep() ==null){
                throw new IllegalArgumentException("CEP não encontrado");
            }
            return mapToAddressDTO(response);
        }catch (HttpClientErrorException e){
            throw new IllegalArgumentException("Erro ao consultar o CEP: " + e.getMessage());
        }
    }
    private AddressDTO mapToAddressDTO(ViaCepResponse response){
        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setCep(response.getCep());
        addressDTO.setStreet (response.getLogradouro());
        addressDTO.setComplement (response.getComplemento());
        addressDTO.setDistrict (response.getBairro());
        addressDTO.setCity (response.getLocalidade());
        addressDTO.setUf (Uf.valueOf(response.getUf()));
        return addressDTO;
    }
}
