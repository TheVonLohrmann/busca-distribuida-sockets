package serverB;

import utils.SearchUtil;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    private static volatile boolean keepRunning = true;

    public static void main(String[] args) {
        final int PORT = 8081;
        final String DATA_B_FILE = "src/serverB/data_B.json";

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Servidor B escutando na porta " + PORT);

            while (keepRunning) {
                try (Socket clientSocket = serverSocket.accept();
                     BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                     PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

                    handleClient(in, out, DATA_B_FILE);

                } catch (IOException e) {
                    if (keepRunning) {
                        System.err.println("Erro ao lidar com o cliente: " + e.getMessage());
                    }
                }
            }

        } catch (IOException e) {
            System.err.println("Erro ao iniciar o servidor B: " + e.getMessage());
        }
        System.out.println("Servidor B desligado.");
    }

    private static void handleClient(BufferedReader in, PrintWriter out, String DATA_B_FILE) throws IOException {
        String clientMessage;
        while ((clientMessage = in.readLine()) != null) {
            System.out.println("Mensagem recebida do servidor A: " + clientMessage);
            if ("sair".equalsIgnoreCase(clientMessage)) {
                System.out.println("Encerrando o servidor B...");
                out.println("Encerrando o servidor B.");
                keepRunning = false; // Sinalizando para desligar o servidor
                break;
            }
            String results = SearchUtil.searchInJson(DATA_B_FILE, clientMessage);
            System.out.println("Resultados enviados ao servidor A: " + results);
            out.println(results);
        }
    }
}