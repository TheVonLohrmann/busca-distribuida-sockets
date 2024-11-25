package utils;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileReader;

public class SearchUtil {
    public static String searchInJson(String filePath, String query) {
        StringBuilder results = new StringBuilder();
        System.out.println("Buscando no arquivo: " + filePath + " por: " + query);
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            StringBuilder jsonContent = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                jsonContent.append(line);
            }

            if (jsonContent.length() == 0) {
                return "Erro: O arquivo JSON está vazio.";
            }

            JSONObject data = new JSONObject(jsonContent.toString());
            JSONObject titles = data.getJSONObject("title");
            JSONObject abstracts = data.getJSONObject("abstract");
            JSONObject labels = data.getJSONObject("label");

            for (String key : titles.keySet()) {
                String title = titles.optString(key, "");
                String abstractText = abstracts.optString(key, "");
                String label = labels.optString(key, "");

                if (kmpSearch(title.toLowerCase(), query.toLowerCase()) ||
                        kmpSearch(abstractText.toLowerCase(), query.toLowerCase())) {
                    results.append("Título: ").append(title).append("\n")
                            .append("Resumo: ").append(abstractText).append("\n")
                            .append("Categoria: ").append(label).append("\n\n");
                }
            }
        } catch (Exception e) {
            e.printStackTrace(); // Log da exceção
            return "Erro ao processar o JSON: " + e.getMessage();
        }

        System.out.println("Resultados encontrados: " + results.toString());
        return results.length() > 0 ? results.toString() : "Nenhum resultado encontrado.";
    }


    public static boolean kmpSearch(String text, String pattern) {
        if (pattern.isEmpty()) return true;
        if (text.isEmpty()) return false;

        int[] lps = computeLPSArray(pattern);
        int i = 0, j = 0;

        while (i < text.length()) {
            if (pattern.charAt(j) == text.charAt(i)) {
                i++;
                j++;
            }
            if (j == pattern.length()) {
                return true;
            } else if (i < text.length() && pattern.charAt(j) != text.charAt(i)) {
                if (j != 0) {
                    j = lps[j - 1];
                } else {
                    i++;
                }
            }
        }
        return false;
    }

    private static int[] computeLPSArray(String pattern) {
        int[] lps = new int[pattern.length()];
        int len = 0;
        int i = 1;

        while (i < pattern.length()) {
            if (pattern.charAt(i) == pattern.charAt(len)) {
                len++;
                lps[i] = len;
                i++;
            } else {
                if (len != 0) {
                    len = lps[len - 1];
                } else {
                    lps[i] = len;
                    i++;
                }
            }
        }
        return lps;
    }
}