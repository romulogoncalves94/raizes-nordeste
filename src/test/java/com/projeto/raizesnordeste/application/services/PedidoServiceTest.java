package com.projeto.raizesnordeste.application.services;

import com.projeto.raizesnordeste.application.ports.ICampanhaPort;
import com.projeto.raizesnordeste.application.ports.IEstoquePort;
import com.projeto.raizesnordeste.application.ports.IPedidoRepositoryPort;
import com.projeto.raizesnordeste.application.ports.IProdutoPort;
import com.projeto.raizesnordeste.application.ports.IProgramaFidelidadePort;
import com.projeto.raizesnordeste.application.ports.IUnidadePort;
import com.projeto.raizesnordeste.application.ports.IUsuarioPort;
import com.projeto.raizesnordeste.domain.enums.CanalPedidoEnum;
import com.projeto.raizesnordeste.domain.enums.CategoriaProdutoEnum;
import com.projeto.raizesnordeste.domain.enums.StatusPedidoEnum;
import com.projeto.raizesnordeste.domain.enums.TipoMovimentacaoEstoqueEnum;
import com.projeto.raizesnordeste.domain.model.Campanha;
import com.projeto.raizesnordeste.domain.model.ItemPedido;
import com.projeto.raizesnordeste.domain.model.MovimentacaoEstoque;
import com.projeto.raizesnordeste.domain.model.Pedido;
import com.projeto.raizesnordeste.domain.model.Produto;
import com.projeto.raizesnordeste.presentation.exceptions.BusinessRuleException;
import com.projeto.raizesnordeste.presentation.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.util.Objects.nonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PedidoService")
class PedidoServiceTest {

    @Mock
    private IPedidoRepositoryPort repositoryPort;

    @Mock
    private IUsuarioPort usuarioPort;

    @Mock
    private IUnidadePort unidadePort;

    @Mock
    private IProdutoPort produtoPort;

    @Mock
    private IEstoquePort estoquePort;

    @Mock
    private IProgramaFidelidadePort programaFidelidadePort;

    @Mock
    private ICampanhaPort campanhaPort;

    @InjectMocks
    private PedidoService pedidoService;

    private final UUID idUsuario = UUID.randomUUID();
    private final UUID idUnidade = UUID.randomUUID();

    private Produto getProduto(UUID id, BigDecimal preco) {
        return new Produto(id, "Baião de Dois", preco, CategoriaProdutoEnum.PRATO_PRINCIPAL);
    }

    private ItemPedido getItemPedido(UUID idProduto, Integer quantidade) {
        return new ItemPedido(null, idProduto, null, quantidade, null);
    }

    private Pedido getPedidoValido(List<ItemPedido> itens) {
        Pedido pedido = new Pedido();
        pedido.setIdUsuario(idUsuario);
        pedido.setIdUnidade(idUnidade);
        pedido.setCanalPedido(CanalPedidoEnum.BALCAO);
        pedido.setItens(itens);
        return pedido;
    }

    private void semCampanhaVigente() {
        when(campanhaPort.findMelhorVigente()).thenReturn(Optional.empty());
    }

    private MovimentacaoEstoque movimentacaoEquivalenteA(UUID idUnidade, UUID idProduto, Integer quantidade, TipoMovimentacaoEstoqueEnum tipo) {
        return argThat(m -> nonNull(m)
                && idUnidade.equals(m.getIdUnidade())
                && idProduto.equals(m.getIdProduto())
                && quantidade.equals(m.getQuantidade())
                && tipo == m.getTipo());
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando usuário não é encontrado")
    void deveLancarResourceNotFoundException_quandoUsuarioNaoEncontrado() {
        Pedido pedido = getPedidoValido(new ArrayList<>(List.of(getItemPedido(UUID.randomUUID(), 1))));

        when(usuarioPort.findById(idUsuario)).thenThrow(new ResourceNotFoundException("Usuário não encontrado"));

        assertThatThrownBy(() -> pedidoService.save(pedido))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(repositoryPort, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando unidade não é encontrada")
    void deveLancarResourceNotFoundException_quandoUnidadeNaoEncontrada() {
        Pedido pedido = getPedidoValido(new ArrayList<>(List.of(getItemPedido(UUID.randomUUID(), 1))));

        when(unidadePort.findById(idUnidade)).thenThrow(new ResourceNotFoundException("Unidade não encontrada"));

        assertThatThrownBy(() -> pedidoService.save(pedido))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Deve lançar BusinessRuleException quando pedido está sem itens")
    void deveLancarBusinessRuleException_quandoPedidoSemItens() {
        Pedido pedido = getPedidoValido(new ArrayList<>());

        assertThatThrownBy(() -> pedidoService.save(pedido))
                .isInstanceOf(BusinessRuleException.class);

        verify(produtoPort, never()).findById(any());
    }

    @Test
    @DisplayName("Deve lançar BusinessRuleException quando itens é nulo")
    void deveLancarBusinessRuleException_quandoItensNulo() {
        Pedido pedido = getPedidoValido(null);

        assertThatThrownBy(() -> pedidoService.save(pedido))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando produto do item não é encontrado")
    void deveLancarResourceNotFoundException_quandoProdutoDoItemNaoEncontrado() {
        UUID idProduto = UUID.randomUUID();
        Pedido pedido = getPedidoValido(new ArrayList<>(List.of(getItemPedido(idProduto, 2))));
        when(produtoPort.findById(idProduto)).thenThrow(new ResourceNotFoundException("Produto não encontrado"));

        assertThatThrownBy(() -> pedidoService.save(pedido))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(estoquePort, never()).movimentar(any());
    }

    @Test
    @DisplayName("Deve propagar BusinessRuleException quando estoque é insuficiente")
    void devePropagarBusinessRuleException_quandoEstoqueInsuficiente() {
        UUID idProduto = UUID.randomUUID();
        Pedido pedido = getPedidoValido(new ArrayList<>(List.of(getItemPedido(idProduto, 5))));
        when(produtoPort.findById(idProduto)).thenReturn(getProduto(idProduto, new BigDecimal("10.00")));
        when(estoquePort.movimentar(any(MovimentacaoEstoque.class)))
                .thenThrow(new BusinessRuleException("Quantidade insuficiente em estoque. Disponível: 2, solicitado: 5"));

        assertThatThrownBy(() -> pedidoService.save(pedido))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("insuficiente");

        verify(repositoryPort, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar BusinessRuleException quando desconto de pontos é maior que o valor do pedido")
    void deveLancarBusinessRuleException_quandoDescontoPontosMaiorQueValorDoPedido() {
        UUID idProduto = UUID.randomUUID();
        Pedido pedido = getPedidoValido(new ArrayList<>(List.of(getItemPedido(idProduto, 1))));
        pedido.setPontosResgatados(10000);
        when(produtoPort.findById(idProduto)).thenReturn(getProduto(idProduto, new BigDecimal("10.00")));
        when(estoquePort.movimentar(any(MovimentacaoEstoque.class))).thenReturn(null);

        assertThatThrownBy(() -> pedidoService.save(pedido))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("não pode ser maior");

        verify(programaFidelidadePort, never()).resgatar(any());
        verify(repositoryPort, never()).save(any());
    }

    @Test
    @DisplayName("Deve salvar pedido sem descontos quando sem pontos e sem campanha vigente")
    void deveSalvarPedidoSemDescontos_quandoSemPontosESemCampanhaVigente() {
        UUID idProduto = UUID.randomUUID();
        Pedido pedido = getPedidoValido(new ArrayList<>(List.of(getItemPedido(idProduto, 2))));
        when(produtoPort.findById(idProduto)).thenReturn(getProduto(idProduto, new BigDecimal("10.00")));
        when(estoquePort.movimentar(any(MovimentacaoEstoque.class))).thenReturn(null);
        semCampanhaVigente();
        when(repositoryPort.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Pedido salvo = pedidoService.save(pedido);

        assertThat(salvo.getValorBruto()).isEqualByComparingTo("20.00");
        assertThat(salvo.getValorDescontoPontos()).isEqualByComparingTo("0");
        assertThat(salvo.getValorDescontoCampanha()).isEqualByComparingTo("0");
        assertThat(salvo.getValorTotal()).isEqualByComparingTo("20.00");
        assertThat(salvo.getPontosResgatados()).isZero();
        assertThat(salvo.getIdCampanhaAplicada()).isNull();
        assertThat(salvo.getStatus()).isEqualTo(StatusPedidoEnum.AGUARDANDO_PAGAMENTO);
        verify(estoquePort).movimentar(movimentacaoEquivalenteA(idUnidade, idProduto, 2, TipoMovimentacaoEstoqueEnum.SAIDA));
    }

    @Test
    @DisplayName("Deve aplicar desconto de pontos antes do total quando resgate é válido")
    void deveAplicarDescontoDePontosAntesDoTotal_quandoResgateValido() {
        UUID idProduto = UUID.randomUUID();
        Pedido pedido = getPedidoValido(new ArrayList<>(List.of(getItemPedido(idProduto, 1))));
        pedido.setPontosResgatados(500);
        when(produtoPort.findById(idProduto)).thenReturn(getProduto(idProduto, new BigDecimal("10.00")));
        when(estoquePort.movimentar(any(MovimentacaoEstoque.class))).thenReturn(null);
        semCampanhaVigente();
        when(repositoryPort.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Pedido salvo = pedidoService.save(pedido);

        assertThat(salvo.getValorBruto()).isEqualByComparingTo("10.00");
        assertThat(salvo.getValorDescontoPontos()).isEqualByComparingTo("5.00");
        assertThat(salvo.getValorTotal()).isEqualByComparingTo("5.00");
        assertThat(salvo.getPontosResgatados()).isEqualTo(500);
        verify(programaFidelidadePort).resgatar(any());
    }

    @Test
    @DisplayName("Deve aplicar desconto de campanha sobre valor pós-pontos quando resgate e campanha vigente simultâneos")
    void deveAplicarDescontoDeCampanhaSobreValorPosPontos_quandoResgateECampanhaVigenteSimultaneos() {
        UUID idProduto = UUID.randomUUID();
        Pedido pedido = getPedidoValido(new ArrayList<>(List.of(getItemPedido(idProduto, 1))));
        pedido.setPontosResgatados(500);
        UUID idCampanha = UUID.randomUUID();
        Campanha campanha = new Campanha(idCampanha, "Semana Nordestina", new BigDecimal("10"), null, null, true);

        when(produtoPort.findById(idProduto)).thenReturn(getProduto(idProduto, new BigDecimal("100.00")));
        when(estoquePort.movimentar(any(MovimentacaoEstoque.class))).thenReturn(null);
        when(campanhaPort.findMelhorVigente()).thenReturn(Optional.of(campanha));
        when(repositoryPort.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Pedido salvo = pedidoService.save(pedido);

        assertThat(salvo.getValorBruto()).isEqualByComparingTo("100.00");
        assertThat(salvo.getValorDescontoPontos()).isEqualByComparingTo("5.00");
        assertThat(salvo.getValorDescontoCampanha()).isEqualByComparingTo("9.50");
        assertThat(salvo.getValorTotal()).isEqualByComparingTo("85.50");
        assertThat(salvo.getIdCampanhaAplicada()).isEqualTo(idCampanha);
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando pedido não é encontrado por id")
    void deveLancarResourceNotFoundException_quandoPedidoNaoEncontradoPorId() {
        UUID id = UUID.randomUUID();
        when(repositoryPort.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pedidoService.findById(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Deve buscar todos sem filtro quando canalPedido é nulo")
    void deveBuscarTodosSemFiltro_quandoCanalPedidoNulo() {
        Page<Pedido> pagina = new PageImpl<>(List.of());
        when(repositoryPort.findAll(any(Pageable.class))).thenReturn(pagina);

        Page<Pedido> resultado = pedidoService.findAll(0, 10, "ASC", "id", null);

        assertThat(resultado).isSameAs(pagina);
        verify(repositoryPort, never()).findAllByCanalPedido(any(), any());
    }

    @Test
    @DisplayName("Deve filtrar por canalPedido quando canal é informado")
    void deveFiltrarPorCanalPedido_quandoCanalInformado() {
        Page<Pedido> pagina = new PageImpl<>(List.of());
        when(repositoryPort.findAllByCanalPedido(eq(CanalPedidoEnum.APP), any(Pageable.class))).thenReturn(pagina);

        Page<Pedido> resultado = pedidoService.findAll(0, 10, "ASC", "id", CanalPedidoEnum.APP);

        assertThat(resultado).isSameAs(pagina);
        verify(repositoryPort, never()).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("Deve lançar BusinessRuleException ao atualizar status de pedido já finalizado")
    void deveLancarBusinessRuleException_quandoAtualizarStatusDePedidoJaFinalizado() {
        UUID id = UUID.randomUUID();
        Pedido pedido = getPedidoValido(new ArrayList<>());
        pedido.setId(id);
        pedido.setStatus(StatusPedidoEnum.CANCELADO);
        when(repositoryPort.findById(id)).thenReturn(Optional.of(pedido));

        assertThatThrownBy(() -> pedidoService.updateStatus(id, StatusPedidoEnum.COZINHA))
                .isInstanceOf(BusinessRuleException.class);

        verify(repositoryPort, never()).update(any());
    }

    @Test
    @DisplayName("Deve atualizar status sem acumular pontos quando transição normal")
    void deveAtualizarStatus_semAcumularPontos_quandoTransicaoNormal() {
        UUID id = UUID.randomUUID();
        Pedido pedido = getPedidoValido(new ArrayList<>());
        pedido.setId(id);
        pedido.setStatus(StatusPedidoEnum.AGUARDANDO_PAGAMENTO);
        pedido.setValorTotal(new BigDecimal("30.00"));

        when(repositoryPort.findById(id)).thenReturn(Optional.of(pedido));
        when(repositoryPort.update(pedido)).thenReturn(pedido);

        Pedido atualizado = pedidoService.updateStatus(id, StatusPedidoEnum.COZINHA);

        assertThat(atualizado.getStatus()).isEqualTo(StatusPedidoEnum.COZINHA);
        verify(programaFidelidadePort, never()).acumularPorCompra(any(), any());
    }

    @Test
    @DisplayName("Deve acumular pontos quando status transiciona para ENTREGUE")
    void deveAcumularPontos_quandoStatusTransicionaParaEntregue() {
        UUID id = UUID.randomUUID();
        Pedido pedido = getPedidoValido(new ArrayList<>());
        pedido.setId(id);
        pedido.setStatus(StatusPedidoEnum.PRONTO);
        pedido.setValorTotal(new BigDecimal("30.00"));

        when(repositoryPort.findById(id)).thenReturn(Optional.of(pedido));
        when(repositoryPort.update(pedido)).thenReturn(pedido);

        pedidoService.updateStatus(id, StatusPedidoEnum.ENTREGUE);

        verify(programaFidelidadePort).acumularPorCompra(idUsuario, new BigDecimal("30.00"));
    }

    @Test
    @DisplayName("Deve lançar BusinessRuleException ao cancelar pedido já finalizado")
    void deveLancarBusinessRuleException_quandoCancelarPedidoJaFinalizado() {
        UUID id = UUID.randomUUID();
        Pedido pedido = getPedidoValido(new ArrayList<>());
        pedido.setId(id);
        pedido.setStatus(StatusPedidoEnum.ENTREGUE);
        when(repositoryPort.findById(id)).thenReturn(Optional.of(pedido));

        assertThatThrownBy(() -> pedidoService.cancelar(id))
                .isInstanceOf(BusinessRuleException.class);

        verify(estoquePort, never()).movimentar(any());
    }

    @Test
    @DisplayName("Deve estornar estoque de cada item sem estornar pontos quando pedido sem pontos resgatados")
    void deveEstornarEstoqueDeCadaItem_semEstornarPontos_quandoPedidoSemPontosResgatados() {
        UUID id = UUID.randomUUID();
        UUID idProduto = UUID.randomUUID();
        Pedido pedido = getPedidoValido(new ArrayList<>(List.of(getItemPedido(idProduto, 3))));
        pedido.setId(id);
        pedido.setStatus(StatusPedidoEnum.AGUARDANDO_PAGAMENTO);
        pedido.setPontosResgatados(0);

        when(repositoryPort.findById(id)).thenReturn(Optional.of(pedido));
        when(repositoryPort.update(pedido)).thenReturn(pedido);

        Pedido cancelado = pedidoService.cancelar(id);

        assertThat(cancelado.getStatus()).isEqualTo(StatusPedidoEnum.CANCELADO);
        verify(estoquePort).movimentar(movimentacaoEquivalenteA(idUnidade, idProduto, 3, TipoMovimentacaoEstoqueEnum.ENTRADA));
        verify(programaFidelidadePort, never()).estornarResgate(any(), any());
    }

    @Test
    @DisplayName("Deve estornar pontos quando pedido com pontos resgatados")
    void deveEstornarPontos_quandoPedidoComPontosResgatados() {
        UUID id = UUID.randomUUID();
        UUID idProduto = UUID.randomUUID();
        Pedido pedido = getPedidoValido(new ArrayList<>(List.of(getItemPedido(idProduto, 1))));
        pedido.setId(id);
        pedido.setStatus(StatusPedidoEnum.AGUARDANDO_PAGAMENTO);
        pedido.setPontosResgatados(200);

        when(repositoryPort.findById(id)).thenReturn(Optional.of(pedido));
        when(repositoryPort.update(pedido)).thenReturn(pedido);

        pedidoService.cancelar(id);

        verify(programaFidelidadePort).estornarResgate(idUsuario, 200);
    }
}
