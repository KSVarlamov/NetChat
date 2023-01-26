package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class IncomeHandler extends Thread {
    private final int port;
    private final ChatController controller;
    private final Logger logger = LoggerFactory.getLogger(IncomeHandler.class);

    public IncomeHandler(int port, ChatController controller) {
        this.port = port;
        this.controller = controller;
    }

    @Override
    public void run() {
        logger.info("Старт потока приема внешних сообщений");
        try (ServerSocket serverSocket = new ServerSocket(port)) {

            logger.info("Слушаю сообщения на порту {}", serverSocket.getLocalPort());
            serverSocket.setSoTimeout(10000);
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Socket client = serverSocket.accept();
                    controller.addClient(client);
                } catch (SocketTimeoutException ignored) {
                }
            }
        } catch (IOException e) {
            logger.error("Сокет закрыт");
        }
    }
}
