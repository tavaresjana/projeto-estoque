package com.projeto.estoque_poc.service;

import com.projeto.estoque_poc.model.Produto;
import com.projeto.estoque_poc.repository.ProdutoRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Iterator;

@Service
public class ProdutoService {

    @Autowired
    private ProdutoRepository produtoRepository;

    public void salvarPlanilha(MultipartFile file) throws IOException {
        Workbook workbook = new XSSFWorkbook(file.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);
        Iterator<Row> rows = sheet.iterator();

        while (rows.hasNext()) {
            Row currentRow = rows.next();
            if (currentRow.getRowNum() == 0) {
                continue; // Ignorar a linha de cabeçalho
            }
            Produto produto = new Produto();
            produto.setNome(currentRow.getCell(0).getStringCellValue());
            produto.setPreco(currentRow.getCell(1).getNumericCellValue());
            produto.setQuantidade((int) currentRow.getCell(2).getNumericCellValue());

            produtoRepository.save(produto);
        }
        workbook.close();
    }
}
