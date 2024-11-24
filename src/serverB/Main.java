package serverB;

import org.json.JSONObject;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    public static void main(String[] args) {
        final int PORT = 8081;
        final String DATA_FILE = "src/serverB/data_B.json";

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Servidor B escutando na porta " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Conexão estabelecida com o servidor A.");
                try (
                        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
                ) {
                    handleClient(in, out, DATA_FILE);
                } catch (IOException e) {
                    System.err.println("Erro na comunicação com o Servidor A: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Erro ao iniciar o servidor B: " + e.getMessage());
        }
    }

    /**
     * Metodo que lida com a comunicação do servidor B com o servidor A.
     */
    private static void handleClient(BufferedReader in, PrintWriter out, String DATA_FILE) throws IOException {
        String clientMessage;

        while ((clientMessage = in.readLine()) != null) {
            System.out.println("Mensagem recebida do Servidor A: " + clientMessage);

            if ("sair".equalsIgnoreCase(clientMessage)) {
                System.out.println("Comando 'sair' recebido. Encerrando o servidor B.");
                out.println("Comando 'sair' recebido. Encerrando.");
                out.flush();
                break; // Interrompe o loop e fecha a conexão
            }

            // Realiza a busca nos dados locais
            String results = searchInJson(DATA_FILE, clientMessage);
            System.out.println("Resultado da busca:\n" + results);

            // Cria uma resposta em formato JSON
            JSONObject response = new JSONObject();
            response.put("query", clientMessage);
            response.put("results", results);

            // Envia os resultados formatados para o Servidor A
            out.println(response.toString());
            out.flush();  // Garantir que a resposta seja enviada para o Servidor A
        }
    }

    /**
     * Realiza a busca no arquivo JSON local.
     */
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

    /**
     * Implementação do algoritmo KMP para busca de padrões.
     */
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

    /**
     * Calcula o array de prefixos para o KMP.
     */
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