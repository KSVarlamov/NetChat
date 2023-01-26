package org.example;

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
    public static final Scanner SCANNER = new Scanner(System.in);
    private static final String APP_DIR = "_NET CHAT\\";
    private static final String SETTINGS_FILE_NAME = "server.properties";
    private static final Logger logger;
    private static final Path HOME_DIR = Path.of(System.getProperty("user.home") + "\\" + APP_DIR);
    private static final Path SETTINGS_FILE = Path.of(HOME_DIR + "\\" + SETTINGS_FILE_NAME);
    private static final Properties properties = new Properties();
    private static int port = 6644;
    private static ChatController controller;


    static {
        System.setProperty("App.logs", HOME_DIR.toString());
        logger = LoggerFactory.getLogger(Main.class);
    }

    public static void main(String[] args) {
        logger.info("Запуск сервера");
        loadSettingsFromFile();
        controller = new ChatController(port);
        System.out.println("Добро пожаловать в консоль управления сервером. Чат-сервер стартовал на порту " + port);
        controller.startChat();
        printMenu();
        boolean isExit = false;
        while (!isExit) {
            String command = SCANNER.nextLine();
            switch (command) {
                case "/stop":
                    logger.info("Остановка сервера чата");
                    controller.stopChat();
                    isExit = true;
                    break;
                case "/help":
                    printMenu();
                    break;
                case "/stat":
                    printStatistics();
                    break;
                default:
                    System.out.println("Неизвестная команда");
                    printMenu();
            }
        }
        SCANNER.close();
    }

    private static void printStatistics() {
        StringBuilder stat = new StringBuilder();
        stat.append("Сейчас на сервере: ").append(controller.getClientsCount()).append("\n")
                .append("Все отправлено сообщений: ").append(controller.getMessagesCount());
        System.out.println(stat);
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
            System.out.println("Введите порт сервера: ");
            String newPort = SCANNER.nextLine();
            properties.setProperty("port", newPort);
            port = Integer.parseInt(newPort);
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
        System.out.printf("%s   /stop %s- остановка работы сервера%n", ConsoleColors.YELLOW, ConsoleColors.WHITE);
        System.out.printf("%s   /stat %s- текущая статистика%n", ConsoleColors.YELLOW, ConsoleColors.WHITE);
        System.out.printf("%s   /help %s- вывод списка доступных команд%n", ConsoleColors.YELLOW, ConsoleColors.WHITE);
    }
}