package khanin.dmitrii.hashMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Logic {
    private static String[] keywords = new String[] {
            "Override", "String", "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class",
            "const", "continue", "default", "do", "double", "else", "enum", "extends", "false", "final", "finally",
            "float", "for", "goto", "if", "implements", "import", "instanceof", "int", "interface", "long", "native",
            "new", "package", "private", "protected", "public", "return", "short", "static", "strictfp", "super",
            "switch", "synchronized", "this", "throw", "throws", "transient", "true", "try", "void", "volatile", "while"
    };

    public static String obfuscation(String origin) {
        StringBuilder result = new StringBuilder();
        MyHashMap<String, String> classesAndMethods = new MyHashMap<>();
        int obfuscatingNum = 1;
        {
            // Первый проход, ищем классы и функции объявленные в этом файле, а также импортированные классы

            // Разделяем код на отдельные слова
            String[] words = origin.split("[ \n<]");
            {
                // Убираем пустые слова
                List<String> wordsList = new ArrayList<>();
                for (int i = 0; i < words.length; i++) {
                    if (!words[i].isEmpty()) {
                        wordsList.add(words[i]);
                    }
                }
                words = wordsList.toArray(new String[0]);
            }

            // Пробегаемся по всем словам, ищем объявленные классы и методы, а также импортированные классы
            for (int i = 0; i < words.length; i++) {
                String word = words[i];
                if (word.equals("class")) {
                    // нашли объвленный в этом файле класс
                    String createdClass = words[++i];
                    if (!classesAndMethods.containsKey(createdClass)) {
                        classesAndMethods.put(createdClass, "v" + obfuscatingNum++);
                    }
                } else if (word.equals("public") && i + 3 < words.length && words[i + 1].equals("static")
                        && words[i + 2].equals("void") && words[i + 3].startsWith("main")) {
                    // public static void main, игнорируем его
                    i += 3;
                } else if (word.equals("@Override")) {
                    // @Override метод, игнорируем его
                    int j = 1;
                    while (i + j < words.length) {
                        String nextWord = words[i + j];
                        if (!((nextWord.equals("static") || nextWord.equals("final") || nextWord.equals("synchronized")
                                || nextWord.equals("abstract") || nextWord.equals("volatile")
                                || nextWord.equals("transient") || nextWord.equals("public")
                                || nextWord.equals("protected") || nextWord.equals("private")))) {
                            i += j;
                            break;
                        }
                        j++;
                    }
                } else if (word.equals("public") || word.equals("private") || word.equals("protected")
                        || word.equals("abstract")) {
                    // Может быть объявленным классом/методом/полем, нужно узнать метод это или нет
                    // Если это класс, то он обфусцируется на следующем шаге цикла
                    // Если это поле, то его заменим во втором проходе по коду
                    int j = 1;
                    while (i + j < words.length) {
                        String nextWord = words[i + j];
                        if (!((nextWord.equals("static") || nextWord.equals("final") || nextWord.equals("synchronized")
                                || nextWord.equals("abstract") || nextWord.equals("volatile")
                                || nextWord.equals("transient")))) {
                            if (nextWord.equals("class")) break; // это класс, идем дальше
                            String method = i + j + 1 < words.length ? words[i + j + 1] : "";
                            String afterMethod = i + j + 2 < words.length ? words[i + j + 2] : "";
                            if (method.contains("(")) {
                                // нашли объявленный метод, обфусцируем его
                                method = method.substring(0, method.indexOf("("));
                                if (!classesAndMethods.containsKey(method)) {
                                    classesAndMethods.put(method, "v" + obfuscatingNum++);
                                }
                                i++;
                            } else if (afterMethod.startsWith("(")) {
                                // нашли метод, обфусцируем его
                                if (method.equals("main")) {
                                    i += j;
                                    break;
                                }
                                if (!classesAndMethods.containsKey(method)) {
                                    classesAndMethods.put(method, "v" + obfuscatingNum++);
                                }
                                i++;
                            }
                            // это объявленное поле, игнорируем его
                            i += j - 1;
                            break;
                        }
                        j++;
                    }
                } /*else if (word.equals("import")) {
                    // Нашли импортированный класс, не обфусцируем его
                    String imported = words[i + 1].substring(0, words[i + 1].indexOf(";"));
                    classesAndMethods.put(imported, imported);
                    String[] importedPackage = imported.split("\\.");
                    imported = importedPackage[importedPackage.length - 1];
                    classesAndMethods.put(imported, imported);
                }*/
            }
        }

        {
            // Второй проход, собираем результат, обфусцируем все что нужно
            StringBuilder wordBuilder = new StringBuilder();
            boolean isSingleQuotationsOpened = false;
            boolean isDoubleQuotationsOpened = false;
            for (char c : origin.toCharArray()) {
                if (c == '\"') {
                    result.append(c);
                    isDoubleQuotationsOpened = !isDoubleQuotationsOpened;
                } else if (c == '\'') {
                    result.append(c);
                    isSingleQuotationsOpened = !isSingleQuotationsOpened;
                } else if (isSingleQuotationsOpened || isDoubleQuotationsOpened) {
                    // Кавычки открыты, просто добавляем символ в результат
                    result.append(c);
                } else if (isSuitableChar(c)) {
                    // Кавычки закрыты, данный символ является символом класса/метода/поля
                    wordBuilder.append(c);
                } else {
                    // Кавычки закрыты, данный символ не является символом класса/метода/поля
                    String word = wordBuilder.toString();
                    wordBuilder.setLength(0);
                    if (word.isEmpty() || word.charAt(0) >= '0' && word.charAt(0) <= '9') result.append(word);
                    else if (classesAndMethods.containsKey(word)) {
                        // Это объявленный в этом файле класс/метод, обфусцируем его
                        result.append(classesAndMethods.get(word));
                    } else result.append(word);
                    result.append(c);
                }
            }
        }

        return result.toString();

        /*StringBuilder sb = new StringBuilder();
        String nl = System.getProperty("line.separator");
        char lastSep = '-';
        for (String s : origin.split("\n")) {
            StringBuilder wordSb = new StringBuilder();
            boolean isSingleQuotationsOpened = false;
            boolean isDoubleQuotationsOpened = false;
            for (char c : s.toCharArray()) {
                if (c == '\"') {
                    sb.append(c);
                    isDoubleQuotationsOpened = !isDoubleQuotationsOpened;
                } else if (c == '\'') {
                    sb.append(c);
                    isSingleQuotationsOpened = !isSingleQuotationsOpened;
                } else if (!isSingleQuotationsOpened && !isDoubleQuotationsOpened) {
                    if (isSuitableChar(c)) {
                        wordSb.append(c);
                    } else {
                        String word = wordSb.toString();
                        wordSb.setLength(0);
                        if (word.isEmpty() || word.charAt(0) >= '0' && word.charAt(0) <= '9'
                                || word.equals("main") && c == '(') sb.append(word);
                        else if (!isKeyword(word)) {
                            if (map.containsKey(word)) {
                                sb.append(map.get(word));
                            } else {
                                String newWord = "v" + i++;
                                map.put(word, newWord);
                                sb.append(newWord);
                            }
                        } else {
                            sb.append(word);
                        }
                        sb.append(c);
                    }
                } else {
                    sb.append(c);
                }
            }
            String word = wordSb.toString();
            wordSb.setLength(0);
            if (word.isEmpty() || word.charAt(0) >= '0' && word.charAt(0) <= '9') sb.append(word);
            else if (!isKeyword(word)) {
                if (map.containsKey(word)) {
                    sb.append(map.get(word));
                } else {
                    String newWord = "v" + i++;
                    map.put(word, newWord);
                    sb.append(newWord);
                }
            } else {
                sb.append(word);
            }
            sb.append(nl);
        }
        return sb.toString();*/
    }

    private static boolean isKeyword(String word) {
        return Arrays.binarySearch(keywords, word) >= 0;
    }

    private static boolean isSuitableChar(char c) {
        return c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c == '$' || c == '_' || c >= '0' && c <= '9';
    }
}
