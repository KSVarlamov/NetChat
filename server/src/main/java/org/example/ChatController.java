package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class ChatController {

    private final Logger logger = LoggerFactory.getLogger(ChatController.class);
    private final Logger messagesLogger = LoggerFactory.getLogger("chatlog");
    private final ConcurrentHashMap<String, ClientHandler> clients = new ConcurrentHashMap<>();
    private final IncomeHandler incomeHandler;
    private int messagesCount = 0;

    public ChatController(int port) {
        this.incomeHandler = new IncomeHandler(port, this);
    }

    public void addClient(Socket client) {

        logger.info("Подключение клиента: {}:{}", client.getInetAddress(), client.getPort());
        new ClientHandler(client, this);
    }

    public void stopChat() {
        logger.info("Получена команда на остановку чата");
        logger.info("Рассылка клиентам уведомлений о закрытии");
        for (ClientHandler c : clients.values()) {
            c.close();
        }
        incomeHandler.interrupt();
        logger.info("Остановка сервера");

    }

    public void startChat() {
        incomeHandler.start();
    }

    public boolean registerUser(String username, ClientHandler clientHandler) {
        if (clients.containsKey(username)) return false;
        clients.put(username, clientHandler);
        return true;
    }

    public boolean canRegister(String username) {
        return !clients.containsKey(username);
    }

    public void sendSystemMessage(String message) {
        message = "Системное сообщение: " + message;
        messagesLogger.info(message);
        for (ClientHandler c : clients.values()) {
            c.send(message);
        }
    }

    public synchronized void sendAll(String message, ClientHandler sender) {
        message = String.format("[%s] %s", sender.getUserName(), message);
        messagesLogger.info(message);
        for (ClientHandler c : clients.values()) {
            c.send(message);
        }
        messagesCount++;
    }

    public int getClientsCount() {
        return clients.size();
    }

    public int getMessagesCount() {
        return messagesCount;
    }

    public void connectionLost(String clientName) {
        if (clientName != null) {
            clients.remove(clientName);
        }
    }
}
