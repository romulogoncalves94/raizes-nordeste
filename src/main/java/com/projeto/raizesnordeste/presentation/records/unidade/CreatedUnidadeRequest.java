package com.projeto.raizesnordeste.presentation.records.unidade;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.br.CNPJ;

public record CreatedUnidadeRequest(
        @Schema(description = "Razão social da unidade", example = "Raízes do Nordeste Boa Viagem LTDA")
        @NotBlank(message = "O campo RAZAO_SOCIAL não pode ser nulo ou vazio")
        @Size(min = 5, max = 150, message = "O campo RAZAO_SOCIAL deve ter entre 5 e 150 caracteres.")
        String razaoSocial,

        @Schema(description = "CNPJ", example = "12.345.678/0001-90")
        @NotBlank(message = "O campo CNPJ não pode ser nulo ou vazio")
        @CNPJ(message = "O campo CNPJ deve ser um CNPJ válido.")
        @Size(min = 18, max = 18, message = "O campo CNPJ deve ter 18 caracteres.")
        String cnpj,

        @Schema(description = "CEP (somente números)", example = "50030230")
        @NotBlank(message = "O campo CEP não pode ser nulo ou vazio")
        @Pattern(regexp = "\\d{8}", message = "O campo CEP deve conter 8 dígitos numéricos.")
        String cep,

        @Schema(description = "Logradouro", example = "Av. Boa Viagem, 1000")
        @Size(max = 255, message = "O campo LOGRADOURO deve ter no máximo 255 caracteres.")
        String logradouro,

        @Schema(description = "Bairro", example = "Boa Viagem")
        @Size(max = 255, message = "O campo BAIRRO deve ter no máximo 255 caracteres.")
        String bairro,

        @Schema(description = "UF", example = "PE")
        @NotBlank(message = "O campo UF não pode ser nulo ou vazio")
        @Size(min = 2, max = 2, message = "O campo UF deve ter 2 caracteres.")
        String uf
) {
}
