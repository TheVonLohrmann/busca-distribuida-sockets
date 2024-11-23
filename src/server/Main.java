package server;

import org.json.JSONObject;

import java.io.*;
import java.net.*;

public class Main {
    public static void main(String[] args) {
        final int PORT = 8080;
        final String SERVER_B_HOST = "127.0.0.1";
        final int SERVER_B_PORT = 8081;

        final String dataAFile = "src/server/data_A.json"; // Caminho para o arquivo JSON local

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Servidor A está escutando na porta " + PORT);

            // Conexão persistente com o servidor B
            Socket socketB = new Socket(SERVER_B_HOST, SERVER_B_PORT);
            BufferedReader inB = new BufferedReader(new InputStreamReader(socketB.getInputStream()));
            PrintWriter outB = new PrintWriter(socketB.getOutputStream(), true);

            while (true) {
                // Aguarda conexão de um cliente
                Socket clientSocket = serverSocket.accept();
                System.out.println("Cliente conectado: " + clientSocket.getInetAddress());

                try (
                        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
                ) {
                    String clientMessage;

                    while ((clientMessage = in.readLine()) != null) {
                        System.out.println("Mensagem recebida do cliente: " + clientMessage);

                        // Verifica se o comando é "sair"
                        if ("sair".equalsIgnoreCase(clientMessage)) {
                            System.out.println("Comando 'sair' recebido do cliente.");
                            outB.println("sair"); // Envia o comando para o servidor B
                            System.out.println("Encerrando o servidor A...");
                            serverSocket.close();
                            socketB.close();
                            return;
                        }

                        // Criação de uma variável final para a query
                        final String query = clientMessage;

                        // Realiza a busca local primeiro
                        String response = searchInJson(dataAFile, query);

                        if (response == null || response.equals("Nenhum resultado encontrado.")) {
                            // Se não encontrou no arquivo A, tenta buscar no servidor B
                            synchronized (outB) {
                                outB.println(query); // Envia consulta ao servidor B
                            }
                            response = inB.readLine(); // Recebe resposta do servidor B
                        }

                        // Envia a resposta ao cliente
                        if (response != null) {
                            System.out.println("Resposta enviada ao cliente: " + response);
                            out.println(response);
                        } else {
                            out.println("Nenhum resultado encontrado.");
                        }
                    }
                } catch (IOException e) {
                    System.err.println("Erro na comunicação com o cliente: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Erro no servidor A: " + e.getMessage());
        }
    }

    /**
     * Realiza a busca no arquivo JSON local.
     *
     * @param filePath Caminho do arquivo JSON.
     * @param query Substring a ser buscada.
     * @return Resultado da busca ou mensagem de erro.
     */
    private static String searchInJson(String filePath, String query) {
        String result = null;

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            // Lê o conteúdo completo do arquivo JSON
            StringBuilder jsonContent = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                jsonContent.append(line);
            }

            // Converte o conteúdo em um objeto JSON
            JSONObject jsonObject = new JSONObject(jsonContent.toString());
            JSONObject titles = jsonObject.getJSONObject("title");

            // Itera sobre as chaves (índices) e verifica se o título contém a query
            for (String key : titles.keySet()) {
                String title = titles.getString(key);

                // Ajusta a comparação para ser case-insensitive e verificar a chave ou o valor
                if (key.equals(query)) {
                    result = title;
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("Erro ao ler o arquivo JSON: " + e.getMessage());
            result = "Erro ao acessar o arquivo local.";
        } catch (Exception e) {
            System.err.println("Erro ao processar o JSON: " + e.getMessage());
            result = "Erro ao processar o arquivo JSON.";
        }

        return result != null ? result : "Nenhum resultado encontrado.";
    }
}
