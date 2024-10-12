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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Controller
public class ProdutoController {

    @Autowired
    private ProdutoService produtoService;

    @GetMapping("/")
    public String index(Model model) {
        int totalProdutos = produtoService.contarTotalProdutos();
        int produtosVencer = produtoService.contarProdutosAVencer();
        int estoqueBaixo = produtoService.contarEstoqueBaixo();
        double valorTotal = produtoService.calcularValorTotal();

        model.addAttribute("totalProdutos", totalProdutos);
        model.addAttribute("produtosVencer", produtosVencer);
        model.addAttribute("estoqueBaixo", estoqueBaixo);
        model.addAttribute("valorTotal", valorTotal);

        return "index";
    }

    @PostMapping("/upload")
    public String handleFileUpload(@RequestParam("file") MultipartFile file, Model model) {
        try {
            produtoService.salvarPlanilha(file);
            model.addAttribute("message", "Planilha enviada e dados salvos com sucesso!");
            return "redirect:/";
        } catch (Exception e) {
            model.addAttribute("message", "Erro ao processar a planilha: " + e.getMessage());
            return "index";
        }
    }


    @GetMapping("/produtos")
    public String listarProdutos(Model model) {
        List<Produto> produtos = produtoService.buscarTodos();
        model.addAttribute("produtos", produtos);
        return "produtos";
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

    @GetMapping("/produtos/editar/{id}")
    public String editarProduto(@PathVariable Long id, Model model) {
        Produto produto = produtoService.buscarPorId(id);
        model.addAttribute("produto", produto);
        return "editar-produto"; // página HTML para editar o produto
    }

    // Atualizar o produto
    @PostMapping("/produtos/editar")
    public String atualizarProduto(@ModelAttribute Produto produto) {
        produtoService.atualizarProduto(produto);
        return "redirect:/produtos";
    }

    // Excluir o produto
    @GetMapping("/produtos/excluir/{id}")
    public String excluirProduto(@PathVariable Long id) {
        produtoService.excluirProduto(id);
        return "redirect:/produtos";
    }

    // Exibe o formulário para adicionar um novo produto
    @GetMapping("/produtos/novo")
    public String exibirFormularioNovoProduto(Model model) {
        model.addAttribute("produto", new Produto());
        return "adicionar-produto"; // Nome do template HTML para o formulário
    }

    // Processa o formulário e adiciona o novo produto
    @PostMapping("/produtos/novo")
    public String adicionarProduto(@ModelAttribute Produto produto) {
        produtoService.salvarProduto(produto);
        return "redirect:/produtos"; // Redireciona para a lista de produtos após adicionar
    }
}
