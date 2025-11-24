package br.unicamp.ft.si400.votacao.utils;

import java.net.URL;
import java.awt.Image;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.io.BufferedReader;
import java.io.InputStreamReader;


public final class Info {
    public static final String author = "Prof. Andre F. de Angelis"; 
    public static final String sysName = "Sistema de Votação Distribuída"; //
    public static final String copyRight = "Copyright \u00A9 2025. " + author + ". All rights reserved.";
    public static final String mission = "Projeto de Si400 para implementar um sistema de votação cliente-servidor."; //
    public static final String date = "November, 2025 (Ver 1.0: November, 2020)";
    public static final String version = "Ver. 1.0";

    private static final String ResFolder = "/resources/";
    private static final String HelpFile = "Document_Help.txt";
    private static final String DisclaimerFile = "Document_Disclaimer.txt";
    private static final String logoFile = "Unicamp_logo.jpg";
    private static Image logoImage = null;

    public static String getAboutText() {
        StringBuilder finalText = new StringBuilder();
        finalText.append("\n");
        finalText.append(sysName + "\n");
        finalText.append(version + " - ");
        finalText.append(date + "\n");
        finalText.append("\n");
        finalText.append(mission + "\n");
        finalText.append("\n");
        finalText.append("Baseado no código de: " + author + "\n");
        finalText.append("\n");
        finalText.append(copyRight + "\n");
        return (finalText.toString());
    }


    public static String getDisclaimerText() {
        return (getTextFromResourceFile(ResFolder + DisclaimerFile));
    }


    public static String getHelpText() {
        return (getTextFromResourceFile(ResFolder + HelpFile));
    }

    public static Image getLogoImage() {
        if (logoImage == null) {
            try {
                final URL auxURL = Info.class.getResource(ResFolder + Info.logoFile);
                if (auxURL == null) {
                     throw new IOException("Recurso não encontrado: " + ResFolder + Info.logoFile);
                }
                logoImage = ImageIO.read(auxURL);
            } catch (final IOException e) {
                System.out.println(Info.getLongVersion() + "\nLogo não encontrada. " + e.getMessage());
            } catch (Exception e) {
                System.out.println(Info.getLongVersion() + "\nErro ao carregar a logo. " + e.getMessage());
            }
        }
        return (logoImage);
    }

    public static String getLongVersion() {
        return (sysName + " - " + version + " - " + date);
    }

    public static String getShortVersion() {
        return (version + " - " + date);
    }

    private static String getTextFromResourceFile(String fileName) {
        StringBuilder finalText = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(Info.class.getResourceAsStream(fileName)))) {
            String buffer;
            while ((buffer = reader.readLine()) != null) {
                finalText.append(buffer + "\n");
            }
        } catch (NullPointerException e) {
            System.err.println(Info.getLongVersion() + "\nErro ao carregar arquivo (NullPointerException): " + fileName + "\n" + e.getMessage());
            return "Arquivo '" + fileName + "' não encontrado no classpath.\nPor favor, crie o arquivo.";
        } catch (IOException e) {
            System.err.println(Info.getLongVersion() + "\nErro de I/O ao carregar arquivo: " + fileName + "\n" + e.getMessage());
        }
        return (finalText.toString());
    }
}
