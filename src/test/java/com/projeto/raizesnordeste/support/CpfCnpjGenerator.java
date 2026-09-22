package com.projeto.raizesnordeste.support;

import java.security.SecureRandom;

/**
 * Gera CPF/CNPJ formatados com dígitos verificadores válidos (algoritmo mod 11),
 * para satisfazer as validações {@code @CPF}/{@code @CNPJ} do Bean Validation
 * sem depender de números fixos que colidiriam entre testes.
 */
public final class CpfCnpjGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();

    private CpfCnpjGenerator() {
    }

    public static String randomCpf() {
        int[] n = new int[9];
        for (int i = 0; i < 9; i++) {
            n[i] = RANDOM.nextInt(10);
        }
        int d1 = calcularDigito(n, 9, 10);
        int[] withD1 = appendDigit(n, d1);
        int d2 = calcularDigito(withD1, 10, 11);

        String cpf = digitsToString(n) + d1 + d2;
        return cpf.substring(0, 3) + "." + cpf.substring(3, 6) + "." + cpf.substring(6, 9) + "-" + cpf.substring(9, 11);
    }

    public static String randomCnpj() {
        int[] n = new int[12];
        for (int i = 0; i < 8; i++) {
            n[i] = RANDOM.nextInt(10);
        }
        n[8] = 0;
        n[9] = 0;
        n[10] = 0;
        n[11] = 1;

        int[] pesos1 = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int d1 = calcularDigitoCnpj(n, pesos1);
        int[] withD1 = appendDigit(n, d1);
        int[] pesos2 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int d2 = calcularDigitoCnpj(withD1, pesos2);

        String cnpj = digitsToString(n) + d1 + d2;
        return cnpj.substring(0, 2) + "." + cnpj.substring(2, 5) + "." + cnpj.substring(5, 8) + "/"
                + cnpj.substring(8, 12) + "-" + cnpj.substring(12, 14);
    }

    private static int calcularDigito(int[] digitos, int tamanho, int pesoInicial) {
        int soma = 0;
        int peso = pesoInicial;
        for (int i = 0; i < tamanho; i++) {
            soma += digitos[i] * peso--;
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }

    private static int calcularDigitoCnpj(int[] digitos, int[] pesos) {
        int soma = 0;
        for (int i = 0; i < pesos.length; i++) {
            soma += digitos[i] * pesos[i];
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }

    private static int[] appendDigit(int[] original, int digit) {
        int[] result = new int[original.length + 1];
        System.arraycopy(original, 0, result, 0, original.length);
        result[original.length] = digit;
        return result;
    }

    private static String digitsToString(int[] digits) {
        StringBuilder sb = new StringBuilder();
        for (int digit : digits) {
            sb.append(digit);
        }
        return sb.toString();
    }
}
