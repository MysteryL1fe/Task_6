package khanin.dmitrii.hashMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Logic {
    private static String[] keywords = new String[] {
            "@Override", "System", "abstract", "assert", "break", "case", "catch",
            "class", "const", "continue", "default", "do", "double", "else", "enum", "extends", "false", "final",
            "finally", "for", "goto", "if", "implements", "import", "instanceof", "interface",
            "native", "new", "null", "package", "private", "protected", "public", "return",
            "static", "strictfp", "super", "switch", "synchronized", "this", "throw", "throws", "transient", "true",
            "try", "void", "volatile", "while"
    };

    private static String[] dataTypes = new String[] {
            "boolean", "byte", "char", "float", "int", "long", "short"
    };

    public static String obfuscation(String origin) {
        StringBuilder result = new StringBuilder();
        MyHashMap<String, String> createdClasses = new MyHashMap<>();
        MyHashMap<String, String> createdMethods = new MyHashMap<>();
        MyHashMap<String, String> variables = new MyHashMap<>();
        MyHashMap<String, String> importedClasses = new MyHashMap<>();
        MyHashMap<String, String> variableTypes = new MyHashMap<>();
        MyHashMap<String, String> methodTypes = new MyHashMap<>();
        int obfuscatingNum = 1;
        {
            StringBuilder sb = new StringBuilder();
            {
                // Первый проход, убираем все строки
                boolean isSingleQuotationsOpened = false;
                boolean isDoubleQuotationsOpened = false;
                char[] charArr = origin.toCharArray();
                for (int i = 0; i < charArr.length; i++) {
                    char c = charArr[i];
                    if (c == '\"' && (!(charArr[i - 1] == '\\') || charArr[i - 2] == '\\')) {
                        isDoubleQuotationsOpened = !isDoubleQuotationsOpened;
                    } else if (c == '\'' && (!(charArr[i - 1] == '\\') || charArr[i - 2] == '\\')) {
                        isSingleQuotationsOpened = !isSingleQuotationsOpened;
                    } else if (!(isSingleQuotationsOpened || isDoubleQuotationsOpened)) {
                        sb.append(c);
                    }
                }
            }

            // Второй проход, разделяем код на отдельные слова
            String[] words = sb.toString().split("[ \n<]");
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

            // Третий проход, ищем классы и функции объявленные в этом файле, а также импортированные классы
            // Пробегаемся по всем словам, ищем объявленные классы и методы, а также импортированные классы
            for (int i = 0; i < words.length; i++) {
                String word = words[i];
                if (word.equals("class")) {
                    // нашли объвленный в этом файле класс
                    String createdClass = words[++i];
                    if (!createdClasses.containsKey(createdClass)) {
                        createdClasses.put(createdClass, "v" + obfuscatingNum++);
                    }
                } else if (word.equals("import")) {
                    // Нашли импортированный класс, не обфусцируем его
                    String imported = words[i + 1].substring(0, words[i + 1].indexOf(";"));
                    String[] importedPackage = imported.split("\\.");
                    imported = importedPackage[importedPackage.length - 1];
                    if (!imported.equals("*")) importedClasses.put(imported, imported);
                }
            }

            for (int i = 0; i < words.length; i++) {
                String word = words[i];
                if (word.equals("public") && i + 3 < words.length && words[i + 1].equals("static")
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
                    // Если это поле, то его заменим на четвертом проходе по коду
                    while (++i < words.length) {
                        String nextWord = words[i];
                        if (!((nextWord.equals("static") || nextWord.equals("final") || nextWord.equals("synchronized")
                                || nextWord.equals("abstract") || nextWord.equals("volatile")
                                || nextWord.equals("transient")))) {
                            if (nextWord.equals("class")) {
                                // это класс, прерываем вайл
                                i--;
                                break;
                            }
                            String method = i + 1 < words.length ? words[i + 1] : "";
                            String afterMethod = i + 2 < words.length ? words[i + 2] : "";
                            if (method.contains("(")) {
                                // нашли объявленный метод, обфусцируем его
                                method = method.substring(0, method.indexOf("("));
                                if (!createdMethods.containsKey(method)) {
                                    createdMethods.put(method, "v" + obfuscatingNum++);
                                }
                                methodTypes.put(method, nextWord);
                                if (!createdClasses.containsKey(nextWord) && !importedClasses.containsKey(nextWord)) {
                                    importedClasses.put(nextWord, nextWord);
                                }
                                break;
                            } else if (afterMethod.startsWith("(")) {
                                // нашли объявленный метод, обфусцируем его
                                if (!createdMethods.containsKey(method)) {
                                    createdMethods.put(method, "v" + obfuscatingNum++);
                                }
                                methodTypes.put(method, nextWord);
                                i++;
                                break;
                            }
                            // это объявленное поле, запишем его тип данных
                            variableTypes.put(method, nextWord);
                            variables.put(method, "v" + obfuscatingNum++);
                            i++;
                            break;
                        }
                    }
                }
            }
        }

        {
            // Четвертый проход, собираем результат, обфусцируем все что нужно
            StringBuilder wordBuilder = new StringBuilder();
            boolean isSingleQuotationsOpened = false;
            boolean isDoubleQuotationsOpened = false;
            boolean isImportOrPackage = false;
            boolean isFirstWord = true;
            boolean skip = false;
            String variableType = "";
            int triangleQuotationsCnt = 0;
            char[] chars = origin.toCharArray();
            for (int i = 0; i < chars.length; i++) {
                char c = chars[i];
                if (c == '\"' && (!(chars[i - 1] == '\\') || chars[i - 2] == '\\')) {
                    result.append(c);
                    isDoubleQuotationsOpened = !isDoubleQuotationsOpened;
                } else if (c == '\'' && (!(chars[i - 1] == '\\') || chars[i - 2] == '\\')) {
                    result.append(c);
                    isSingleQuotationsOpened = !isSingleQuotationsOpened;
                } else if (isSingleQuotationsOpened || isDoubleQuotationsOpened) {
                    // Кавычки открыты, просто добавляем символ в результат
                    result.append(c);
                } else if (isSuitableChar(c)) {
                    // Кавычки закрыты, данный символ является символом класса/метода/поля
                    wordBuilder.append(c);
                } else if (c == '\n') {
                    isFirstWord = true;
                    variableType = "";
                    result.append(c);
                }
                else {
                    // Кавычки закрыты, данный символ не является символом класса/метода/поля
                    String word = wordBuilder.toString();
                    wordBuilder.setLength(0);
                    if (word.isEmpty() || word.charAt(0) >= '0' && word.charAt(0) <= '9') {
                        result.append(word).append(c);
                        continue;
                    }
                    else if (word.equals("package") || word.equals("import")) {
                        // Это строка с импортом или пэкэйджом, не обфусцируем ее
                        isImportOrPackage = true;
                        result.append(word);
                    } else if (isImportOrPackage && c == ';') {
                        // Конец импорта/пэкэйджа
                        isImportOrPackage = false;
                        result.append(word);
                    } else if (isFirstWord && c != '.' && !variables.containsKey(word) && isDataType(word)) {
                        result.append(word);
                        variableType = word;
                        if (!createdClasses.containsKey(word) && !importedClasses.containsKey(word)) {
                            importedClasses.put(word, word);
                        }
                        if (c == '<') {
                            triangleQuotationsCnt++;
                        }
                    } else if (isKeyword(word) || isImportOrPackage) {
                        result.append(word);
                    } else if (c == '>' && chars[i - 1] != '<') {
                        result.append(word);
                        if (!createdClasses.containsKey(word) && !importedClasses.containsKey(word)) {
                            importedClasses.put(word, word);
                        }
                        triangleQuotationsCnt--;
                    } else if (createdClasses.containsKey(word)) {
                        // Это объявленный в этом файле класс, обфусцируем его
                        result.append(createdClasses.get(word));
                    } else if (createdMethods.containsKey(word)) {
                        result.append(createdMethods.get(word));
                    } else if (c == '(') {
                        // Это метод, не объявленный в этом файле, не обфусцируем его
                        result.append(word);
                    } else if (importedClasses.containsKey(word)) {
                        result.append(importedClasses.get(word));
                    } else if (variables.containsKey(word)) {
                        result.append(variables.get(word));
                        if (variableTypes.containsKey(word)) {
                            if (!createdClasses.containsKey(variableTypes.get(word))) {
                                skip = true;
                            }
                        } else skip = true;
                    } else if (triangleQuotationsCnt > 0) {
                        result.append(word);
                        if (!createdClasses.containsKey(word) && !importedClasses.containsKey(word)) {
                            importedClasses.put(word, word);
                        }
                    } else if (!variableType.isEmpty()) {
                        if (!createdClasses.containsKey(variableType)) skip = true;
                        variableTypes.put(word, variableType);
                        variableType = "";
                        if (variables.containsKey(word)) {
                            result.append(variables.get(word));
                        } else {
                            String value = "v" + obfuscatingNum++;
                            result.append(value);
                            variables.put(word, value);
                        }
                    } else if (c == ' ') {
                        skip = false;
                            // Проверим, метод это или поле
                            int j = 1;
                            while (i + j < chars.length && chars[i + j] == ' ') {
                                j++;
                            }
                            if (chars[i + j] == '(') {
                                // Это метод, который не объявлен в этом файле, не обфусцируем его
                                result.append(word);
                            } else {
                                // Это поле, обфусцируем его
                                if (variables.containsKey(word)) {
                                    result.append(variables.get(word));
                                    if (variableTypes.containsKey(word)) {
                                        if (!createdClasses.containsKey(word)) skip = true;
                                    } else skip = true;
                                } /*else {
                                    String value = "v" + obfuscatingNum++;
                                    result.append(value);
                                    variables.put(word, value);
                                }*/
                            }
                    } else if (skip) result.append(word);
                    else {

                        /*// Это поле, которое можно обфусцировать
                        if (variables.containsKey(word)) {
                            result.append(variables.get(word));
                        } else {
                            String value = "v" + obfuscatingNum++;
                            result.append(value);
                            variables.put(word, value);
                        }*/
                    }

                    if (c == '@') {
                        // Это может быть аннотацией
                        wordBuilder.append(c);
                        continue;
                    }
                    result.append(c);
                    isFirstWord = false;
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
        return Arrays.binarySearch(keywords, word) >= 0 || Arrays.binarySearch(dataTypes, word) >= 0;
    }

    private static boolean isSuitableChar(char c) {
        return c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c == '$' || c == '_' || c >= '0' && c <= '9';
    }

    private static boolean isDataType(String word) {
        return Arrays.binarySearch(keywords, word) < 0;
    }
}
