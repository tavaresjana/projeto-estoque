package com.projeto.estoque_poc.repository;

import com.projeto.estoque_poc.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    // conta produtos que vencem nos próximos 30 dias (valor setado na service)
    int countByDataValidadeBefore(LocalDate data);

    // conta produtos com estoque abaixo de 10 (valor setado na service)
    int countByQuantidadeLessThan(int quantidade);

    // soma o valor total dos produtos em estoque
    @Query("SELECT SUM(p.valor * p.quantidade) FROM Produto p")
    Double sumValorTotalProdutos();

    // soma o total de produtos em estoque
    @Query("SELECT SUM(p.quantidade) FROM Produto p")
    Integer sumQuantidadeTotal();

    // busca produtos com data de validade próxima
    @Query("SELECT p FROM Produto p WHERE p.dataValidade BETWEEN :dataAtual AND :dataLimite")
    List<Produto> findProdutosComDataDeValidadeProxima(@Param("dataAtual") LocalDate dataAtual, @Param("dataLimite") LocalDate dataLimite);

    // busca produtos vencidos
    @Query("SELECT p FROM Produto p WHERE p.dataValidade < :dataAtual")
    List<Produto> findProdutosVencidos(LocalDate dataAtual);

    // busca produtos com estoque baixo
    @Query("SELECT p FROM Produto p WHERE p.quantidade < 10")
    List<Produto> findProdutosEstoqueBaixo();

    List<Produto> findByNomeContainingIgnoreCase(String nome);


}
