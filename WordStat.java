import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

// Базовый класс для всех вариантов статистики слов (зад. 4, 5, 6)
// Статистика представляет собой LinkedHashMap<String, StatEntry> - т.е. структуру данных, в которой
// элементы одновременно добавляются в хеш-таблицу и в список. Благодаря хеш-таблице их можно быстро
// искать, а благодаря списку сохраняется порядок следования слов в тексте. Ключами хеш-таблицы
// являются, собственно, слова. Значениями - экземпляры класса StatEntry (на самом деле для зад. 4-5,
// достаточно было, чтобы значения были целыми числами, однако для зад. 6 помимо количества вхождений
// нужны еще индексы вхождений, поэтому используем класс StatEntry, объединяющий в себе все эти данные).
abstract class WordStat {
    // Aункция добавления очередного значения (слова, префикса или подстроки) в статистику
    static void addWordToStat(String word, Map<String, StatEntry> stat) {
        var en = stat.computeIfAbsent(word, k -> new StatEntry());
        ++en.count;

//        // если то, что выше - слишком сложно, то можно, вместо этого так:
//        var en = stat.get(word);
//        if (en != null) {
//            ++en.count;
//        } else {
//            en = new StatEntry();
//            en.count = 1;
//            stat.put(word, en);
//        }
    }

    // абстрактный метод учета очередного найденного слова для перегрузки в подклассах
    // в одних вариантах надо его учитывать целиком, в других - только его префис
    // в третьих - его подстроки
    // поэтому метод сделан абстрактным и предназначен для реализации в подклассах
    abstract protected void accountWord(String word, Map<String, StatEntry> stat);

    // (((((((((((((((((((((((((((((((((((
    // Вариант попроще - более прямой и самодостаточный, для задания 4
    
    // Функция "сброса" очередного накапливаемого слова в статистику
    private void flushWord(StringBuilder word, Map<String, StatEntry> stat) {
        // Для накопления слова лучше использовать класс StringBuilder, а не просто String,
        // т.к. String - immutable-класс, т.е. каждый его экземпляр будучи однажды создан
        // уже не может быть изменен (по крайней мере, простыми средствами), а при накоплении
        // мы все время изменяем накапливаемую строку, дописывая ей в конец еще символы.
        // В случае со String единственный вариант это сделать - создавать при каждом изменении
        // новый экземпляр данного класса, отличающийся от предыдущего добавленным символом.
        // При этом сначала все символы из старого копируются в новый (они какое-то время
        // существуют в памяти одновременно), после чего старый "выбрасывается" и через какое-то
        // время собирается сборщиком мусора. Это, очевидно, не эффективно ни по памяти, ни по
        // времени. StringBuilder же может изменяться внутри себя. Для этого в нем заранее
        // выделяется какой-то буфер памяти (длинее, чем надо, чтобы хранить изначальную строку,
        // в т.ч. и если она пустая) и если при очередном добавлении (append) получившаяся строка
        // все еще помещается в этом буфере (что в основном и происходит благодаря его стратегии
        // роста), то это добавление просиходит быстро - символы пишутся в уже выделенную память
        // и просто изменяется переменная, хранящая текущую заполненную длину в этом буфере.
        // Поэтому накапливать строки лучше при помощи StringBuilder
        
        // "Cброс" накапливаемого слова присходит, только если есть что сбрасывать,
        // т.е. если накопленное слово не пустое.
        if (word.length() > 0) {
            // word.toString() создает экземпляр String по текущему значению StringBuilder путем его копирования
            // Да, в этом месте можно было поступить и эффективнее, но не будем слишком усложнять.
            accountWord(word.toString(), stat);
            
            // После добавления накапливаемого слова в статистику, длина накапливаемого слова сбрасывается в 0,
            // чтобы как бы начать накопление нового слова заново c пустой строки. При этом также будет заново
            // использован старый буфер памяти, никакого перевыделения не происходит.
            word.setLength(0);
        }
    }

    // Функция проверка того, что очередной символ является символом слова
    private static boolean isWordChar(char ch) {
        return Character.isLetter(ch)
                || ch == '\''
                || Character.getType(ch) == Character.DASH_PUNCTUATION;
    }

    // Основной метод для зад. 4. Создает статистику и последовательно заполняет ее,
    Map<String, StatEntry> makeStat(Reader r) throws IOException {
        var stat = new LinkedHashMap<String, StatEntry>(); // создаем экземпляр нашей статистики
        var word = new StringBuilder(); // создаем экземпляр StringBuilder для нашего накапливаемого слова
        
        // На самом деле, совершенно не за чем заварачивать входной Reader в BufferedReader,
        // потому как следующей же строкой мы выделяем свой буфер (так просто удобнее работать с Reader).
        // Но раз просили - завернем в BufferedReader.
        r = new BufferedReader(r);
        
        char[] buf = new char[8192]; // Создаем в памяти буфер для хранения прочитанных из файла символов
        while (true) {  // цикл чтения из файла. Как бы бесконечный, но внутри имеется break, по которому
                        // и происходит выход из него
            int n = r.read(buf); // читаем из файла сколько получится, но не более buf.length символов
            // n - сколько символов удалось прочитать из файла
            if (n < 0) { 
                // если n < 0, то файл кончился - надо выходить из цикла
                break;
            }
            // обрабатываем считанные символы последовательно по одному
            for (int i = 0;  i < n;  ++i) {
                // для очередного символа проверяем, является ли он символом слова
                if (isWordChar(buf[i])) {
                    // если является - добавляем его к накапливаемому слову,
                    // сделав большую букву (если это она) маленькой
                    word.append(Character.toLowerCase(buf[i]));
                } else {
                    // Если встретился символ, не пренадлежащий слову, то значит очередное слово
                    // закончилось (если, конечно, оно успело начаться, т.е. это не очередной пробельный
                    // символ подряд), а значит надо "сбросить" его в статистику.
                    // Если это очередной пробельный символ подряд - то ничего страшного, т.к. внутри
                    // flushWord имеется проверка на непустоту накопленного слова.
                    flushWord(word, stat);
                }
            }
        }
        
        // В конце после цикла также надо сбросить накопленное слово в статистику, т.к. если последний
        // символ файл был символом слова, то мы еще не успели сбросить его внутри цикла.
        // Если успели - отработает проверка на непустоту слова внутри flushWord.
        flushWord(word, stat);
        
        return stat; // возвращаем нашу накопленную статистику
    }
    // )))))))))))))))))))))))))))))))))))
    
    // (((((((((((((((((((((((((((((((((((
    // Вариант посложнее - реализация через свой Scanner, для зад. 5-6.
    // А именно через WordStatScanner, который унаследован от MyScanner

//    // Основной метод для зад. 5-6. Создает статистику и последовательно заполняет ее,
//    // получая слова одно за одним из WordStatScanner.
//    // Для каждого слова вызывает тот самый, определенный в подклассах метод accountWord
//    Map<String, StatEntry> makeStat(Reader r) throws IOException {
//        var stat = new LinkedHashMap<String, StatEntry>();
//
//        // На самом деле, совершенно не за чем заварачивать в BufferedReader,
//        // потому как в MyScanner, от которого унаследован WordStatScanner,
//        // используется свой буффер (так просто удобнее работать с Reader).
//        // Но раз просили - завернем в BufferedReader
//        var s = new WordStatScanner(new BufferedReader(r));
//
//        while (s.hasNext()) {
//            // Наш сканнер для всеобщности выдает не просто строки, а экземпляры класса Token
//            // Вообще говоря, Token-ы бывают двух типов - слова и концы строк.
//            // Однако при сборе статистики по словам нам не важно, как именно текст был разбит
//            // на строки, поэтому мы отключили сканеру слежение за концами строк
//            // (см. конструктор WordStatScanner). Поэтому здесь мы не проверяем, является ли
//            // этот токен словом или концом строки - он всегда слово, поэтому просто берем его текст
//            // и делаем все большие буквы маленькими.
//            Token tok = s.next();
//            String word = tok.text;
//            accountWord(word.toLowerCase(), stat);
//        }
//        return stat;
//    }
    // )))))))))))))))))))))))))))))))))))

    // Метод пост-обработки статистики. Возвращает явно упорядоченную статитистику, причем,
    // ее порядок может не совпадать с порядком, в котором они сохранены в LinkedHashMap, т.е.
    // с порядком, в котором слова встречаются в тексте - в зависимости от реализации данной функции.
    protected Collection<Map.Entry<String, StatEntry>> postProcess(Map<String, StatEntry> stat) {
        // Реализация по умолчанию просто возвращает упорядоченное множество из накопленной LinkedHashMap,
        // т.е. порядок по умолчанию - это порядок, в котором слова добавлялись в статистику.
        return stat.entrySet();
    }

    // Метод печати статистики очередного слова в выходной поток p.
    protected void printStatEntry(PrintStream p, StatEntry en) {
        // Для всех вариантов задания 4 и задания 5 работает этот вариант по умолчанию, который
        // печатает лишь количество вхождений слова, игнорируя индексы.
        // В задании 6 данный метод перегружен.
        p.print(en.count);
    }

    // Метод сброса упорядоченной статистики в файл.
    private void writeStatToFile(Collection<Map.Entry<String, StatEntry>> stat, String filePath) throws IOException {
        // Открываем на запись файл в виде PrintStream безопасным с точки зрения исключения образом
        // (см. try-with-resources в документации на язык Java:
        //  https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html )
        // То, что внутри try (...) будет автоматически закрыто при выходе из блока после try,
        // даже если выход произойдет из-за выбрасывания исключения.
        try (PrintStream p = new PrintStream(filePath, StandardCharsets.UTF_8)) {
            for (Map.Entry<String, StatEntry> en : stat) { // просто обходим все слова в поданном нам порядке
                // И для каждого слова печатаем:
                p.print(en.getKey());               // это слово
                p.print(' ');                       // пробел после него
                printStatEntry(p, en.getValue());   // что-то, что напечатает метод printStatEntry (возможно перегшруженный в подклассах)
                p.println();                        // переход на новую строку
            }
        }
    }

    // Метод запуска чтения входного текста, построения и постобработки статистики и печати ее в выходной файл
    void process(String inFile, String outFile) throws IOException {
        Map<String, StatEntry> stat; // объявляем экземпляр статистики
        
        // Безопасно с точки зрения исключений (см. try-with-resources в документации на язык Java:
        //  https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html )
        // открываем файл входного текста в виде InputStream и оборачиваем его в Reader, чтобы самостоятельно не
        // возиться с кодировкой UTF-8, у которой переменная длина символа, а читать сразу посимвольно, а не побайтово.
        try (InputStream in = new FileInputStream(inFile);
             InputStreamReader r = new InputStreamReader(in, StandardCharsets.UTF_8))
        {
            stat = makeStat(r); // вызываем основной метод построения статистики
        }
        var sortedStat = postProcess(stat); // пост-обработка статистики (ее опциональное переупорядочивание)
        writeStatToFile(sortedStat, outFile); // запись переупорядоченной статистики в выходной файл
    }
}
