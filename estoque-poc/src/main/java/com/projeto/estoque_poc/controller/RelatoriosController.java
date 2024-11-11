package com.projeto.estoque_poc.controller;

import com.projeto.estoque_poc.model.Produto;
import com.projeto.estoque_poc.service.ProdutoService;
import com.projeto.estoque_poc.service.RelatoriosService;
import com.projeto.estoque_poc.util.DataUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.ByteArrayOutputStream;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import static com.projeto.estoque_poc.service.RelatoriosService.formatarListaDeProdutos;

@Controller
public class RelatoriosController {

    @Autowired
    private RelatoriosService relatoriosService;


    @Autowired
    private ProdutoService produtoService;

    //pagina html relatorio
    @GetMapping("/relatorio")
    public String relatorios(Model model) {
        List<String> tiposRelatorio = List.of("Vencimento Próximos 30 Dias", "Produtos Vencidos", "Estoque Baixo");
        model.addAttribute("tiposRelatorio", tiposRelatorio);
        return "relatorio";
    }

    @GetMapping("/gerarRelatorio")
    public String gerarRelatorio(@RequestParam String tipo, Model model, RedirectAttributes redirectAttributes) {

        if (tipo == null || tipo.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage1", "Selecione um tipo válido.");
            return "redirect:/relatorio"; // Redireciona para exibir o formulário
        }

        List<Map<String, Object>> produtos = new ArrayList<>();
        String relatorioTitulo = "";

        switch (tipo) {
            case "Vencimento Próximos 30 Dias":
                produtos = relatoriosService.formatarListaDeProdutos(produtoService.produtosComDataDeValidadeProxima(30));
                relatorioTitulo = "Relatório de Produtos com Vencimento nos Próximos 30 Dias";
                break;
            case "Produtos Vencidos":
                produtos = relatoriosService.formatarListaDeProdutos(produtoService.listarProdutosVencidos());
                relatorioTitulo = "Relatório de Produtos Vencidos";
                break;
            case "Estoque Baixo":
                produtos = relatoriosService.formatarListaDeProdutos(produtoService.listarProdutosEstoqueBaixo());
                relatorioTitulo = "Relatório de Produtos com Estoque Baixo";
                break;
            default:
                break;
        }

        // Adiciona o conteúdo para o relatório
        model.addAttribute("produtos", produtos);
        model.addAttribute("titulo", relatorioTitulo);


        model.addAttribute("tipoSelecionado", tipo);

        // Certifique-se de que a lista de tipos sempre é carregada ao gerar o relatório
        model.addAttribute("tiposRelatorio", List.of("Vencimento Próximos 30 Dias", "Produtos Vencidos", "Estoque Baixo"));

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


    /*******************
    * pagina html index *
    *******************/

    @GetMapping("/relatorio-index")
    public String relatoriosIndex(Model model) {
        model.addAttribute("tiposRelatorio", new String[]{
                "Vencimento Próximos 30 Dias",
                "Produtos Vencidos",
                "Estoque Baixo"
        });
        return "index";
    }

    @GetMapping("/gerarRelatorio-index")
    public String gerarRelatorioIndex(@RequestParam String tipo, Model model, RedirectAttributes redirectAttributes) {

        if (tipo == null || tipo.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Selecione algo válido.");
            return "redirect:/relatorio"; // Redireciona para a página de relatórios
        }

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

        model.addAttribute("tiposRelatorio", List.of("Vencimento Próximos 30 Dias", "Produtos Vencidos", "Estoque Baixo")); // Adicione sua lógica de tipos de relatórios

        return "index";
    }

    @GetMapping("/relatorios/gerar-pdf-index")
    public ResponseEntity<byte[]> gerarPdfIndex(@RequestParam String tipo) {
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

    /******************
    * pagina html FINAC
    *******************/
    @GetMapping("/finac/relatorio")
    public String relatoriosFinac(Model model) {
        List<String> tiposRelatorio = List.of("Vencimento Próximos 30 Dias", "Produtos Vencidos", "Estoque Baixo");
        model.addAttribute("tiposRelatorio", tiposRelatorio);

        //dashboard
        int totalProdutos = produtoService.contarTotalProdutos();
        int produtosVencer = produtoService.contarProdutosAVencer();
        double valorTotal = produtoService.calcularValorTotal();

        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        String valorTotalFormatado = currencyFormatter.format(valorTotal);

        model.addAttribute("totalProdutos", totalProdutos);
        model.addAttribute("produtosVencer", produtosVencer);
        model.addAttribute("valorTotal", valorTotalFormatado);


        return "finac/relatorio";
    }

    @GetMapping("/finac/relatorio/gerarRelatorio")
    public String gerarRelatorioFinac(@RequestParam String tipo, Model model, RedirectAttributes redirectAttributes) {

        if (tipo == null || tipo.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage1", "Selecione um tipo válido.");
            return "redirect:/finac/relatorio"; // Redireciona para exibir o formulário
        }

        List<Map<String, Object>> produtos = new ArrayList<>();
        String relatorioTitulo = "";

        switch (tipo) {
            case "Vencimento Próximos 30 Dias":
                produtos = relatoriosService.formatarListaDeProdutos(produtoService.produtosComDataDeValidadeProxima(30));
                relatorioTitulo = "Relatório de Produtos com Vencimento nos Próximos 30 Dias";
                break;
            case "Produtos Vencidos":
                produtos = relatoriosService.formatarListaDeProdutos(produtoService.listarProdutosVencidos());
                relatorioTitulo = "Relatório de Produtos Vencidos";
                break;
            case "Estoque Baixo":
                produtos = relatoriosService.formatarListaDeProdutos(produtoService.listarProdutosEstoqueBaixo());
                relatorioTitulo = "Relatório de Produtos com Estoque Baixo";
                break;
            default:
                break;
        }

        //dashboard
        int totalProdutos = produtoService.contarTotalProdutos();
        int produtosVencer = produtoService.contarProdutosAVencer();
        double valorTotal = produtoService.calcularValorTotal();

        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        String valorTotalFormatado = currencyFormatter.format(valorTotal);

        model.addAttribute("totalProdutos", totalProdutos);
        model.addAttribute("produtosVencer", produtosVencer);
        model.addAttribute("valorTotal", valorTotalFormatado);

        // Adiciona o conteúdo para o relatório
        model.addAttribute("produtos", produtos);
        model.addAttribute("titulo", relatorioTitulo);

        model.addAttribute("tipoSelecionado", tipo);

        // Certifique-se de que a lista de tipos sempre é carregada ao gerar o relatório
        model.addAttribute("tiposRelatorio", List.of("Vencimento Próximos 30 Dias", "Produtos Vencidos", "Estoque Baixo"));

        return "finac/relatorio";
    }

    @GetMapping("/finac/relatorios/gerar-pdf")
    public ResponseEntity<byte[]> gerarPdfFinac(@RequestParam String tipo) {
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

    @PostMapping("/finac/relatorios/produtos/relatorio-vencimento-proximo")
    public String gerarRelatorioProdutosVencimentoProximo(@RequestParam(value = "dias", required = false)
                                                          Integer dias, Model model, RedirectAttributes redirectAttributes) {
        if (dias == null || dias <= 0) {
            redirectAttributes.addFlashAttribute("errorMessage", "Por favor, informe uma quantidade de dias válida.");

            List<String> tiposRelatorio = List.of("Vencimento Próximos 30 Dias", "Produtos Vencidos", "Estoque Baixo");
            model.addAttribute("tiposRelatorio", tiposRelatorio);

            return "redirect:/finac/relatorios/produtos/relatorio-vencimento-proximo"; // Redireciona para exibir o formulário
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

        //dashboard
        int totalProdutos = produtoService.contarTotalProdutos();
        int produtosVencer = produtoService.contarProdutosAVencer();
        double valorTotal = produtoService.calcularValorTotal();

        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        String valorTotalFormatado = currencyFormatter.format(valorTotal);

        model.addAttribute("totalProdutos", totalProdutos);
        model.addAttribute("produtosVencer", produtosVencer);
        model.addAttribute("valorTotal", valorTotalFormatado);

        model.addAttribute("produtosProximosDoVencimento", produtosComDataFormatada);

        model.addAttribute("dias", dias);

        List<String> tiposRelatorio = List.of("Vencimento Próximos 30 Dias", "Produtos Vencidos", "Estoque Baixo");
        model.addAttribute("tiposRelatorio", tiposRelatorio);

        return "/finac/relatorio";
    }
}
