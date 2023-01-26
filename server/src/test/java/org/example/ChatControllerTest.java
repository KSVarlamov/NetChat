package org.example;

import org.junit.jupiter.api.*;

import java.io.*;
import java.net.Socket;
import java.nio.file.Path;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ChatControllerTest {
    static ChatController controller;
    static int port = 6644;
    static final Socket socketOne;
    static final BufferedReader inOne;
    static final BufferedWriter outOne;
    static {
        try {
            String APP_DIR = "_NET CHAT\\";
            Path HOME_DIR = Path.of(System.getProperty("user.home") + "\\" + APP_DIR);
            System.setProperty("App.logs", HOME_DIR.toString());
            controller = new ChatController(port);
            controller.startChat();
            socketOne = new Socket("localhost", port);
            inOne = new BufferedReader(new InputStreamReader(socketOne.getInputStream()));
            outOne = new BufferedWriter(new OutputStreamWriter(socketOne.getOutputStream()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }



    @AfterAll
    public static void clean() {
        controller.stopChat();
//        try {
//            inOne.close();
//            outOne.close();
//            socketOne.close();
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
    }

    @Test
    @Order(1)
    void clientStartConnection() {
        controller.addClient(socketOne);
        try {
            String line = inOne.readLine();
            Assertions.assertEquals("For start send \"/start <username>", line);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    @Order(2)
    void registerUsers() {
        try {
            int clients = controller.getClientsCount();
            outOne.write("/start User1");
            outOne.newLine();
            outOne.flush();
            String response = inOne.readLine();
            Assertions.assertEquals("/ok", response);
            Assertions.assertEquals(controller.getClientsCount(), ++clients);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
//    @Test
//    @Order(3)
//    void registerAnotherUserOnSocket() {
//        try {
//            int clients = controller.getClientsCount();
//            outOne.write("/start User1");
//            outOne.newLine();
//            outOne.flush();
//            String response = null;
//            if (inOne.ready()) response = inOne.readLine();
//            Assertions.assertEquals("/error incorrect user name", response);
//            Assertions.assertEquals(controller.getClientsCount(), clients);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }

}
