package com.projeto.estoque_poc.service;

import com.projeto.estoque_poc.exceptions.ProdutoNaoEncontradoException;
import com.projeto.estoque_poc.model.Produto;
import com.projeto.estoque_poc.repository.ProdutoRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.xhtmlrenderer.pdf.ITextRenderer;


@Service
public class ProdutoService {

    @Autowired
    private ProdutoRepository produtoRepository;

    // salva planilha excel enviada
    public void salvarPlanilha(MultipartFile file) throws IOException {
        Workbook workbook = new XSSFWorkbook(file.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);
        Iterator<Row> rows = sheet.iterator();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

        while (rows.hasNext()) {
            Row currentRow = rows.next();
            if (currentRow.getRowNum() == 0) {
                continue; // Ignorar a linha de cabeçalho
            }

            Produto produto = new Produto();
            produto.setNome(currentRow.getCell(0).getStringCellValue());
            produto.setValor(currentRow.getCell(1).getNumericCellValue());
            produto.setQuantidade((int) currentRow.getCell(2).getNumericCellValue());

            // Tratamento da data de validade (coluna 3)
            Cell dataValidadeCell = currentRow.getCell(3);
            if (dataValidadeCell != null) {
                if (dataValidadeCell.getCellType() == CellType.NUMERIC) {
                    if (DateUtil.isCellDateFormatted(dataValidadeCell)) {
                        LocalDate dataValidade = dataValidadeCell.getDateCellValue()
                                .toInstant()
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate();
                        produto.setDataValidade(dataValidade);
                    }
                } else if (dataValidadeCell.getCellType() == CellType.STRING) {
                    try {
                        String dateStr = dataValidadeCell.getStringCellValue();
                        LocalDate dataValidade = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("dd/MM/yyyy")); // Formato esperado
                        produto.setDataValidade(dataValidade);
                    } catch (Exception e) {
                        System.out.println("Erro ao converter data: " + dataValidadeCell.getStringCellValue());
                    }
                }
            }


            produtoRepository.save(produto);
        }
        workbook.close();
    }

    // gera pdf
    public ByteArrayOutputStream gerarPdfProdutos() throws IOException {
        List<Produto> produtos = produtoRepository.findAll(); // Busca todos os produtos
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        // Geração de XHTML
        StringBuilder xhtml = new StringBuilder();
        xhtml.append("<html><body><h1>Lista de Produtos</h1><table border='1'>");
        xhtml.append("<tr><th>ID</th><th>Nome</th><th>Quantidade</th><th>Valor</th><th>Data de Validade</th></tr>");

        for (Produto produto : produtos) {
            xhtml.append("<tr>");
            xhtml.append("<td>").append(produto.getId()).append("</td>");
            xhtml.append("<td>").append(produto.getNome()).append("</td>");
            xhtml.append("<td>").append(produto.getQuantidade()).append("</td>");
            xhtml.append("<td>").append(produto.getValor()).append("</td>");
            xhtml.append("<td>").append(produto.getDataValidade() != null ? produto.getDataValidade().toString() : "N/A").append("</td>");
            xhtml.append("</tr>");
        }
        xhtml.append("</table></body></html>");

        // Geração do PDF
        ITextRenderer renderer = new ITextRenderer();
        renderer.setDocumentFromString(xhtml.toString());
        renderer.layout();
        renderer.createPDF(outputStream);

        return outputStream; // Retorna o ByteArrayOutputStream com o PDF gerado
    }

    // dados da dashbaord
    public int contarTotalProdutos() {
        Integer total = produtoRepository.sumQuantidadeTotal();
        return (total != null) ? total : 0;
    }

    public int contarProdutosAVencer() {
        LocalDate hoje = LocalDate.now();
        return produtoRepository.countByDataValidadeBefore(hoje.plusDays(30));
    }

    public int contarEstoqueBaixo() {
        int limiteEstoqueBaixo = 10;
        return produtoRepository.countByQuantidadeLessThan(limiteEstoqueBaixo);
    }

    public double calcularValorTotal() {
        Double total = produtoRepository.sumValorTotalProdutos();
        return (total != null) ? total : 0.0;
    }

    // crud
    public List<Produto> buscarTodos() {
        return produtoRepository.findAll();
    }

    public Produto buscarPorId(Long id) {
        return produtoRepository.findById(id)
                .orElseThrow(() -> new ProdutoNaoEncontradoException("Produto não encontrado. Id:" + id));
    }

    public Produto atualizarProduto(Produto produto) {
        Optional<Produto> produtoExistente = produtoRepository.findById(produto.getId());
        if (produtoExistente.isPresent()) {
            return produtoRepository.save(produto);
        } else {
            throw new IllegalArgumentException("Produto não encontrado para o ID: " + produto.getId());
        }
    }

    public void excluirProduto(Long id) {
        produtoRepository.deleteById(id);
    }

    public Produto salvarProduto(Produto produto) {
        return produtoRepository.save(produto);
    }

    // relatórios
    public List<Produto> produtosComDataDeValidadeProxima(int dias) {
        LocalDate dataAtual = LocalDate.now();
        LocalDate dataLimite = dataAtual.plusDays(dias);
        return produtoRepository.findProdutosComDataDeValidadeProxima(dataAtual, dataLimite);
    }

    public List<Produto> listarProdutosVencidos() {
        LocalDate dataAtual = LocalDate.now();
        return produtoRepository.findProdutosVencidos(dataAtual);
    }

    public List<Produto> listarProdutosEstoqueBaixo() {
        return produtoRepository.findProdutosEstoqueBaixo();
    }

    public List<Produto> buscarPorProdutosRecentes() {
        List<Produto> listaProdutosRecentes = produtoRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        return listaProdutosRecentes.stream().limit(3).collect(Collectors.toList());
    }

    public List<Produto> buscarPorNome(String nome) {
        return produtoRepository.findByNomeContainingIgnoreCase(nome);
    }

    public void adicionarQuantidade(Long id, int quantidade) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));
        produto.setQuantidade(produto.getQuantidade() + quantidade);
        produtoRepository.save(produto);
    }

    public void subtrairQuantidade(Long id, int quantidade) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));
        int novaQuantidade = produto.getQuantidade() - quantidade;
        if (novaQuantidade < 0) {
            throw new RuntimeException("Quantidade não pode ser negativa");
        }
        produto.setQuantidade(novaQuantidade);
        produtoRepository.save(produto);
    }

    public Optional<Produto> findById(Long id) {
        return produtoRepository.findById(id);
    }

}
