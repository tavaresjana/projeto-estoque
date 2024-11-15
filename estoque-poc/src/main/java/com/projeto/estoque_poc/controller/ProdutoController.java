package com.projeto.estoque_poc.controller;

import com.projeto.estoque_poc.model.Produto;
import com.projeto.estoque_poc.service.ProdutoService;
import com.projeto.estoque_poc.util.DataUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

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

        // Formatar o valor total como moeda brasileira
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        String valorTotalFormatado = currencyFormatter.format(valorTotal);

        model.addAttribute("totalProdutos", totalProdutos);
        model.addAttribute("produtosVencer", produtosVencer);
        model.addAttribute("estoqueBaixo", estoqueBaixo);
        model.addAttribute("valorTotal", valorTotalFormatado);

        List<Produto> produtosRecentes = produtoService.buscarPorProdutosRecentes();

        List<Map<String, Object>> produtosComDataFormatada = produtosRecentes.stream().map(produto -> {
            Map<String, Object> produtoMap = Map.of(
                    "id", produto.getId(),
                    "nome", produto.getNome(),
                    "quantidade", produto.getQuantidade(),
                    "dataValidade", DataUtil.formatarData(produto.getDataValidade())
            );
            return produtoMap;
        }).collect(Collectors.toList());

        model.addAttribute("produtosRecentes", produtosComDataFormatada);
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
        List<Map<String, Object>> produtosComDataFormatada = produtos.stream().map(produto -> {
            Map<String, Object> produtoMap = Map.of(
                    "id", produto.getId(),
                    "nome", produto.getNome(),
                    "quantidade", produto.getQuantidade(),
                    "valor", produto.getValor(),
                    "dataValidade", DataUtil.formatarData(produto.getDataValidade())
            );
            return produtoMap;
        }).collect(Collectors.toList());

        model.addAttribute("produtos", produtosComDataFormatada);

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
    @PostMapping("/produtos/editar/{id}")
    public String atualizarProduto(@PathVariable Long id, @ModelAttribute Produto produto) {
        produto.setId(id);  // Define o ID para garantir que estamos atualizando o produto correto
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

    // Exibe o formulário de busca
    @GetMapping("/produtos/relatorio-vencimento-proximo")
    public String exibirFormularioRelatorio(Model model) {
        List<String> tiposRelatorio = List.of("Vencimento Próximos 30 Dias", "Produtos Vencidos", "Estoque Baixo");
        model.addAttribute("tiposRelatorio", tiposRelatorio);
        return "relatorio";
    }

    // Processa o formulário e exibe a lista de produtos com vencimento próximo
    @PostMapping("/produtos/relatorio-vencimento-proximo")
    public String gerarRelatorioProdutosVencimentoProximo(@RequestParam(value = "dias", required = false)
                                                          Integer dias, Model model, RedirectAttributes redirectAttributes) {
        if (dias == null || dias <= 0) {
            redirectAttributes.addFlashAttribute("errorMessage", "Por favor, informe uma quantidade de dias válida.");

            List<String> tiposRelatorio = List.of("Vencimento Próximos 30 Dias", "Produtos Vencidos", "Estoque Baixo");
            model.addAttribute("tiposRelatorio", tiposRelatorio);

            return "redirect:/produtos/relatorio-vencimento-proximo"; // Redireciona para exibir o formulário
        }

        List<Produto> produtosProximosDoVencimento = produtoService.produtosComDataDeValidadeProxima(dias);
        List<Map<String, Object>> produtosComDataFormatada = produtosProximosDoVencimento.stream().map(produto -> {
            Map<String, Object> produtoMap = Map.of(
                    "id", produto.getId(),
                    "nome", produto.getNome(),
                    "quantidade", produto.getQuantidade(),
                    "dataValidade", DataUtil.formatarData(produto.getDataValidade())
            );
            return produtoMap;
        }).collect(Collectors.toList());

        model.addAttribute("produtosProximosDoVencimento", produtosComDataFormatada);

        model.addAttribute("dias", dias); // Passa o valor de dias para exibir no formulário

        List<String> tiposRelatorio = List.of("Vencimento Próximos 30 Dias", "Produtos Vencidos", "Estoque Baixo");
        model.addAttribute("tiposRelatorio", tiposRelatorio);

        return "relatorio";
    }

    @GetMapping("/produtos/buscar")
    public String buscarProduto(@RequestParam String nome, Model model) {
        model.addAttribute("produtos", produtoService.buscarPorNome(nome));
        return "produtos"; // nome da sua página HTML
    }

    @PostMapping("/produtos/entrada/{id}")
    public String adicionarQuantidade(@PathVariable Long id, @RequestParam int quantidade) {
        produtoService.adicionarQuantidade(id, quantidade);
        return "redirect:/produtos"; // redireciona para a página de produtos
    }

    @PostMapping("/produtos/saida/{id}")
    public String subtrairQuantidade(@PathVariable Long id, @RequestParam int quantidade, RedirectAttributes redirectAttributes) {
        Produto produto = produtoService.findById(id) // Método para buscar o produto
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        if (quantidade > produto.getQuantidade()) {
            redirectAttributes.addFlashAttribute("error", "Quantidade a ser subtraída é maior do que a disponível.");
            return "redirect:/produtos"; // redireciona para a página de produtos
        }

        produtoService.subtrairQuantidade(id, quantidade);
        redirectAttributes.addFlashAttribute("success", "Quantidade subtraída com sucesso.");
        return "redirect:/produtos"; // redireciona para a página de produtos
    }

    /*****************
     * pag de usuario
     *****************/
    @GetMapping("/user/produtos")
    public String produtosUsuario(Model model) {
        // Dados do dashboard
        int totalProdutos = produtoService.contarTotalProdutos();
        int produtosVencer = produtoService.contarProdutosAVencer();
        int estoqueBaixo = produtoService.contarEstoqueBaixo();

        // Adiciona as informações do dashboard ao modelo
        model.addAttribute("estoqueBaixo", estoqueBaixo);
        model.addAttribute("totalProdutos", totalProdutos);
        model.addAttribute("produtosVencer", produtosVencer);

        // Adiciona a lista de produtos ao modelo
        List<Produto> produtos = produtoService.buscarTodos();
        List<Map<String, Object>> produtosComDataFormatada = produtos.stream().map(produto -> {
            Map<String, Object> produtoMap = Map.of(
                    "id", produto.getId(),
                    "nome", produto.getNome(),
                    "quantidade", produto.getQuantidade(),
                    "valor", produto.getValor(),
                    "dataValidadeFormatada", DataUtil.formatarData(produto.getDataValidade())
            );
            return produtoMap;
        }).collect(Collectors.toList());

        model.addAttribute("produtos", produtosComDataFormatada);

        return "/user/produtos";
    }

    @GetMapping("/user/produtos/editar/{id}")
    public String editarProdutoRoleUser(@PathVariable Long id, Model model) {
        Produto produto = produtoService.buscarPorId(id);
        model.addAttribute("produto", produto);
        return "/user/editar-produto"; // página HTML para editar o produto
    }

    @PostMapping("/user/produtos/editar/{id}")
    public String atualizarProdutoRoleUser(@PathVariable Long id, @ModelAttribute Produto produto) {
        produto.setId(id);  // Define o ID para garantir que estamos atualizando o produto correto
        produtoService.atualizarProduto(produto);
        return "redirect:/user/produtos";
    }

    @GetMapping("/user/produtos/excluir/{id}")
    public String excluirProdutoRoleUser(@PathVariable Long id) {
        produtoService.excluirProduto(id);
        return "redirect:/user/produtos";
    }

    @GetMapping("/user/produtos/novo")
    public String exibirFormularioNovoProdutoRoleUser(Model model) {
        model.addAttribute("produto", new Produto());
        return "/user/adicionar-produto"; // Nome do template HTML para o formulário
    }

    @PostMapping("/user/produtos/novo")
    public String adicionarProdutoRoleUser(@ModelAttribute Produto produto) {
        produtoService.salvarProduto(produto);
        return "redirect:/user/produtos"; // Redireciona para a lista de produtos após adicionar
    }

    @GetMapping("/user/produtos/buscar")
    public String buscarProdutoRoleUser(@RequestParam String nome, Model model) {
        model.addAttribute("produtos", produtoService.buscarPorNome(nome));
        List<Produto> produtos = produtoService.buscarPorNome(nome);
        formatarDataEAdicionarModelo(produtos, model);

        int totalProdutos = produtoService.contarTotalProdutos();
        int produtosVencer = produtoService.contarProdutosAVencer();
        int estoqueBaixo = produtoService.contarEstoqueBaixo();

        // Adiciona as informações do dashboard ao modelo
        model.addAttribute("estoqueBaixo", estoqueBaixo);
        model.addAttribute("totalProdutos", totalProdutos);
        model.addAttribute("produtosVencer", produtosVencer);


        return "/user/produtos"; // nome da sua página HTML
    }

    @GetMapping("/user/listprodutos")
    public String listarProdutosRoleUser(Model model) {
        List<Produto> produtos = produtoService.buscarTodos();

        formatarDataEAdicionarModelo(produtos, model);

        int totalProdutos = produtoService.contarTotalProdutos();
        int produtosVencer = produtoService.contarProdutosAVencer();
        int estoqueBaixo = produtoService.contarEstoqueBaixo();

        // Adiciona as informações do dashboard ao modelo
        model.addAttribute("estoqueBaixo", estoqueBaixo);
        model.addAttribute("totalProdutos", totalProdutos);
        model.addAttribute("produtosVencer", produtosVencer);

        return "/user/produtos";
    }

    @PostMapping("/user/produtos/entrada/{id}")
    public String adicionarQuantidadeRoleUser(@PathVariable Long id, @RequestParam int quantidade) {
        produtoService.adicionarQuantidade(id, quantidade);
        return "redirect:/user/produtos"; // redireciona para a página de produtos
    }

    @PostMapping("/user/produtos/saida/{id}")
    public String subtrairQuantidadeRoleUser(@PathVariable Long id, @RequestParam int quantidade, RedirectAttributes redirectAttributes) {
        Produto produto = produtoService.findById(id) // Método para buscar o produto
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        if (quantidade > produto.getQuantidade()) {
            redirectAttributes.addFlashAttribute("error", "Quantidade a ser subtraída é maior do que a disponível.");
            return "redirect:/user/produtos"; // redireciona para a página de produtos
        }

        produtoService.subtrairQuantidade(id, quantidade);
        redirectAttributes.addFlashAttribute("success", "Quantidade subtraída com sucesso.");
        return "redirect:/user/produtos"; // redireciona para a página de produtos
    }

    private void formatarDataEAdicionarModelo(List<Produto> produtos, Model model){
        List<Map<String, Object>> produtosComDataFormatada = produtos.stream().map(produto -> {
            Map<String, Object> produtoMap = Map.of(
                    "id", produto.getId(),
                    "nome", produto.getNome(),
                    "quantidade", produto.getQuantidade(),
                    "valor", produto.getValor(),
                    "dataValidade", DataUtil.formatarData(produto.getDataValidade())
            );
            return produtoMap;
        }).collect(Collectors.toList());

        model.addAttribute("produtos", produtosComDataFormatada);
    }
}
