package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class ChatController {

    private final Logger logger = LoggerFactory.getLogger(ChatController.class);
    private final Logger messagesLogger = LoggerFactory.getLogger("chatlog");
    private final ConcurrentHashMap<String, ChatClient> clients = new ConcurrentHashMap<>();
    private final ChatServer chatServer;
    private int messagesCount = 0;

    public ChatController(int port) {
        this.chatServer = new ChatServer(port, this);
    }

    public void addClient(Socket client) {

        logger.info("ZZZ Подключение клиента: {}:{}", client.getInetAddress(), client.getPort());
            new ChatClient(client, this);
    }

    public void stopChat() {
        logger.info("Получена команда на остановку чата");
        logger.info("Рассылка клиентам уведомлений о закрытии");
        for (ChatClient c : clients.values()) {
            c.close();
        }
        chatServer.interrupt();
        logger.info("Остановка сервера");

    }

    public void startChat() {
        chatServer.start();
    }

    public boolean registerUser(String username, ChatClient chatClient) {
        if (clients.containsKey(username)) return false;
        clients.put(username, chatClient);
        sendSystemMessage("К нам присоединился " + username);
        return true;
    }

    public synchronized void sendSystemMessage(String message) {
        message = "Системное сообщение: " + message;
        messagesLogger.info(message);
        for (ChatClient c : clients.values()) {
            c.send(message);
        }
    }

    public synchronized void sendAll(String message, ChatClient sender) {
        message = String.format("[%s] %s", sender.getUserName(), message);
        messagesLogger.info(message);
        for (ChatClient c : clients.values()) {
            if (c == sender) continue;
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
        clients.remove(clientName);
    }
}
