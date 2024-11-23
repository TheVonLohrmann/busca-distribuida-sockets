package server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    public static void main(String[] args) {
        final int PORT = 8080;
        final String SERVER_B_HOST = "127.0.0.1";
        final int SERVER_B_PORT = 8081;

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
                    // Processa mensagens do cliente
                    String clientMessage;
                    while ((clientMessage = in.readLine()) != null) {
                        System.out.println("Mensagem recebida do cliente: " + clientMessage);

                        // Verifica se o comando é "sair"
                        if ("sair".equalsIgnoreCase(clientMessage)) {
                            System.out.println("Comando 'sair' recebido do cliente.");

                            // Envia o comando "sair" para o servidor B
                            outB.println(clientMessage);

                            // Encerra o servidor A
                            System.out.println("Encerrando o servidor A...");
                            serverSocket.close();
                            socketB.close();
                            return;
                        }

                        // Envia a mensagem para o servidor B
                        outB.println(clientMessage);

                        // Recebe a resposta do servidor B
                        String serverBResponse = inB.readLine();
                        System.out.println("Resposta do servidor B: " + serverBResponse);

                        // Envia a resposta de volta ao cliente
                        out.println(serverBResponse);
                    }
                } catch (IOException e) {
                    System.err.println("Erro na comunicação com o cliente: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Erro no servidor A: " + e.getMessage());
        }
    }
}
