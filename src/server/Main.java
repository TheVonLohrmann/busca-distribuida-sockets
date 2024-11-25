package server;

import utils.SearchUtil;
import java.io.*;
import java.net.*;

public class Main {
    private static volatile boolean keepRunning = true;

    public static void main(String[] args) {
        final int PORT = 8080;
        final String SERVER_B_HOST = "127.0.0.1";
        final int SERVER_B_PORT = 8081;
        final String DATA_A_FILE = "src/server/data_A.json";

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Servidor A está escutando na porta " + PORT);

            while (keepRunning) {
                try (Socket clientSocket = serverSocket.accept();
                     BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                     PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                     Socket socketB = new Socket(SERVER_B_HOST, SERVER_B_PORT);
                     BufferedReader inB = new BufferedReader(new InputStreamReader(socketB.getInputStream()));
                     PrintWriter outB = new PrintWriter(socketB.getOutputStream(), true)) {

                    System.out.println("Cliente conectado: " + clientSocket.getInetAddress());
                    handleClient(in, out, inB, outB, DATA_A_FILE, socketB);

                } catch (IOException e) {
                    if (keepRunning) {
                        System.err.println("Erro ao lidar com o cliente: " + e.getMessage());
                    }
                }
            }

        } catch (IOException e) {
            System.err.println("Erro no servidor A: " + e.getMessage());
        }
        System.out.println("Servidor A desligado.");
    }

    private static void handleClient(BufferedReader in, PrintWriter out,
                                     BufferedReader inB, PrintWriter outB,
                                     String dataAFile, Socket socketB) throws IOException {
        String clientMessage;
        while ((clientMessage = in.readLine()) != null) {
            System.out.println("Mensagem recebida do cliente: " + clientMessage);

            if ("sair".equalsIgnoreCase(clientMessage)) {
                System.out.println("Encerrando o servidor A...");
                outB.println("sair");
                keepRunning = false; // Sinalizando para desligar o servidor
                break;
            }

            // Utiliza SearchUtil para buscar nos dados do servidor A
            String localResults = SearchUtil.searchInJson(dataAFile, clientMessage);

            if (!localResults.equals("Nenhum resultado encontrado.")) {
                out.println(localResults);
            } else {
                outB.println(clientMessage);
                System.out.println("Mensagem enviada ao servidor B: " + clientMessage);

                // Lendo a resposta do servidor B e enviando cada linha imediatamente para o cliente
                boolean responseStarted = false;
                try {
                    socketB.setSoTimeout(5000); // 5 segundos de tempo limite
                    String line;
                    while ((line = inB.readLine()) != null) {
                        out.println(line);
                        responseStarted = true;
                        System.out.println("Resposta do servidor B: " + line);
                    }
                } catch (SocketTimeoutException e) {
                    System.err.println("Tempo limite ao aguardar a resposta do servidor B");
                } catch (IOException e) {
                    System.err.println("Erro na comunicação com o servidor B: " + e.getMessage());
                }

                if (!responseStarted) {
                    out.println("Nenhum resultado encontrado no servidor B.");
                }
            }
        }
    }
}