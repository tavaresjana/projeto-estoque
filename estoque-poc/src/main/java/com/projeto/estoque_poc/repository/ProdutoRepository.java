package com.projeto.estoque_poc.repository;

import com.projeto.estoque_poc.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    // Exemplo de método para contar produtos que vencem nos próximos 30 dias
    int countByDataValidadeBefore(LocalDate data);

    // Exemplo de método para contar produtos com estoque abaixo de um valor definido
    int countByQuantidadeLessThan(int quantidade);

    // Exemplo de método para somar o valor total dos produtos
    @Query("SELECT SUM(p.valor * p.quantidade) FROM Produto p")
    Double sumValorTotalProdutos();

    @Query("SELECT SUM(p.quantidade) FROM Produto p")
    Integer sumQuantidadeTotal();
}
