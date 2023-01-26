package org.example.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;

public class ChatClient {
    private final Logger logger = LoggerFactory.getLogger(ChatClient.class);
    private final Logger messagesLogger = LoggerFactory.getLogger("chatLog");
    private String host;
    private int port;
    private String username;
    private Socket socket;
    private BufferedReader in;
    private BufferedWriter out;
    private String reason;

    public ChatClient(String host, int port, String username) {
        this.host = host;
        this.port = port;
        this.username = username;
    }

    public String getReason() {
        return reason;
    }

    public boolean connect() {
        logger.info("[{}] соединение с сервером {}:{}", username, host, port);
        try {
            socket = new Socket(host, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            String message;

            message = in.readLine();
            if (message.equals("For start send \"/start <username>")) {
                out.write("/start " + username + "\n");
                out.flush();
            }
            message = in.readLine();
            if (message.equals("/ok")) {
                MessageReader messageReader = new MessageReader();
                messageReader.start();
            } else {
                reason = message;
                return false;
            }
        } catch (IOException e) {
            logger.error("Не удалось подключиться к серверу", e);
        }
        return socket.isConnected();
    }


    public void close() {
        try {
            socket.close();
        } catch (IOException e) {
            logger.error("Ошибка работы с сокетом", e);
        }
    }

    private void send(String message) {
        try {

            out.write(message + "\n");
            out.flush();
        } catch (IOException e) {
            logger.error("Ошибка отправки сообщения", e);
        }
    }

    public void sendMessage(String message) {
        messagesLogger.info("[{}] {}", username, message);
        try {
            out.write("/message " + message + "\n");
            out.flush();
        } catch (IOException e) {
            logger.error("Ошибка отправки сообщения", e);
        }
    }

    private class MessageReader extends Thread {
        @Override
        public void run() {
            String message = "";
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    message = in.readLine();
                    if (message == null) {
                        System.out.println("Сервер разорвал соединение");
                        logger.info("Сервер разорвал соединение");
                        break;
                    }
                    messagesLogger.info(message);
                }

            } catch (IOException e) {
                logger.info("Закрытие сокета");
            }
        }
    }
}
