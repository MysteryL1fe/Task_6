package khanin.dmitrii.hashMap;

import java.util.Arrays;

public class Logic {
    private static String[] keywords = new String[] {
            "Override", "String", "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class",
            "const", "continue", "default", "do", "double", "else", "enum", "extends", "false", "final", "finally",
            "float", "for", "goto", "if", "implements", "import", "instanceof", "int", "interface", "long", "native",
            "new", "package", "private", "protected", "public", "return", "short", "static", "strictfp", "super",
            "switch", "synchronized", "this", "throw", "throws", "transient", "true", "try", "void", "volatile", "while"
    };

    public static String obfuscation(String origin) {
        MyHashMap<String, String> createdClassesAndMethods = new MyHashMap<>();
        int obfuscatingNum = 1;
        // Первый проход, ищем классы и функции объявленные в этом файле, а также импортированные классы
        String[] words = origin.split("[ \n<]");
        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            if (word.equals("class")) { // нашли объвленный в этом файле класс
                String createdClass = words[++i];
                if (!createdClassesAndMethods.containsKey(createdClass)) {
                    createdClassesAndMethods.put(createdClass, "v" + obfuscatingNum++);
                }
            } else if (word.equals("public") || word.equals("private") || word.equals("protected")
                    || word.equals("abstract")) {
                int j = 1;
                while (i + j < words.length) {
                    String nextWord = words[i + j];
                    if (!((nextWord.equals("static") || nextWord.equals("final") || nextWord.equals("synchronized")
                            || nextWord.equals("abstract") || nextWord.equals("volatile")
                            || nextWord.equals("transient")))) {
                        if (nextWord.equals("class")) break;
                        String method = i + j + 1 < words.length ? words[i + j + 1] : "";
                        String afterMethod = i + j + 2 < words.length ? words[i + j + 2] : "";
                        if (method.contains("(")) {
                            method = method.substring(0, method.indexOf("("));
                            if (method.equals("main")) {
                                i += j;
                                break;
                            }
                            if (!createdClassesAndMethods.containsKey(method)) {
                                createdClassesAndMethods.put(method, "v" + obfuscatingNum++);
                            }
                            i++;
                        } else if (afterMethod.startsWith("(")) {
                            if (method.equals("main")) {
                                i += j;
                                break;
                            }
                            if (!createdClassesAndMethods.containsKey(method)) {
                                createdClassesAndMethods.put(method, "v" + obfuscatingNum++);
                            }
                            i++;
                        }
                        i += j - 1;
                        break;
                    }
                    j++;
                }
                /*String method;
                if (word.startsWith("(")) method = words[i - 1];
                else method = words[i].substring(0, words[i].indexOf("("));
                // мы нашли метод, надо проверить объявляется ли он в этом классе или только вызывается
                if (!method.contains(".") && !createdClassesAndMethods.containsKey(method)) {
                    createdClassesAndMethods.put(method, "v" + obfuscatingNum++);
                }*/
            }
        }
        words = null;

        // Второй проход, обфусцируем все что нужно
        StringBuilder result = new StringBuilder();
        StringBuilder wordBuilder = new StringBuilder();
        boolean isSingleQuotationsOpened = false;
        boolean isDoubleQuotationsOpened = false;
        for (char c : origin.toCharArray()) {
            //если открыты кавычки, то просто добавляем символ в результат
            if (c == '\"') {
                result.append(c);
                isDoubleQuotationsOpened = !isDoubleQuotationsOpened;
            } else if (c == '\'') {
                result.append(c);
                isSingleQuotationsOpened = !isSingleQuotationsOpened;
            } else if (isSingleQuotationsOpened || isDoubleQuotationsOpened) {
                result.append(c);
            } else if (isSuitableChar(c)) { // кавычки закрыты, данный символ является символом класса/метода/поля
                wordBuilder.append(c);
            } else { // кавычки закрыты, данный символ не является символом класса/метода/поля
                String word = wordBuilder.toString();
                wordBuilder.setLength(0);
                if (word.isEmpty() || word.charAt(0) >= '0' && word.charAt(0) <= '9') result.append(word);
                else if (createdClassesAndMethods.containsKey(word)) { // Это созданный класс/метод
                    result.append(createdClassesAndMethods.get(word));
                } else if (c == '(' && createdClassesAndMethods.containsKey(word)) {
                    result.append(createdClassesAndMethods.get(word));
                } else result.append(word);
                result.append(c);
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
