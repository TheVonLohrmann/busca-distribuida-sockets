package client;

import java.io.*;
import java.net.Socket;

public class Client {
    public static void main(String[] args) {
        final String SERVER_A_HOST = "127.0.0.1"; // IP do Servidor A
        final int SERVER_A_PORT = 8080;          // Porta do Servidor A

        try (Socket socket = new Socket(SERVER_A_HOST, SERVER_A_PORT)) {
            System.out.println("Conectado ao Servidor A!");

            // Streams para comunicação
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true); // Enviar dados para o servidor
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream())); // Receber dados do servidor
            BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in)); // Entrada do usuário

            // Thread para escutar mensagens do servidor
            Thread serverListener = new Thread(() -> {
                try {
                    String serverResponse;
                    while ((serverResponse = in.readLine()) != null) {
                        System.out.println("Servidor: " + serverResponse);
                    }
                } catch (IOException e) {
                    System.err.println("Conexão com o servidor encerrada.");
                }
            });
            serverListener.start();

            // Loop para enviar mensagens
            while (true) {
                System.out.print("Digite a substring para busca (ou 'sair' para encerrar): ");
                String substring = userInput.readLine();

                if ("sair".equalsIgnoreCase(substring)) {
                    System.out.println("Encerrando conexão...");
                    break;
                }

                if (substring == null || substring.trim().isEmpty()) {
                    System.out.println("Erro: Substring não pode ser vazia.");
                    continue;
                }

                out.println(substring); // Enviar para o servidor
            }

            // Encerrar conexão
            socket.close();
            System.out.println("Conexão encerrada.");
        } catch (IOException e) {
            System.err.println("Erro na comunicação com o servidor: " + e.getMessage());
        }
    }
}
