package serverB;

import org.json.JSONObject;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    public static void main(String[] args) {
        final int PORT = 8081;
        final String DATA_FILE = "src/serverB/data_B.json";

        JSONObject dataObject = loadJsonData(DATA_FILE);
        if (dataObject == null) {
            System.err.println("Erro ao carregar o arquivo JSON.");
            return;
        }

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Servidor B escutando na porta " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();

                try (
                        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
                ) {
                    String clientMessage;
                    while ((clientMessage = in.readLine()) != null) {
                        if ("sair".equalsIgnoreCase(clientMessage)) {
                            System.out.println("Encerrando servidor B.");
                            return;
                        }
                        String response = searchInJson(dataObject, clientMessage);
                        out.println(response != null ? response : "Nenhum resultado encontrado.");
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Erro no servidor B: " + e.getMessage());
        }
    }

    private static JSONObject loadJsonData(String filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            StringBuilder jsonBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }
            return new JSONObject(jsonBuilder.toString());
        } catch (IOException e) {
            System.err.println("Erro ao carregar JSON: " + e.getMessage());
            return null;
        }
    }

    private static String searchInJson(JSONObject dataObject, String query) {
        try {
            JSONObject titleObject = dataObject.getJSONObject("title");
            if (titleObject.has(query)) {
                return "Servidor B encontrou: " + titleObject.getString(query);
            }
        } catch (Exception e) {
            return "Erro ao buscar JSON: " + e.getMessage();
        }
        return  null;
    }
}