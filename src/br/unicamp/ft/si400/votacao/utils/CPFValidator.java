package br.unicamp.ft.si400.votacao.utils;

/**
 * Classe utilitária para validação de CPF.
 * Requerida para o servidor e cliente.
 * Este é um validador básico (algoritmo Módulo 11).
 * Identificadores internos corrigidos para inglês.
 */
public class CPFValidator {

    /**
     * Valida um número de CPF.
     * @param cpf O CPF como String (pode conter pontos e traço).
     * @return true se o CPF for válido, false caso contrário.
     */
    public static boolean validate(String cpf) {
        if (cpf == null) return false;
        
        // CORRIGIDO: cpfLimpo -> cleanCPF
        String cleanCPF = cpf.replaceAll("[.\\-]", "");

        if (cleanCPF.length() != 11 || hasAllSameDigits(cleanCPF)) {
            return false;
        }

        try {
            // CORRIGIDO: d1 -> digit1, d2 -> digit2
            int digit1 = calculateDigit(cleanCPF.substring(0, 9), 10);
            int digit2 = calculateDigit(cleanCPF.substring(0, 9) + digit1, 11);

            return (cleanCPF.equals(cleanCPF.substring(0, 9) + digit1 + digit2));
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static boolean hasAllSameDigits(String s) {
        char first = s.charAt(0);
        for (int i = 1; i < s.length(); i++) {
            if (s.charAt(i) != first) return false;
        }
        return true;
    }

    // CORRIGIDO: peso -> weight
    private static int calculateDigit(String str, int weight) {
        // CORRIGIDO: soma -> sum
        int sum = 0;
        for (int i = str.length() - 1; i >= 0; i--) {
            // CORRIGIDO: digito -> digit
            int digit = Integer.parseInt(str.substring(i, i + 1));
            sum += digit * weight;
            weight--;
            if (weight < 2) weight = 9; // Para o segundo dígito
        }
        // CORRIGIDO: resto -> remainder
        int remainder = sum % 11;
        return (remainder < 2) ? 0 : (11 - remainder);
    }
}