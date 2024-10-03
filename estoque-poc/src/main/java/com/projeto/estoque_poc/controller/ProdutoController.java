package com.projeto.estoque_poc.controller;

import com.projeto.estoque_poc.model.Produto;
import com.projeto.estoque_poc.service.ProdutoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

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
}
