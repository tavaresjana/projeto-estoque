package com.projeto.estoque_poc.controller;

import com.projeto.estoque_poc.model.Produto;
import com.projeto.estoque_poc.service.ProdutoService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Controller
public class ProdutoController {

    @Autowired
    private ProdutoService produtoService;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file, Model model) {
        try {
            produtoService.salvarPlanilha(file);
            model.addAttribute("message", "Planilha enviada e dados salvos com sucesso!");
        } catch (Exception e) {
            model.addAttribute("message", "Erro ao processar a planilha: " + e.getMessage());
        }
        return "index";
    }

    @GetMapping("/dashboard")
    public String mostrarDashboard(Model model) {
        // Obtendo dados do backend via ProdutoService
        int totalProdutos = produtoService.contarTotalProdutos();
        int produtosVencer = produtoService.contarProdutosAVencer();
        int estoqueBaixo = produtoService.contarEstoqueBaixo();
        double valorTotal = produtoService.calcularValorTotal();
        List<Produto> produtosRecentes = produtoService.listarProdutosRecentes();

        // Adicionando os dados ao modelo para que Thymeleaf possa renderizá-los
        model.addAttribute("totalProdutos", totalProdutos);
        model.addAttribute("produtosVencer", produtosVencer);
        model.addAttribute("estoqueBaixo", estoqueBaixo);
        model.addAttribute("valorTotal", valorTotal);
        model.addAttribute("produtosRecentes", produtosRecentes);

        return "dashboard";  // Nome da página que será renderizada
    }

    @GetMapping("/produtos")
    public String listarProdutos(Model model) {
        List<Produto> produtos = produtoService.buscarTodos(); // Busca todos os produtos do banco
        model.addAttribute("produtos", produtos); // Adiciona a lista de produtos ao modelo
        return "produtos"; // Nome da página HTML
    }

    @GetMapping("/gerar-pdf")
    public ResponseEntity<byte[]> gerarPdfProdutos() {
        try {
            ByteArrayOutputStream pdfStream = produtoService.gerarPdfProdutos();
            byte[] pdfBytes = pdfStream.toByteArray();

            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type", "application/pdf");
            headers.add("Content-Disposition", "attachment; filename=produtos.pdf");

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (IOException e) {
            e.printStackTrace(); // Tratar o erro de forma mais adequada em produção
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao gerar PDF".getBytes());
        }
    }

}
