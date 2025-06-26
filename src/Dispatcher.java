import java.util.Queue;
import java.util.LinkedList;

public class Dispatcher {
    protected String extension;
    protected int maxThreadsNumber;
    protected int freeParsersNumber; //Количество доступных парсеров
    protected Queue<String> directions; //Очередь директорий для парсинга
    protected int processedFileNumber;
    protected int stringNumber;
    protected final Object lock; //Объект блокировки

    public Dispatcher(String direction, String extension, int threadsNumber){
        this.extension = extension;
        this.maxThreadsNumber = threadsNumber;
        this.freeParsersNumber = this.maxThreadsNumber;
        this.directions = new LinkedList<>();
        this.directions.offer(direction);
        this.lock = new Object();
    }

    //Сделать красиво
    public void run(){
        long startTime = System.currentTimeMillis();

        distributeTasks(); //Создать парсеры и озадачить работой

        synchronized (this.lock) {
            try {
                this.lock.wait(); // Основной поток блокируется
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        long endTime = System.currentTimeMillis();
        long duration = (endTime - startTime); // время в миллисекундах

        //Результаты
        System.out.println("Найдено файлов: " + this.processedFileNumber);
        System.out.println("Общее количество строк: " + this.stringNumber);
        System.out.println("Время выполнения: " + duration + " мсек");
    }

    public void updateResults(int processedFileNumber, int stringNumber, Queue<String> directions){
        synchronized (this.lock) { //дергаем общий ресурс...
            this.directions.addAll(directions);
            this.processedFileNumber = this.processedFileNumber + processedFileNumber;
            this.stringNumber += stringNumber;
            this.freeParsersNumber = this.freeParsersNumber + 1;
        }

        distributeTasks(); //Создать парсеры и озадачить работой
    }

    protected void distributeTasks(){
        String direction;

        synchronized (this.lock) { //дергаем общий ресурс...
            int freeParsersNumber = this.freeParsersNumber; //Кол-во будет обновляться, так что запомним сколько там в начале было
            for(int i = 1; i <= freeParsersNumber; i++) { //А кто тут баклуши бьёт?
                direction = this.directions.poll(); //Удаляет и возвращает директорию из начала очереди
                if (direction != null) {//Работа есть
                    this.freeParsersNumber = this.freeParsersNumber - 1;
                    Parser parser = new Parser(this, direction, this.extension);
                    parser.start(); //За работу!
                } else { //Работы нет...
                    if(this.freeParsersNumber==this.maxThreadsNumber){ //Таки больше работы нет совсем
                        this.lock.notifyAll(); //Разбудить основной поток
                        break;
                    }
                    else { //Остальные пока работают, а мы курим бамбук
                        break;
                    }
                }
            }
        }
    }
}
