package com.projeto.estoque_poc.repository;

import com.projeto.estoque_poc.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {
}
