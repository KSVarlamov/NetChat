package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;


public class ChatClient extends Thread {
    private final Logger logger = LoggerFactory.getLogger(ChatClient.class);
    private Socket socket;
    private BufferedReader in;
    private BufferedWriter out;
    private volatile String userName = null;
    private ChatController controller;
    private boolean isTimeToDie = false;

    public ChatClient(Socket socket, ChatController controller) {
        try {
            this.socket = socket;
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.controller = controller;
            this.start();
        } catch (IOException e) {
            logger.error("Ошибка работы с сокетом", e);
        }
    }

    @Override
    public void run() {
        send("For start send \"/start <username>");

        String message;
        try {
            while (true) {
                message = in.readLine();
                if (message == null) {
                    logger.info("Поток пользователя [{}] потерял соединение", userName);
                    if (userName != null) controller.connectionLost(userName);
                    break;
                }
                if (message.startsWith("/")) {
                    processMessage(message);
                }
            }
        } catch (SocketException e) {
                logger.info("Отключение клиента [{}]", userName);
        } catch (IOException e) {
            logger.error("IO Error", e);
        }
        controller.connectionLost(userName);
        logger.info("Поток пользователя [{}] закончил работу", userName);
    }

    private synchronized void registerUser(String name) {
        if (this.userName != null) {
            send("/error incorrect user name (null name)");
            return;
        }
        String tmp = name.substring(7);
        if (tmp.length() > 0) {
            boolean canRegister = controller.canRegister(tmp);
            if (canRegister) {
                if (controller.registerUser(tmp, this)) {
                    this.userName = tmp;
                    send("/ok");
                    controller.sendSystemMessage("К нам присоединился " + tmp);
                    logger.info("Поток пользователя [{}] зарегистрирован на сервере", this.userName);
                } else {
                    send("/error incorrect user name (unknown err)");
                }
            } else {
                send("/error incorrect user name (user already registered)");
            }
        }
    }

    private void processMessage(String message) {
        if (message.startsWith("/start ")) {
            registerUser(message);
        } else if (message.equals("/stop")) {
            controller.connectionLost(userName);
            controller.sendSystemMessage(String.format("Пользователь [%s] вышел из чата", userName));
            close();
        } else if (message.startsWith("/message ")) {
            String mess = message.substring(9);
            if (mess.length() > 0) {
                controller.sendAll(mess, this);
            }
        }
    }

    void send(String msg) {
        try {
            out.write(msg + "\n");
            out.flush();
        } catch (IOException e) {
            logger.error("Сокет клиента [" + userName + "] внезапно закрыт", e);
        }
    }

    void close() {
        try {
            logger.info("Поток пользователя [{}] заканчивает работу", userName);
            isTimeToDie = true;
            socket.close();
        } catch (IOException e) {
            logger.error("Ошибка закрытия сокета клиента [" + userName + "]", e);
        }
    }

    public String getUserName() {
        return userName;
    }
}
