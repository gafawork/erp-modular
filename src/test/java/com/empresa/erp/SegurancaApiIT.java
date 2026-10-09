package com.empresa.erp;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SegurancaApiIT {

    @Autowired MockMvc mvc;

    @Test
    void semCredenciaisRetorna401() throws Exception {
        mvc.perform(get("/api/v1/clientes")).andExpect(status().isUnauthorized());
    }

    @Test
    void perfilEstoqueNaoAcessaVendas() throws Exception {
        mvc.perform(get("/api/v1/vendas").with(httpBasic("estoque", "estoque123")))
                .andExpect(status().isForbidden());
    }

    @Test
    void vendasListaClientes() throws Exception {
        mvc.perform(get("/api/v1/clientes").with(httpBasic("vendas", "vendas123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.number").value(0));
    }

    @Test
    void payloadInvalidoRetornaProblemDetail400() throws Exception {
        mvc.perform(post("/api/v1/produtos").with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sku\":\"\",\"nome\":\"X\",\"precoCusto\":1,\"precoVenda\":0,\"estoqueMinimo\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.sku").exists())
                .andExpect(jsonPath("$.campos.precoVenda").exists());
    }

    @Test
    void regraDeNegocioDeQualquerModuloRetorna422() throws Exception {
        mvc.perform(post("/api/v1/fornecedores").with(httpBasic("compras", "compras123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"razaoSocial\":\"ACME\",\"cnpj\":\"11.222.333/0001-82\",\"email\":\"a@acme.com\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail").value("CNPJ inválido: 11222333000182"));
    }

    @Test
    void produtoInexistenteRetorna404() throws Exception {
        mvc.perform(get("/api/v1/produtos/999999").with(httpBasic("admin", "admin123")))
                .andExpect(status().isNotFound());
    }
}
