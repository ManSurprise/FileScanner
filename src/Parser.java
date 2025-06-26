import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.Queue;
import java.util.stream.Stream;
import java.nio.file.*;
import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipFile;

import static java.nio.file.Files.*;

public class Parser extends Thread {
    protected Dispatcher dispatcher;
    protected String direction;
    protected String extension;
    protected int fileNumber;   //Не двигать отсюда,а то лямбда не сработает!
    protected int stringNumber; //Не двигать отсюда,а то лямбда не сработает!

    public Parser(Dispatcher dispatcher, String direction, String extension){
        this.dispatcher = dispatcher;
        this.direction = direction;
        this.extension = extension;
    }

    @Override
    public void run(){
        Queue<String> directions = new LinkedList<>(); //Очередь вновь найденных папок
        this.stringNumber = 0;
        this.fileNumber = 0;

        //чёт поработать...
        Path dir = Paths.get(this.direction);

        try (Stream<Path> paths = list(dir)) {
            paths.forEach(path -> {
                String name = path.getFileName().toString();
                String fullName = this.direction+"/"+path.getFileName().toString();
                String extension = "";

                if (isRegularFile(path)) {
                    int dotIndex = name.lastIndexOf('.');
                    if (dotIndex > 0 && dotIndex < name.length() - 1) {
                        extension = name.substring(dotIndex);
                        if(extension.trim().equals(this.extension.trim()) && !extension.trim().equals(".zip")){
                            this.fileNumber = this.fileNumber + 1;

                            try {
                                this.stringNumber += countLines(fullName);
                            } catch (IOException e) {
                                System.err.println("Ошибка подсчёта строк в файле: " + fullName + " " + e.getMessage());
                            }
                        }
                        else if(extension.trim().equals(".zip")){
                            try {
                                unzip(fullName);
                            } catch (IOException e) {
                                System.err.println("Ошибка разархирования файла: " + fullName + " " + e.getMessage());
                            }
                        }
                    }
                } else if (isDirectory(path)) {
                    directions.offer(fullName); //Вот еще нашел директорию, обработать надо
                }
            });
        } catch (IOException e) {
            System.err.println("Ошибка при чтении папки: " + e.getMessage());
        }

        //Вернуть результат
        this.dispatcher.updateResults(this.fileNumber, this.stringNumber, directions);
    }

    public static long countLines(String filePath) throws IOException {
        long lineCount = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            while (reader.readLine() != null) {
                lineCount++;
            }
        }
        return lineCount;
    }

    public void unzip(String zipFilePath) throws IOException{
        //Рекурсивно прочитать файлы из потока архива
        readFilesFromZIPSteam(new FileInputStream(zipFilePath));
    }

    protected void readFilesFromZIPSteam(InputStream steam) throws IOException{
        String extension = "";

        try (ZipInputStream zis = new ZipInputStream(new BufferedInputStream(steam))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                String filePath = entry.getName();

                if (!entry.isDirectory()) {
                    int dotIndex = filePath.lastIndexOf('.');
                    if (dotIndex > 0 && dotIndex < filePath.length() - 1) {
                        extension = filePath.substring(dotIndex);
                    }

                    if(extension.trim().equals(this.extension.trim()) && !extension.trim().equals(".zip")){
                        this.fileNumber = this.fileNumber + 1;

                        //Шалость удалась... Непонятно как, Но это работает!
                        Reader reader = new InputStreamReader(zis);
                        BufferedReader br = new BufferedReader(reader);
                        String line;
                        while ((line = br.readLine()) != null) {
                            this.stringNumber++;
                        }

                    }
                    else if(extension.trim().equals(".zip")){
                        readFilesFromZIPSteam(zis); //Рекурсивно прочитать файлы из потока архива
                    }
                }
                zis.closeEntry();
            }
        } catch (IOException e) {
            //e.printStackTrace();
            System.err.println("Ошибка разархирования");
        }
    }
}
