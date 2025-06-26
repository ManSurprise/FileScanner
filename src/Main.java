public class Main {
    public static void main(String[] args) {
        String direction = "";
        String extension = "";
        int threadsNumber = 1;

        //Разбить строку на параметры по-человечески, а не как они нам пришли
        String allArguments = "";
        for (int i = 0; i < args.length; i++){
            allArguments = allArguments + " "+ args[i];
        }

        //allArguments = "java -jar FileScanner.jar --dir=C:\\Program Files (x86) --ext=.txt --threads=20"; //Тест
        //allArguments = "java -jar FileScanner.jar --dir=C:/JavaProjects/FileScanner/out/artifacts/FileScanner_jar --ext=.txt --threads=20"; //Тест

        String[] humanArgs = allArguments.split("--");

        //Распарсить аргументы из командной строки
        for (int i = 0; i < humanArgs.length; i++) {
            humanArgs[i] = humanArgs[i].trim(); //Обрезать пробелы

            String[] argument = humanArgs[i].split("=");
            if(argument.length >= 2) {
                switch (argument[0]) {
                    case "dir":
                        direction = argument[1];
                        break;
                    case "ext":
                        extension = argument[1];
                        break;
                    case "threads":
                        try {
                            threadsNumber = Integer.parseInt(argument[1]);
                        } catch (NumberFormatException e) {
                            System.out.println("Строка не является числом: " + argument[1]);
                        }
                        break;
                    default:
                        System.out.println("Неизвестная опция: " + argument[0]);
                }
            }
        }

        //Что имеем с птицы гусь
        System.out.println("Директория: " + direction);
        System.out.println("Расширение: " + extension);
        System.out.println("Максимальное количество потоков: " + threadsNumber);

        if(direction.isEmpty() || extension.isEmpty() || threadsNumber==0) {
            System.out.println("О, нет! Некорректный набор параметров, программа завершена");
            return;
        }

        Dispatcher dispatcher = new Dispatcher(direction, extension, threadsNumber); //Создаём диспетчера
        dispatcher.run(); //И пусть он с этим разбирается, он умеет, могёт
    }
}