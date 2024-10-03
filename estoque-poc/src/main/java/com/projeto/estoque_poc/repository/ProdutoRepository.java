package com.projeto.estoque_poc.repository;

import com.projeto.estoque_poc.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    @Query("SELECT COUNT(p) FROM Produto p WHERE p.dataValidade < CURRENT_DATE")
    int contarProdutosAVencer();

    @Query("SELECT COUNT(p) FROM Produto p WHERE p.quantidade < 10")
    int contarEstoqueBaixo();

    @Query("SELECT SUM(p.valor * p.quantidade) FROM Produto p")
    double calcularValorTotal();

    List<Produto> findTop5ByOrderByIdDesc();
}
