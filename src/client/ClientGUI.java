package client;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;

public class ClientGUI extends Application {
    private PrintWriter out;
    private BufferedReader in;
    private Process serverAProcess;
    private Process serverBProcess;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        final String SERVER_A_HOST = "127.0.0.1";
        final int SERVER_A_PORT = 8080;

        VBox root = new VBox(10);
        root.setPadding(new Insets(15));

        // elementos da interface
        TextField inputField = new TextField();
        inputField.setPromptText("Digite a substring para busca...");
        Button sendButton = new Button("Enviar");
        Button startServerButton = new Button("Ligar servidores");
        Button connectButton = new Button("Conectar");
        Button stopServerButton = new Button("Desligar servidores");
        TextArea outputArea = new TextArea();
        outputArea.setEditable(false);

        // Botão para ligar os servidores
        startServerButton.setOnAction(event -> {
            try{
                if (serverBProcess == null || serverBProcess.isAlive()) {
                    serverAProcess = new ProcessBuilder("java", "-cp", "bin", "server.Main").start();
                    outputArea.appendText("Servidor A iniciado.\n");
                }

                if (serverAProcess == null || serverAProcess.isAlive()) {
                    serverAProcess = new ProcessBuilder("java", "-cp", "bin", "server.Main").start();
                    outputArea.appendText("Servidor A iniciado.\n");
                }
            } catch (IOException ex) {
                outputArea.appendText("Erro ao iniciar os servidores: " + ex.getMessage() + "\n");
            }
        });

        // botão para fazer a conexão com os servidores
        connectButton.setOnAction(event -> {
            try {
                Socket socket = new Socket(SERVER_A_HOST, SERVER_A_PORT);
                out = new PrintWriter(socket.getOutputStream(), true);
               in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

               // Listener para resposta do servidor
                Thread serverListener = new Thread(() -> {
                    try {
                        String response;
                        while ((response = in.readLine()) != null) {
                            outputArea.appendText("Servidor: " + response + "\n");
                        }
                    } catch (IOException ex) {
                        outputArea.appendText("Conexão com o servidor encerrada.\n");
                    }
                });
                serverListener.start();
                outputArea.appendText("Servidor conectado.\n");
            } catch (IOException ex) {
                outputArea.appendText("Erro ao conectar ao servidor: " + ex.getMessage() + "\n");
            }
        });

        //
        sendButton.setOnAction(e -> {
            String substring = inputField.getText();
            if (substring.isEmpty()) {
                outputArea.appendText("A substring não pode estar vazia.\n");
                return;
            }
            out.println(substring);
            outputArea.appendText("Enviado: " + substring + "\n");
            inputField.clear();
        });

        root.getChildren().addAll(inputField, sendButton, outputArea);

        try {
            Socket socket = new Socket(SERVER_A_HOST, SERVER_A_PORT);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            Thread serverListener = new Thread(() -> {
                try {
                    String response;
                    while ((response = in.readLine()) != null) {
                        outputArea.appendText("Servidor: " + response + "\n");
                    }
                } catch (IOException e) {
                    outputArea.appendText("Conexão com o servidor encerrada.\n");
                }
            });
            serverListener.start();
        } catch (IOException e) {
            outputArea.appendText("Erro ao conectar ao servidor: " + e.getMessage() + "\n");
        }

        stage.setTitle("Cliente de Busca");
        stage.setScene(new Scene(root, 400, 300));
        stage.show();
    }
}

