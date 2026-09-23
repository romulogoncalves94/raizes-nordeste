package com.projeto.raizesnordeste.presentation.api;

import com.projeto.raizesnordeste.presentation.exceptions.StandardError;
import com.projeto.raizesnordeste.presentation.records.campanha.CampanhaResponse;
import com.projeto.raizesnordeste.presentation.records.campanha.CreatedCampanhaRequest;
import com.projeto.raizesnordeste.presentation.records.campanha.UpdateCampanhaRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Tag(name = "CampanhaController", description = "Controller responsável pelas campanhas e promoções")
@RequestMapping("/api/campanhas")
public interface ICampanhaController {

    @Operation(summary = "Criar uma nova campanha",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Campanha criada com sucesso"),
                    @ApiResponse(responseCode = "400", description = "Requisição inválida", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = StandardError.class)))
            })
    @PostMapping
    ResponseEntity<CampanhaResponse> save(@Valid @RequestBody final CreatedCampanhaRequest request);

    @Operation(summary = "Buscar campanha por id",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Campanha encontrada com sucesso"),
                    @ApiResponse(responseCode = "404", description = "Campanha não encontrada", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = StandardError.class)))
            })
    @GetMapping("/{id}")
    ResponseEntity<CampanhaResponse> findById(
            @NotNull(message = "O id da Campanha precisa ser informado")
            @Parameter(description = "Id da Campanha", required = true, example = "aaa4735d-35b0-49ee-9dfb-aad6c3ff3125")
            @PathVariable UUID id
    );

    @Operation(summary = "Buscar todas as campanhas",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista de campanhas retornada com sucesso")
            })
    @GetMapping
    ResponseEntity<Page<CampanhaResponse>> findAll(
            @Parameter(description = "Número da página", example = "0", required = true)
            @RequestParam(name = "page", defaultValue = "0") final Integer page,

            @Parameter(description = "Linhas por página", example = "10", required = true)
            @RequestParam(name = "linesPerPage", defaultValue = "10") final Integer linesPerPage,

            @Parameter(description = "Ordenação", example = "ASC", required = true)
            @RequestParam(name = "direction", defaultValue = "ASC") final String direction,

            @Parameter(description = "Ordenar por atributo", example = "id", required = true)
            @RequestParam(name = "orderBy", defaultValue = "id") final String orderBy
    );

    @Operation(summary = "Listar campanhas vigentes no momento (ativa=true e dentro do período)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista de campanhas vigentes retornada com sucesso")
            })
    @GetMapping("/vigentes")
    ResponseEntity<List<CampanhaResponse>> findCampanhasVigentes();

    @Operation(summary = "Atualizar campanha",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Campanha atualizada com sucesso"),
                    @ApiResponse(responseCode = "400", description = "Requisição inválida", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = StandardError.class))),
                    @ApiResponse(responseCode = "404", description = "Campanha não encontrada", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = StandardError.class)))
            })
    @PutMapping("/{id}")
    ResponseEntity<CampanhaResponse> update(
            @Parameter(description = "Id da Campanha", required = true, example = "aaa4735d-35b0-49ee-9dfb-aad6c3ff3125")
            @PathVariable UUID id,

            @Valid @RequestBody final UpdateCampanhaRequest request
    );

    @Operation(summary = "Excluir campanha",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Campanha removida com sucesso"),
                    @ApiResponse(responseCode = "404", description = "Campanha não encontrada", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = StandardError.class)))
            })
    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(
            @NotNull(message = "O id da Campanha precisa ser informado")
            @Parameter(description = "Id da Campanha", required = true, example = "aaa4735d-35b0-49ee-9dfb-aad6c3ff3125")
            @PathVariable UUID id
    );
}
