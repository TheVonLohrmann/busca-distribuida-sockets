package serverB;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    public static void main(String[] args) {
        final int PORT = 8081;

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Servidor B está escutando na porta " + PORT);

            while (true) {
                // Aguarda conexão do servidor A
                Socket clientSocket = serverSocket.accept();
                System.out.println("Conexão recebida de: " + clientSocket.getInetAddress());

                try (
                        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
                ) {
                    // Processa mensagens do servidor A
                    String clientMessage;
                    while ((clientMessage = in.readLine()) != null) {
                        System.out.println("Mensagem recebida do servidor A: " + clientMessage);

                        // Verifica se o comando é "sair"
                        if ("sair".equalsIgnoreCase(clientMessage)) {
                            System.out.println("Comando 'sair' recebido. Encerrando o servidor B...");
                            serverSocket.close();
                            return;
                        }

                        // Simula a busca e responde ao servidor A
                        String response = "'" + clientMessage + "' foi encontrado em Data_B";
                        out.println(response);
                    }
                } catch (IOException e) {
                    System.err.println("Erro na comunicação com o servidor A: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Erro no servidor B: " + e.getMessage());
        }
    }
}
