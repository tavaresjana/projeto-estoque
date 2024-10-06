package com.projeto.estoque_poc.service;

import com.projeto.estoque_poc.model.Produto;
import com.projeto.estoque_poc.repository.ProdutoRepository;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.List;

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
        return produtoRepository.findAll(); // Retorna todos os produtos do banco
    }
    public int contarTotalProdutos() {
        return (int) produtoRepository.count();  // Total de produtos
    }

    public int contarProdutosAVencer() {
        return produtoRepository.contarProdutosAVencer();  // Buscar do banco os produtos perto de vencer
    }

    public int contarEstoqueBaixo() {
        return produtoRepository.contarEstoqueBaixo();  // Buscar do banco produtos com estoque baixo
    }

    public double calcularValorTotal() {
        return produtoRepository.calcularValorTotal();  // Calcular valor total dos produtos
    }

    public List<Produto> listarProdutosRecentes() {
        return produtoRepository.findTop5ByOrderByIdDesc();  // Buscar últimos 5 produtos adicionados
    }
}
