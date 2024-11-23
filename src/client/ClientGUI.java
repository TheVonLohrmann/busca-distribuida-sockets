package client;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;

public class ClientGUI extends Application {
    private PrintWriter out;
    private BufferedReader in;
    private Socket clientSocket;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        final String SERVER_A_HOST = "127.0.0.1";
        final int SERVER_A_PORT = 8080;

        VBox root = new VBox(10);
        root.setPadding(new Insets(15));

        TextField inputField = new TextField();
        inputField.setPromptText("Digite a substring para busca...");
        TextArea outputArea = new TextArea();
        outputArea.setEditable(false);

        Button sendButton = new Button("Enviar");
        Button exitButton = new Button("Sair");

        try {
            clientSocket = new Socket(SERVER_A_HOST, SERVER_A_PORT);
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            Thread serverListener = new Thread(() -> {
                try {
                    String response;
                    while ((response = in.readLine()) != null) {
                        outputArea.appendText("Servidor: " + response + "\n");
                    }
                } catch (IOException ex) {
                    outputArea.appendText("Conexão com o servidor encerrada ou erro.\n");
                }
            });
            serverListener.setDaemon(true);
            serverListener.start();

            outputArea.appendText("Conectado ao servidor A.\n");
        } catch (IOException ex) {
            outputArea.appendText("Erro ao conectar ao servidor: " + ex.getMessage() + "\n");
        }

        sendButton.setOnAction(e -> enviarMensagem(inputField, outputArea));
        exitButton.setOnAction(e -> enviarComandoSair(outputArea));
        inputField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                enviarMensagem(inputField, outputArea);
            }
        });

        root.getChildren().addAll(inputField, sendButton, exitButton, outputArea);
        stage.setTitle("Cliente de Busca");
        stage.setScene(new Scene(root, 400, 400));
        stage.setOnCloseRequest(e -> finalizarAplicacao());
        stage.show();
    }

    private void enviarMensagem(TextField inputField, TextArea outputArea) {
        String mensagem = inputField.getText();
        if (mensagem.isEmpty()) {
            return;
        }
        try {
            out.println(mensagem);
            outputArea.appendText("Enviado: " + mensagem + "\n");
            inputField.clear();
        } catch (Exception ex) {
            outputArea.appendText("Erro ao enviar mensagem: " + ex.getMessage() + "\n");
        }
    }

    private void enviarComandoSair(TextArea outputArea) {
        try {
            out.println("sair");
            outputArea.appendText("Enviado comando: sair\n");
            finalizarAplicacao();
        } catch (Exception ex) {
            outputArea.appendText("Erro ao enviar comando sair: " + ex.getMessage() + "\n");
        }
    }

    private void finalizarAplicacao() {
        try {
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
            }
            System.exit(0);
        } catch (IOException ex) {
            System.err.println("Erro ao finalizar conexão: " + ex.getMessage());
        }
    }
}