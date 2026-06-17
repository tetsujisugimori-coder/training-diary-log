import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class FitnessLogApp {
    private static final String APP_NAME = "家トレ帳 Java版";
    private static final Path CSV_PATH = Paths.get("fitness_log.csv");
    private static final String CSV_HEADER = "date,category,exercise_id,exercise_name,side,value,unit,set_count,interval_sec,distance_km,intensity,memo";

    private static final Scanner scanner = new Scanner(System.in);

    private static final Exercise[] STRENGTH_EXERCISES = {
        new Exercise("STRENGTH_SQUAT", "スクワット", false),
        new Exercise("STRENGTH_SITUP", "腹筋", false),
        new Exercise("STRENGTH_PUSHUP", "腕立て", false),
        new Exercise("STRENGTH_BACKEXT", "背筋", false),
        new Exercise("STRENGTH_PLANK", "プランク", false),
        new Exercise("STRENGTH_CALFRAISE", "かかと上げ", false),
        new Exercise("STRENGTH_OTHER", "その他（一時）", true)
    };

    private static final Exercise[] STRETCH_EXERCISES = {
        new Exercise("STRETCH_KUSSHIN", "屈伸", false),
        new Exercise("STRETCH_FORWARD", "前屈", false),
        new Exercise("STRETCH_SPLIT_FORWARD", "開脚前屈", false, "右", "左", "中央"),
        new Exercise("STRETCH_HIP_CIRCLE", "股関節回し", false),
        new Exercise("STRETCH_PRONE_HIP_90", "うつ伏せ股関節90度（上体反らし）", false, "右", "左"),
        new Exercise("STRETCH_SUPINE_QUAD", "仰向け前もも伸ばし（股関節前側）", false, "右", "左"),
        new Exercise("STRETCH_STANDING_QUAD", "立ち前もも伸ばし（壁支え）", false, "右", "左"),
        new Exercise("STRETCH_SEATED_TWIST", "座位腰ひねり（脚組み）", false, "右", "左"),
        new Exercise("STRETCH_ACHILLES", "アキレス腱伸ばし", false),
        new Exercise("STRETCH_SHOULDER", "肩回し", false),
        new Exercise("STRETCH_NECK", "首回し", false),
        new Exercise("STRETCH_SIDE", "体側伸ばし", false),
        new Exercise("STRETCH_HAMSTRING", "太もも裏伸ばし", false),
        new Exercise("STRETCH_CALF", "ふくらはぎ伸ばし", false),
        new Exercise("STRETCH_OTHER", "その他（一時）", true)
    };

    private static final Exercise[] WALK_EXERCISES = {
        new Exercise("WALK_NORMAL", "散歩", false)
    };

    public static void main(String[] args) {
        System.out.println(APP_NAME);

        while (true) {
            int menu = showMainMenu();

            if (menu == 1) {
                addRecord();
            } else if (menu == 2) {
                listRecords();
            } else if (menu == 3) {
                System.out.println("終了します。");
                break;
            }
        }
    }

    private static int showMainMenu() {
        System.out.println();
        System.out.println("1. 記録を追加する");
        System.out.println("2. ログを一覧表示する");
        System.out.println("3. 終了する");
        return inputInt("番号を入力してください: ", 1, 3);
    }

    private static void addRecord() {
        String category = selectCategory();
        Exercise exercise = selectExercise(category);
        String exerciseName = exercise.name;

        if (exercise.isOther) {
            exerciseName = inputNonEmptyString("具体的な種目名を入力してください: ");
        }

        String side = selectSide(exercise);
        Record record = new Record();
        record.date = LocalDate.now().toString();
        record.category = category;
        record.exerciseId = exercise.id;
        record.exerciseName = exerciseName;
        record.side = side;
        record.distanceKm = 0.0;

        if ("筋トレ".equals(category)) {
            record.value = inputInt("1セットあたりの回数、または秒数: ", 1, Integer.MAX_VALUE);
            record.unit = selectUnit();
            record.setCount = inputInt("セット数: ", 1, Integer.MAX_VALUE);
            record.intervalSec = inputInt("インターバル秒数: ", 0, Integer.MAX_VALUE);
            record.intensity = inputIntensity();
            record.memo = inputMemo();
        } else if ("柔軟体操".equals(category)) {
            record.value = inputInt("1回あたりの秒数: ", 1, Integer.MAX_VALUE);
            record.unit = "秒";
            record.setCount = inputInt("セット数: ", 1, Integer.MAX_VALUE);
            record.intervalSec = inputInt("インターバル秒数: ", 0, Integer.MAX_VALUE);
            record.intensity = inputIntensity();
            record.memo = inputMemo();
        } else {
            record.value = inputInt("時間（分）: ", 1, Integer.MAX_VALUE);
            record.unit = "分";
            record.setCount = 1;
            record.intervalSec = 0;
            record.distanceKm = inputDouble("距離km: ", 0.0);
            record.intensity = inputIntensity();
            record.memo = inputMemo();
        }

        try {
            saveRecord(record);
            System.out.println("記録を保存しました。");
        } catch (IOException e) {
            System.out.println("保存に失敗しました: " + e.getMessage());
        }
    }

    private static String selectCategory() {
        System.out.println();
        System.out.println("カテゴリを選んでください。");
        System.out.println("1. 筋トレ");
        System.out.println("2. 柔軟体操");
        System.out.println("3. 散歩");

        int choice = inputInt("番号を入力してください: ", 1, 3);
        if (choice == 1) {
            return "筋トレ";
        } else if (choice == 2) {
            return "柔軟体操";
        } else {
            return "散歩";
        }
    }

    private static Exercise selectExercise(String category) {
        Exercise[] exercises;
        if ("筋トレ".equals(category)) {
            exercises = STRENGTH_EXERCISES;
        } else if ("柔軟体操".equals(category)) {
            exercises = STRETCH_EXERCISES;
        } else {
            exercises = WALK_EXERCISES;
        }

        System.out.println();
        System.out.println("種目を選んでください。");
        for (int i = 0; i < exercises.length; i++) {
            System.out.println((i + 1) + ". " + exercises[i].name);
        }

        int choice = inputInt("番号を入力してください: ", 1, exercises.length);
        return exercises[choice - 1];
    }

    private static String selectSide(Exercise exercise) {
        if (exercise.sides.length == 0) {
            return "なし";
        }

        System.out.println();
        System.out.println("方向・部位を選んでください。");
        for (int i = 0; i < exercise.sides.length; i++) {
            System.out.println((i + 1) + ". " + exercise.sides[i]);
        }

        int choice = inputInt("番号を入力してください: ", 1, exercise.sides.length);
        return exercise.sides[choice - 1];
    }

    private static String selectUnit() {
        System.out.println();
        System.out.println("単位を選んでください。");
        System.out.println("1. 回");
        System.out.println("2. 秒");
        int choice = inputInt("番号を入力してください: ", 1, 2);
        return choice == 1 ? "回" : "秒";
    }

    private static int inputIntensity() {
        System.out.println();
        System.out.println("きつさを入力してください。");
        System.out.println("1 = かなり楽");
        System.out.println("2 = やや楽");
        System.out.println("3 = 普通");
        System.out.println("4 = きつい");
        System.out.println("5 = かなりきつい");
        return inputInt("きつさ 1-5: ", 1, 5);
    }

    private static int inputInt(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            try {
                int value = Integer.parseInt(input);
                if (value >= min && value <= max) {
                    return value;
                }
            } catch (NumberFormatException e) {
                // 数字ではない入力も、範囲外の数字も同じように再入力してもらいます。
            }

            System.out.println("無効な入力です。もう一度入力してください。");
        }
    }

    private static double inputDouble(String prompt, double min) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            try {
                double value = Double.parseDouble(input);
                if (value >= min) {
                    return value;
                }
            } catch (NumberFormatException e) {
                // 数値に変換できない場合は再入力してもらいます。
            }

            System.out.println("無効な入力です。もう一度入力してください。");
        }
    }

    private static String inputNonEmptyString(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (!input.isEmpty()) {
                return input;
            }
            System.out.println("空欄にはできません。もう一度入力してください。");
        }
    }

    private static String inputMemo() {
        System.out.print("メモ: ");
        return scanner.nextLine();
    }

    private static void saveRecord(Record record) throws IOException {
        boolean needsHeader = !Files.exists(CSV_PATH) || Files.size(CSV_PATH) == 0;

        try (BufferedWriter writer = Files.newBufferedWriter(
                CSV_PATH,
                StandardCharsets.UTF_8,
                java.nio.file.StandardOpenOption.CREATE,
                java.nio.file.StandardOpenOption.APPEND)) {
            if (needsHeader) {
                writer.write(CSV_HEADER);
                writer.newLine();
            }

            writer.write(String.join(",",
                escapeCsv(record.date),
                escapeCsv(record.category),
                escapeCsv(record.exerciseId),
                escapeCsv(record.exerciseName),
                escapeCsv(record.side),
                escapeCsv(String.valueOf(record.value)),
                escapeCsv(record.unit),
                escapeCsv(String.valueOf(record.setCount)),
                escapeCsv(String.valueOf(record.intervalSec)),
                escapeCsv(String.valueOf(record.distanceKm)),
                escapeCsv(String.valueOf(record.intensity)),
                escapeCsv(record.memo)
            ));
            writer.newLine();
        }
    }

    private static void listRecords() {
        if (!Files.exists(CSV_PATH)) {
            System.out.println("まだ記録がありません。");
            return;
        }

        List<Record> records = new ArrayList<>();

        try {
            List<String> csvRecords = readCsvRecords(CSV_PATH);
            for (int i = 0; i < csvRecords.size(); i++) {
                if (i == 0) {
                    continue;
                }

                List<String> cols = parseCsvLine(csvRecords.get(i));
                if (cols.size() < 12) {
                    continue;
                }

                records.add(recordFromColumns(cols));
            }
        } catch (IOException e) {
            System.out.println("読み込みに失敗しました: " + e.getMessage());
            return;
        }

        if (records.isEmpty()) {
            System.out.println("まだ記録がありません。");
            return;
        }

        System.out.println();
        System.out.printf("%-12s  %-8s  %-30s  %-6s  %-18s  %-6s  %s%n",
                "日付", "カテゴリ", "種目", "方向", "内容", "きつさ", "メモ");

        for (Record record : records) {
            System.out.printf("%-12s  %-8s  %-30s  %-6s  %-18s  %-6d  %s%n",
                    record.date,
                    record.category,
                    record.exerciseName,
                    record.side,
                    formatContent(record),
                    record.intensity,
                    record.memo);
        }
    }

    private static String escapeCsv(String value) {
        if (value == null) {
            value = "";
        }

        boolean needsQuote = value.contains(",") || value.contains("\"")
                || value.contains("\n") || value.contains("\r");
        String escaped = value.replace("\"", "\"\"");

        if (needsQuote) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }

    private static List<String> parseCsvLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                values.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }

        values.add(current.toString());
        return values;
    }

    private static List<String> readCsvRecords(Path path) throws IOException {
        List<String> records = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            StringBuilder record = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                if (record.length() > 0) {
                    record.append("\n");
                }
                record.append(line);

                // メモ内の改行など、引用符の中の改行は1件のCSVとしてまとめます。
                if (isCsvRecordComplete(record.toString())) {
                    records.add(record.toString());
                    record.setLength(0);
                }
            }

            if (record.length() > 0) {
                records.add(record.toString());
            }
        }

        return records;
    }

    private static boolean isCsvRecordComplete(String text) {
        boolean inQuotes = false;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < text.length() && text.charAt(i + 1) == '"') {
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            }
        }

        return !inQuotes;
    }

    private static Record recordFromColumns(List<String> cols) {
        Record record = new Record();
        record.date = cols.get(0);
        record.category = cols.get(1);
        record.exerciseId = cols.get(2);
        record.exerciseName = cols.get(3);
        record.side = cols.get(4);
        record.value = parseIntOrDefault(cols.get(5), 0);
        record.unit = cols.get(6);
        record.setCount = parseIntOrDefault(cols.get(7), 0);
        record.intervalSec = parseIntOrDefault(cols.get(8), 0);
        record.distanceKm = parseDoubleOrDefault(cols.get(9), 0.0);
        record.intensity = parseIntOrDefault(cols.get(10), 0);
        record.memo = cols.get(11);
        return record;
    }

    private static int parseIntOrDefault(String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private static double parseDoubleOrDefault(String value, double defaultValue) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private static String formatContent(Record record) {
        if ("散歩".equals(record.category)) {
            return record.value + record.unit + " / " + record.distanceKm + "km";
        }
        return record.value + record.unit + " x " + record.setCount + "セット";
    }

    private static class Exercise {
        String id;
        String name;
        boolean isOther;
        String[] sides;

        Exercise(String id, String name, boolean isOther, String... sides) {
            this.id = id;
            this.name = name;
            this.isOther = isOther;
            this.sides = sides;
        }
    }

    private static class Record {
        String date;
        String category;
        String exerciseId;
        String exerciseName;
        String side;
        int value;
        String unit;
        int setCount;
        int intervalSec;
        double distanceKm;
        int intensity;
        String memo;
    }
}
