package com.projeto.estoque_poc.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DataUtil {

    public static LocalDate converterParaLocalDate(String data) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        try {
            return LocalDate.parse(data, formatter);
        } catch (DateTimeParseException e) {
            System.err.println("Formato de data inválido: " + data);
            return null; // ou lançar uma exceção personalizada, conforme necessário
        }
    }

    public static String formatarData(LocalDate data) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return data.format(formatter);
    }
}
