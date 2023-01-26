package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {
    private static final String APP_DIR = "_NET CHAT\\";
    private static final String SETTINGS_FILE_NAME = "server_settings.txt";
    private static final Logger logger;
    private static final Path HOME_DIR = Path.of(System.getProperty("user.home") + "\\" + APP_DIR);

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
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            boolean isExit = false;
            while (!isExit) {
                String command = reader.readLine();

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
        } catch (IOException e) {
            logger.error("Ошибка работы с консолью",e);
        }
    }

    private static void printStatistics() {
        StringBuilder stat = new StringBuilder();
        stat.append("Сейчас на сервере: ").append(controller.getClientsCount()).append("\n")
                .append("Все отправлено сообщений: ").append(controller.getMessagesCount());
        System.out.println(stat);
    }

    private static void loadSettingsFromFile() {
        logger.info("Загрузка конфигурации");
        Path settingsPath = HOME_DIR.resolve(SETTINGS_FILE_NAME);
        if (Files.exists(settingsPath)) {
            try (FileReader fileReader = new FileReader(settingsPath.toFile());
                 BufferedReader reader = new BufferedReader(fileReader)) {
                port = Integer.parseInt(reader.readLine());
            } catch (Exception e) {
                logger.error("Ошибка загрузки конфигурации", e);
            }
        } else {
            crateDefaultSettingsFile(settingsPath);
        }
    }

    private static void crateDefaultSettingsFile(Path settings) {

        try {
            Files.createFile(settings);
            Files.write(settings, String.valueOf(port).getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            logger.error("Ошибка создания файла настроек", e);
        }


    }

    private static void printMenu() {
        System.out.println("Доступные команды:");
        System.out.printf("%s   /stop %s- остановка работы сервера%n", ConsoleColors.YELLOW, ConsoleColors.WHITE);
        System.out.printf("%s   /stat %s- текущая статистика%n", ConsoleColors.YELLOW, ConsoleColors.WHITE);
        System.out.printf("%s   /help %s- вывод списка доступных команд%n", ConsoleColors.YELLOW, ConsoleColors.WHITE);
    }
}