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

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Servidor B está escutando na porta " + PORT);

            try (Socket socket = new Socket(SERVER_A_HOST, SERVER_A_PORT)) {
                System.out.println("Conectado ao Servidor " + SERVER_A_HOST + " : " + SERVER_A_PORT);

            } catch (IOException e) {
                System.err.println("Erro na comunicação com o servidor: " + e.getMessage());
            }

        } catch (IOException e) {
            System.err.println("Erro no servidor: " + e.getMessage());
        }
    }
}
