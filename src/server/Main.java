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

            while (true) {
                try (Socket socket = new Socket(SERVER_B_HOST, SERVER_B_PORT)) {
                    System.out.println("Conectado ao Servidor B!");

                    // Interações servidor-cliente:
                    Socket clientSocket = serverSocket.accept(); // Aceita conexões do cliente


                    System.out.println("Cliente conectado: " + clientSocket.getInetAddress());

                    // Streams para comunicação com cliente
                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

                    // Processa mensagens do cliente
                    String clientMessage;
                    while ((clientMessage = in.readLine()) != null) {

                        System.out.println("Mensagem recebida: " + clientMessage);

                        // Responde ao cliente
                        String response = "Servidor A processou: " + clientMessage;
                        out.println(response);
                    }

                    System.out.println("Conexão com o cliente encerrada.");
                    clientSocket.close();

                } catch (IOException e) {
                    System.err.println("Erro ao conectar ao servidor B: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Erro no servidor: " + e.getMessage());
        }
    }
}
