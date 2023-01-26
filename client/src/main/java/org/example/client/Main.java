package org.example.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.Scanner;

public class Main {
    private static final String APP_DIR = "_NET CHAT\\";
    private static final String SETTINGS_FILE_NAME = "client.properties";
    private static final Logger logger;
    private static final Path HOME_DIR = Path.of(System.getProperty("user.home") + "\\" + APP_DIR);
    private static final Path SETTINGS_FILE = Path.of(HOME_DIR + "\\" + SETTINGS_FILE_NAME);
    private static final Properties properties = new Properties();
    private static boolean isConnected = false;
    private static ChatClient client;

    static {
        System.setProperty("App.logs", HOME_DIR.toString());
        logger = LoggerFactory.getLogger(Main.class);
    }

    public static void main(String[] args) {
        logger.info("Начало работы");
        loadSettingsFromFile();
        System.out.println("Настройки загружены");
        printMenu();
        Scanner scanner = new Scanner(System.in);
        String in = "";
        while (true) {
            in = scanner.nextLine();
            if (in.equals("/start")) {
                if (isConnected) {
                    System.out.println("Вы уже подключены");
                } else {
                    connectToServer();
                }
            } else if (in.equals("/help")) {
                printMenu();
            } else if (in.equals("/exit")) {
                if (isConnected) {
                    client.close();
                    isConnected = false;
                }
                break;
            } else if (in.startsWith("/name")) {
                changeName(in);
            } else if (isConnected) {
                client.sendMessage(in);
            }
        }
    }

    private static void changeName(String in) {
        if (in.length() < 7) {
            System.out.println("Некорректное имя");
        } else {
            String newName = in.substring(6);
            properties.setProperty("username", newName);
            System.out.println("Имя изменено");
            saveSettingsToFile();
        }
    }

    private static void connectToServer() {
        client = new ChatClient(
                properties.getProperty("host"),
                Integer.parseInt(properties.getProperty("port")),
                properties.getProperty("username")
        );
        isConnected = client.connect();
        if (!isConnected) {
            System.out.println("Сервер отказал в соединении по причине: " + client.getReason());
        }
    }

    private static void loadSettingsFromFile() {

        if (Files.exists(SETTINGS_FILE)) {
            logger.info("Загрузка настроек приложения");
            try (var out = new FileInputStream(SETTINGS_FILE.toFile())) {
                properties.load(out);
            } catch (IOException e) {
                logger.error("Ошибка чтения файла настроек", e);
                System.out.println("Ошибка чтения файла настроек. Отредактируйте или удалите файл \n" + SETTINGS_FILE);
            }
        } else {
            System.out.println("Файл настроек отсутствует");
            Scanner scanner = new Scanner(System.in);
            System.out.println("Введите адрес сервера: ");
            String host = scanner.nextLine();
            System.out.println("Введите порт сервера: ");
            String port = scanner.nextLine();
            System.out.println("Введите ваше имя в чате: ");
            String username = scanner.nextLine();
            scanner.close();
            properties.setProperty("host", host);
            properties.setProperty("port", port);
            properties.setProperty("username", username);
            saveSettingsToFile();
        }
    }

    private static void saveSettingsToFile() {
        try (var out = new FileOutputStream(SETTINGS_FILE.toFile())) {
            properties.store(out, "");
            logger.info("Сохранение настроек приложения");
        } catch (IOException e) {
            logger.error("Ошибка создания настроек", e);
        }
    }

    private static void printMenu() {
        System.out.println("Доступные команды:");
        System.out.printf("%s   /start %s- подключиться к серверу%n", ConsoleColors.YELLOW, ConsoleColors.WHITE);
        System.out.printf("%s   /exit  %s- выход%n", ConsoleColors.YELLOW, ConsoleColors.WHITE);
        System.out.printf("%s   /help  %s- список доступных команд%n", ConsoleColors.YELLOW, ConsoleColors.WHITE);
        System.out.printf("%s   /name  %s- задать новое имя пользователя%n", ConsoleColors.YELLOW, ConsoleColors.WHITE);
    }
}
