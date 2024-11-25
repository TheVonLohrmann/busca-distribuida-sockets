package server;

import org.json.JSONObject;

import java.io.*;
import java.net.*;

public class Main {
    public static void main(String[] args) {
        final int PORT = 8080;
        final String SERVER_B_HOST = "127.0.0.1";
        final int SERVER_B_PORT = 8081;

        final String dataAFile = "src/server/data_A.json";  // Arquivo JSON para o Servidor A

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Servidor A está escutando na porta " + PORT);

            try (Socket socketB = new Socket(SERVER_B_HOST, SERVER_B_PORT);
                 BufferedReader inB = new BufferedReader(new InputStreamReader(socketB.getInputStream()));
                 PrintWriter outB = new PrintWriter(socketB.getOutputStream(), true)) {

                while (true) {
                    try (Socket clientSocket = serverSocket.accept();
                         BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                         PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

                        System.out.println("Cliente conectado: " + clientSocket.getInetAddress());
                        handleClient(in, out, inB, outB, dataAFile);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Erro no servidor A: " + e.getMessage());
        }
    }

    private static void handleClient(BufferedReader in, PrintWriter out, BufferedReader inB, PrintWriter outB, String dataAFile) throws IOException {
        String clientMessage;
        while ((clientMessage = in.readLine()) != null) {
            System.out.println("Mensagem recebida do cliente: " + clientMessage);

            if ("sair".equalsIgnoreCase(clientMessage)) {
                System.out.println("Encerrando o servidor A...");
                outB.println("sair");
                return;
            }

            // Buscando no JSON local do Servidor A
            String localResults = searchInJson(dataAFile, clientMessage);

            if (!localResults.equals("Nenhum resultado encontrado.")) {
                // Se encontrou localmente, envia ao cliente
                System.out.println("Resposta ao cliente (Servidor A):\n" + localResults);
                out.println(localResults);
            } else {
                // Caso não tenha encontrado localmente, consulta o Servidor B
                System.out.println("Enviando mensagem para o Servidor B: " + clientMessage);
                outB.println(clientMessage);

                // Garantir que a resposta do Servidor B seja lida completamente
                StringBuilder serverBResults = new StringBuilder();
                String line;
                boolean isEmptyResponse = true;
                while ((line = inB.readLine()) != null) {
                    isEmptyResponse = false;
                    serverBResults.append(line).append("\n");
                }

                if (isEmptyResponse) {
                    System.out.println("Nenhuma resposta recebida do Servidor B.");
                } else {
                    System.out.println("Resposta recebida do Servidor B:\n" + serverBResults.toString());
                    // Enviar a resposta completa para o cliente
                    out.println(serverBResults.toString());
                }
            }
        }
    }



    private static String searchInJson(String filePath, String query) {
        StringBuilder results = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            StringBuilder jsonContent = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                jsonContent.append(line);
            }

            if (jsonContent.length() == 0) {
                return "Erro: O arquivo JSON está vazio.";
            }

            // Parse o JSON como um objeto
            JSONObject data = new JSONObject(jsonContent.toString());
            JSONObject titles = data.getJSONObject("title");
            JSONObject abstracts = data.getJSONObject("abstract");
            JSONObject labels = data.getJSONObject("label");

            // Itere sobre as chaves do JSON
            for (String key : titles.keySet()) {
                String title = titles.optString(key, "");
                String abstractText = abstracts.optString(key, "");
                String label = labels.optString(key, "");

                // Verifica se a query está no título ou resumo
                if (kmpSearch(title.toLowerCase(), query.toLowerCase()) ||
                        kmpSearch(abstractText.toLowerCase(), query.toLowerCase())) {
                    results.append("Título: ").append(title).append("\n")
                            .append("Resumo: ").append(abstractText).append("\n")
                            .append("Categoria: ").append(label).append("\n\n");
                }
            }
        } catch (Exception e) {
            return "Erro ao processar o JSON: " + e.getMessage();
        }

        return results.length() > 0 ? results.toString() : "Nenhum resultado encontrado.";
    }

    private static boolean kmpSearch(String text, String pattern) {
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
                return true; // Padrão encontrado
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