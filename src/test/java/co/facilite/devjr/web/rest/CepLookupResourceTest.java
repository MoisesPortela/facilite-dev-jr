package co.facilite.devjr.web.rest;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import co.facilite.devjr.domain.enumeration.Uf;
import co.facilite.devjr.service.CepLookupService;
import co.facilite.devjr.service.dto.AddressDTO;
import co.facilite.devjr.web.rest.errors.BadRequestAlertException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

/**
 * Testes unitários para CepLookupResource
 * Testa todos os cenários: sucesso, CEP inválido, não encontrado, service indisponível
 */
@ExtendWith(MockitoExtension.class)
class CepLookupResourceTest {

    @Mock
    private CepLookupService cepLookupService;

    @InjectMocks
    private CepLookupResource cepLookupResource;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(cepLookupResource).build();
    }

    /**
     * TESTE 1: CEP válido retorna endereço completo
     */
    @Test
    void getCep_ValidCep_ReturnsAddress() throws Exception {
        String cep = "01310100";
        AddressDTO expectedAddress = new AddressDTO();
        expectedAddress.setCep("01310-100");
        expectedAddress.setStreet("Avenida Paulista");
        expectedAddress.setDistrict("Bela Vista");
        expectedAddress.setCity("São Paulo");
        expectedAddress.setUf(Uf.SP);

        when(cepLookupService.normalizeCep(cep)).thenReturn(cep);
        when(cepLookupService.lookup(cep)).thenReturn(expectedAddress);

        mockMvc
            .perform(get("/api/cep/{cep}", cep))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.cep").value("01310-100"))
            .andExpect(jsonPath("$.street").value("Avenida Paulista"))
            .andExpect(jsonPath("$.district").value("Bela Vista"))
            .andExpect(jsonPath("$.city").value("São Paulo"))
            .andExpect(jsonPath("$.uf").value("SP"));

        verify(cepLookupService).normalizeCep(cep);
        verify(cepLookupService).lookup(cep);
    }

    /**
     * TESTE 2: CEP inválido retorna erro 400
     */
    @Test
    void getCep_InvalidCep_ReturnsBadRequest() throws Exception {
        String invalidCep = "123";

        when(cepLookupService.normalizeCep(invalidCep)).thenThrow(
            new BadRequestAlertException("CEP deve ter 8 digitos", "address", "cepinvalido")
        );

        mockMvc.perform(get("/api/cep/{cep}", invalidCep)).andExpect(status().isBadRequest());

        verify(cepLookupService).normalizeCep(invalidCep);
        verify(cepLookupService, never()).lookup(anyString());
    }

    /**
     * TESTE 3: CEP não encontrado retorna erro 404
     */
    @Test
    void getCep_NotFound_ReturnsNotFound() throws Exception {
        String cep = "99999999";

        when(cepLookupService.normalizeCep(cep)).thenReturn(cep);
        when(cepLookupService.lookup(cep)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "CEP não encontrado"));

        mockMvc.perform(get("/api/cep/{cep}", cep)).andExpect(status().isNotFound());

        verify(cepLookupService).normalizeCep(cep);
        verify(cepLookupService).lookup(cep);
    }

    /**
     * TESTE 4: Service indisponível retorna erro 502
     */
    @Test
    void getCep_ServiceUnavailable_ReturnsBadGateway() throws Exception {
        String cep = "01310100";

        when(cepLookupService.normalizeCep(cep)).thenReturn(cep);
        when(cepLookupService.lookup(cep)).thenThrow(new ResponseStatusException(HttpStatus.BAD_GATEWAY, "CEP service unavailable"));

        mockMvc.perform(get("/api/cep/{cep}", cep)).andExpect(status().isBadGateway());

        verify(cepLookupService).normalizeCep(cep);
        verify(cepLookupService).lookup(cep);
    }
}
