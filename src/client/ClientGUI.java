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

    private Button startServersButton; // Declarando os botões como variáveis de classe
    private Button stopServersButton;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        final String SERVER_A_HOST = "127.0.0.1";
        final int SERVER_A_PORT = 8080;

        VBox root = new VBox(10);
        root.setPadding(new Insets(15));

        // Inicializando os botões
        startServersButton = new Button("Ligar Servidores");
        stopServersButton = new Button("Desligar Servidores");
        Button connectButton = new Button("Conectar ao Servidor");
        Button sendButton = new Button("Enviar");

        TextField inputField = new TextField();
        inputField.setPromptText("Digite a substring para busca...");
        TextArea outputArea = new TextArea();
        outputArea.setEditable(false);

        // Configuração do botão: Ligar Servidores
        startServersButton.setOnAction(e -> {
            try {
                if (serverBProcess == null || !serverBProcess.isAlive()) {
                    serverBProcess = new ProcessBuilder("java", "-cp", "bin", "serverB.Main").start();
                    outputArea.appendText("Servidor B iniciado.\n");
                }

                if (serverAProcess == null || !serverAProcess.isAlive()) {
                    serverAProcess = new ProcessBuilder("java", "-cp", "bin", "server.Main").start();
                    outputArea.appendText("Servidor A iniciado.\n");
                }
            } catch (IOException ex) {
                outputArea.appendText("Erro ao iniciar os servidores: " + ex.getMessage() + "\n");
            }
        });

        // Configuração do botão: Conectar ao Servidor
        connectButton.setOnAction(e -> {
            try {
                Socket socket = new Socket(SERVER_A_HOST, SERVER_A_PORT);
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                // Listener para respostas do servidor
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
                outputArea.appendText("Conectado ao servidor A.\n");
            } catch (IOException ex) {
                outputArea.appendText("Erro ao conectar ao servidor: " + ex.getMessage() + "\n");
            }
        });

        // Configuração do botão: Enviar Substring
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

        // Configuração do botão: Desligar Servidores
        stopServersButton.setOnAction(e -> {
            try {
                if (serverAProcess != null) {
                    serverAProcess.destroy();
                    outputArea.appendText("Servidor A desligado.\n");
                }
                if (serverBProcess != null) {
                    serverBProcess.destroy();
                    outputArea.appendText("Servidor B desligado.\n");
                }
                System.exit(0); // Finaliza a aplicação
            } catch (Exception ex) {
                outputArea.appendText("Erro ao desligar os servidores: " + ex.getMessage() + "\n");
            }
        });

        // Adiciona os elementos na interface
        root.getChildren().addAll(
                startServersButton, connectButton, inputField, sendButton, stopServersButton, outputArea
        );

        // Configuração da janela
        stage.setTitle("Cliente de Busca");
        stage.setScene(new Scene(root, 400, 400));
        stage.show();
    }
}

