package com.projeto.estoque_poc.controller;

import com.projeto.estoque_poc.model.Produto;
import com.projeto.estoque_poc.service.ProdutoService;
import com.projeto.estoque_poc.service.RelatoriosService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

@Controller
public class RelatoriosController {

    @Autowired
    private RelatoriosService relatoriosService;


    @Autowired
    private ProdutoService produtoService;

    @GetMapping("/relatorio")
    public String relatorios(Model model) {
        model.addAttribute("tiposRelatorio", new String[]{
                "Vencimento Próximos 30 Dias",
                "Produtos Vencidos",
                "Estoque Baixo"
        });
        return "relatorio";
    }

    @GetMapping("/gerarRelatorio")
    public String gerarRelatorio(@RequestParam String tipo, Model model) {
        List<Produto> produtos = new ArrayList<>();
        String relatorioTitulo = "";

        switch (tipo) {
            case "Vencimento Próximos 30 Dias":
                produtos = produtoService.produtosComDataDeValidadeProxima(30);
                relatorioTitulo = "Relatório de Produtos com Vencimento nos Próximos 30 Dias";
                break;
            case "Produtos Vencidos":
                produtos = produtoService.listarProdutosVencidos();
                relatorioTitulo = "Relatório de Produtos Vencidos";
                break;
            case "Estoque Baixo":
                produtos = produtoService.listarProdutosEstoqueBaixo();
                relatorioTitulo = "Relatório de Produtos com Estoque Baixo";
                break;
            default:
                break;
        }

        model.addAttribute("produtos", produtos);
        model.addAttribute("titulo", relatorioTitulo);
        model.addAttribute("tipoRelatorio", tipo);
        return "relatorio";
    }


    @GetMapping("/relatorios/gerar-pdf")
    public ResponseEntity<byte[]> gerarPdf(@RequestParam String tipo) {
        ByteArrayOutputStream pdfStream;
        switch (tipo) {
            case "Vencimento Próximos 30 Dias":
                pdfStream = relatoriosService.gerarPdfProdutosVencimentoProximo30Dias();
                break;
            case "Produtos Vencidos":
                pdfStream = relatoriosService.gerarPdfProdutosVencidos();
                break;
            case "Estoque Baixo":
                pdfStream = relatoriosService.gerarPdfProdutosEstoqueBaixo();
                break;
            default:
                return ResponseEntity.badRequest().build();
        }

        byte[] pdfBytes = pdfStream.toByteArray();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/pdf");
        headers.add("Content-Disposition", "attachment; filename=" + tipo.replaceAll(" ", "_") + ".pdf");

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
}
