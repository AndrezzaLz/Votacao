package br.unicamp.ft.si400.votacao.utils;


public class CPFValidator {


    public static boolean validate(String cpf) {
        if (cpf == null) return false;
        
        String cleanCPF = cpf.replaceAll("[.\\-]", "");

        if (cleanCPF.length() != 11 || hasAllSameDigits(cleanCPF)) {
            return false;
        }

        try {
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

    private static int calculateDigit(String str, int weight) {
        int sum = 0;
        for (int i = 0; i < str.length(); i++) {
            int digit = Integer.parseInt(str.substring(i, i + 1));
            sum += digit * weight;
            weight--;
        }
        int remainder = sum % 11;
        return (remainder < 2) ? 0 : (11 - remainder);
    }
}
