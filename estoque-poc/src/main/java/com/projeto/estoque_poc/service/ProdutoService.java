package com.projeto.estoque_poc.service;

import com.projeto.estoque_poc.exceptions.ProdutoNaoEncontradoException;
import com.projeto.estoque_poc.model.Produto;
import com.projeto.estoque_poc.repository.ProdutoRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
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

import org.xhtmlrenderer.pdf.ITextRenderer;


@Service
public class ProdutoService {

    @Autowired
    private ProdutoRepository produtoRepository;

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

    public List<Produto> buscarTodos() {
        return produtoRepository.findAll();
    }

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

    public int contarTotalProdutos() {
        Integer total = produtoRepository.sumQuantidadeTotal();
        return (total != null) ? total : 0;
    }

    public int contarProdutosAVencer() {
        LocalDate hoje = LocalDate.now();
        return produtoRepository.countByDataValidadeBefore(hoje.plusDays(30)); // Exemplo: Produtos a vencer nos próximos 30 dias
    }

    public int contarEstoqueBaixo() {
        int limiteEstoqueBaixo = 10;
        return produtoRepository.countByQuantidadeLessThan(limiteEstoqueBaixo);
    }

    public double calcularValorTotal() {
        Double total = produtoRepository.sumValorTotalProdutos();
        return (total != null) ? total : 0.0;
    }

    // Buscar produto por ID
    public Produto buscarPorId(Long id) {
        return produtoRepository.findById(id)
                .orElseThrow(() -> new ProdutoNaoEncontradoException("Produto não encontrado."));
    }

    // Atualizar produto
    public Produto atualizarProduto(Produto produto) {
        return produtoRepository.save(produto);
    }

    // Excluir produto
    public void excluirProduto(Long id) {
        produtoRepository.deleteById(id);
    }

    // Listar todos os produtos
    public List<Produto> listarTodos() {
        return produtoRepository.findAll();
    }
}
