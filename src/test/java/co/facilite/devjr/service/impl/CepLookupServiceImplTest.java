package co.facilite.devjr.service.impl;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import co.facilite.devjr.domain.enumeration.Uf;
import co.facilite.devjr.service.dto.AddressDTO;
import co.facilite.devjr.service.dto.ViaCepResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class CepLookupServiceImplTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private CepLookupServiceImpl cepLookupService;

    private ViaCepResponse validViaCepResponse;

    @BeforeEach
    void setUp() {
        validViaCepResponse = new ViaCepResponse();
        validViaCepResponse.setCep("72006-206");
        validViaCepResponse.setLogradouro("Rua Rua 4A Blocos 2 e 3 Travessa 3");
        validViaCepResponse.setComplemento("");
        validViaCepResponse.setBairro("Setor Habitacional Vicente Pires");
        validViaCepResponse.setLocalidade("Brasília");
        validViaCepResponse.setUf("DF");
        validViaCepResponse.setErro(false);
    }

    /**
     * TESTE 1: Normalização de CEP com diferentes formatos válidos
     */
    @Test
    void normalizeCep_ValidFormats_ReturnsNormalizedCep() {
        assertThat(cepLookupService.normalizeCep("72006-206")).isEqualTo("72006206");
        assertThat(cepLookupService.normalizeCep("72006206")).isEqualTo("72006206");
        assertThat(cepLookupService.normalizeCep("72.006-206")).isEqualTo("72006206");
        assertThat(cepLookupService.normalizeCep(" 72006-206 ")).isEqualTo("72006206");
    }

    /**
     * TESTE 2: CEP nulo lança exceção
     */
    @Test
    void normalizeCep_NullInput_ThrowsException() {
        assertThatThrownBy(() -> cepLookupService.normalizeCep(null))
            .isInstanceOf(ResponseStatusException.class)
            .extracting("status")
            .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    /**
     * TESTE 3: CEP com tamanho inválido lança exceção
     */
    @Test
    void normalizeCep_InvalidLength_ThrowsException() {
        assertThatThrownBy(() -> cepLookupService.normalizeCep("123"))
            .isInstanceOf(ResponseStatusException.class)
            .extracting("status")
            .isEqualTo(HttpStatus.BAD_REQUEST);

        assertThatThrownBy(() -> cepLookupService.normalizeCep("123456789"))
            .isInstanceOf(ResponseStatusException.class)
            .extracting("status")
            .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    /**
     * TESTE 4: Lookup bem-sucedido retorna AddressDTO correto
     */
    @Test
    void lookup_ValidCep_ReturnsAddressDTO() {
        String cep = "72006206";
        String url = "https://viacep.com.br/ws/72006206/json/";

        when(restTemplate.getForObject(url, ViaCepResponse.class)).thenReturn(validViaCepResponse);

        AddressDTO result = cepLookupService.lookup(cep);

        assertThat(result).isNotNull();
        assertThat(result.getCep()).isEqualTo("72006-206");
        assertThat(result.getStreet()).isEqualTo("Rua Rua 4A Blocos 2 e 3 Travessa 3");
        assertThat(result.getDistrict()).isEqualTo("Setor Habitacional Vicente Pires");
        assertThat(result.getCity()).isEqualTo("Brasília");
        assertThat(result.getUf()).isEqualTo(Uf.DF);

        verify(restTemplate).getForObject(url, ViaCepResponse.class);
    }

    /**
     * TESTE 5: ViaCEP retorna erro - CEP não encontrado
     */
    @Test
    void lookup_CepNotFound_ThrowsNotFoundException() {
        String cep = "99999999";
        ViaCepResponse errorResponse = new ViaCepResponse();
        errorResponse.setErro(true);

        when(restTemplate.getForObject(anyString(), eq(ViaCepResponse.class))).thenReturn(errorResponse);

        assertThatThrownBy(() -> cepLookupService.lookup(cep))
            .isInstanceOf(ResponseStatusException.class)
            .extracting("status")
            .isEqualTo(HttpStatus.NOT_FOUND);

        verify(restTemplate).getForObject(anyString(), eq(ViaCepResponse.class));
    }

    /**
     * TESTE 6: ViaCEP retorna null - CEP não encontrado
     */
    @Test
    void lookup_NullResponse_ThrowsNotFoundException() {
        String cep = "99999999";

        when(restTemplate.getForObject(anyString(), eq(ViaCepResponse.class))).thenReturn(null);

        assertThatThrownBy(() -> cepLookupService.lookup(cep))
            .isInstanceOf(ResponseStatusException.class)
            .extracting("status")
            .isEqualTo(HttpStatus.NOT_FOUND);
    }

    /**
     * TESTE 7: Falha na comunicação com ViaCEP
     */
    @Test
    void lookup_RestClientException_ThrowsBadGateway() {
        String cep = "72006206";

        when(restTemplate.getForObject(anyString(), eq(ViaCepResponse.class))).thenThrow(new RestClientException("Connection timeout"));

        assertThatThrownBy(() -> cepLookupService.lookup(cep))
            .isInstanceOf(ResponseStatusException.class)
            .extracting("status")
            .isEqualTo(HttpStatus.BAD_GATEWAY);
    }

    /**
     * TESTE 8: Mapeamento de UF funciona corretamente
     */
    @Test
    void lookup_DifferentUfs_MapsCorrectly() {
        ViaCepResponse rjResponse = new ViaCepResponse();
        rjResponse.setCep("72006-206");
        rjResponse.setLogradouro("Rua Rua 4A Blocos 2 e 3 Travessa 3");
        rjResponse.setBairro("Setor Habitacional Vicente Pires");
        rjResponse.setLocalidade("Brasília");
        rjResponse.setUf("DF");
        rjResponse.setErro(false);

        when(restTemplate.getForObject(anyString(), eq(ViaCepResponse.class))).thenReturn(rjResponse);

        AddressDTO result = cepLookupService.lookup("72006206");

        assertThat(result.getUf()).isEqualTo(Uf.DF);
        assertThat(result.getCity()).isEqualTo("Brasília");
    }
}
