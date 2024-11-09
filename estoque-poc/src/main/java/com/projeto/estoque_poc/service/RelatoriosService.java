package com.projeto.estoque_poc.service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import com.projeto.estoque_poc.model.Produto;
import com.projeto.estoque_poc.repository.ProdutoRepository;
import com.projeto.estoque_poc.util.DataUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RelatoriosService {

    @Autowired
    private ProdutoRepository produtoRepository;

    public ByteArrayOutputStream gerarPdfProdutosVencimentoProximo30Dias() {
        LocalDate dataAtual = LocalDate.now();
        LocalDate dataLimite = dataAtual.plusDays(30);
        List<Produto> listaProduto = produtoRepository.findProdutosComDataDeValidadeProxima(dataAtual, dataLimite);
        return gerarPdf(listaProduto);
    }

    public ByteArrayOutputStream gerarPdfProdutosVencidos() {
        LocalDate dataAtual = LocalDate.now();
        List<Produto> produtos = produtoRepository.findProdutosVencidos(dataAtual);
        return gerarPdf(produtos);
    }

    public ByteArrayOutputStream gerarPdfProdutosEstoqueBaixo() {
        List<Produto> produtos = produtoRepository.findProdutosEstoqueBaixo();
        return gerarPdf(produtos);
    }

    private ByteArrayOutputStream gerarPdf(List<Produto> produtos) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Document document = new Document();
        try {
            PdfWriter.getInstance(document, outputStream);
            document.open();
            for (Produto produto : produtos) {
                document.add(new Paragraph("ID: " + produto.getId()));
                document.add(new Paragraph("Nome: " + produto.getNome()));
                document.add(new Paragraph("Quantidade: " + produto.getQuantidade()));
                document.add(new Paragraph("Valor: " + produto.getValor()));
                document.add(new Paragraph("Data de Validade: " + produto.getDataValidade()));
                document.add(new Paragraph(" "));
            }
        } catch (DocumentException e) {
            e.printStackTrace();
        } finally {
            document.close();
        }
        return outputStream;
    }

    public static List<Map<String, Object>> formatarListaDeProdutos(List<Produto> produtos) {
        return produtos.stream().map(produto -> {
            Map<String, Object> produtoMap = new HashMap<>();
            produtoMap.put("id", produto.getId());
            produtoMap.put("nome", produto.getNome());
            produtoMap.put("quantidade", produto.getQuantidade());
            produtoMap.put("valor", produto.getValor());
            produtoMap.put("dataValidade", DataUtil.formatarData(produto.getDataValidade()));
            return produtoMap;
        }).collect(Collectors.toList());
    }
}
