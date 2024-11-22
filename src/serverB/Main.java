package serverB;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.chrono.IsoEra;

public class Main {
    public static void main(String[] args) {
        final int PORT = 8081;
        final String SERVER_A_HOST = "127.0.0.1"; // IP do Servidor A
        final int SERVER_A_PORT = 8080;           // Porta do Servidor A

        try (ServerSocket serverSocket = new ServerSocket(PORT)) { // Inicializa servidor na porta 8081
            System.out.println("Servidor B está escutando na porta " + PORT);

            while (true){

                // Interações servidor-cliente:
                Socket clientSocket = serverSocket.accept(); // Aceita conexões do servidor A
                System.out.println("Servidor conectado: " + clientSocket.getInetAddress() + " : " + clientSocket.getPort());

                // Streams para comunicação com servidor cliente
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

                // Processa mensagens do servidor cliente
                String clientMessage;
                while ((clientMessage = in.readLine()) != null) {
                    System.out.println("Mensagem recebida do servidor A: " + clientMessage);

                    // Responde ao servidor A
                    String response = "'"+ clientMessage +"'" + " foi encontrado em Data_B " ;
                    out.println(response);
                }
            }



        } catch (IOException e) {
            System.err.println("Erro no servidor: " + e.getMessage());
        }
    }
}
