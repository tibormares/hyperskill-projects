import java.io.*;
import java.util.*;

public class Main {

    private static final Set<String> validArgs = Set.of("-sortingType", "-dataType", "-inputFile", "-outputFile");

    public static void main(final String[] args) {
        Scanner scanner = new Scanner(System.in);
        run(scanner, args);
        scanner.close();
    }

    public static String getArgumentValue(List<String> args, String key) {
        int index = args.indexOf(key);
        if (index != -1 && index + 1 < args.size() && !args.get(index + 1).startsWith("-")) {
            return args.get(index + 1);
        }
        return null;
    }

    public static Map<String, String> parseArguments(List<String> args) {
        Map<String, String> arguments = new HashMap<>();

        for (String argument : args) {
            if (argument.startsWith("-") && !isValidArgument(argument)) {
                System.out.println("\"" + argument + "\" is not a valid parameter. It will be skipped.");
            }
        }

        String outputFile = getArgumentValue(args, "-outputFile");
        if (outputFile != null) {
            arguments.put("-outputFile", outputFile);
        }

        String inputFile = getArgumentValue(args, "-inputFile");
        if (inputFile != null) {
            arguments.put("-inputFile", inputFile);
        }

        String dataType = getArgumentValue(args, "-dataType");
        if (dataType != null && Set.of("long", "integer", "word", "line").contains(dataType)) {
            arguments.put("-dataType", dataType);
        } else {
            System.out.println("No data type defined!");
        }

        String sortingType = getArgumentValue(args, "-sortingType");
        if ("byCount".equals(sortingType)) {
            arguments.put("-sortingType", sortingType);
        } else {
            arguments.put("-sortingType", "natural");
        }

        return arguments;
    }

    public static <T> void printArrayToFile(String type, ArrayList<T> array, String outputFile) {
        if (!outputFile.toLowerCase().endsWith(".txt")) {
            outputFile += ".txt";
        }
        File file = new File(outputFile);
        try (PrintWriter writer = new PrintWriter(file)) {
            writer.println("Total " + type + ": " + array.size() + ".");
            writer.print("Sorted data: ");
            if (type.equals("lines")) {
                writer.println("");
                for (T t : array) {
                    writer.println(t);
                }
            } else {
                for (int i = 0; i < array.size(); i++) {
                    if (i != array.size() - 1) {
                        writer.print(array.get(i) + " ");
                    } else {
                        writer.print(array.get(i));
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error with output file: " + e.getMessage());
        }
    }

    public static <T extends Comparable<T>> void printArrayByCountToFile(Map<T, Integer> map, int count, String label, String outputFile) {
        if (!outputFile.toLowerCase().endsWith(".txt")) {
            outputFile += ".txt";
        }
        File file = new File(outputFile);
        try (PrintWriter writer = new PrintWriter(file)) {
            writer.println("Total " + label + ": " + count + ".");
            ArrayList<Map.Entry<T, Integer>> list = new ArrayList<>(map.entrySet());
            list.sort(Comparator.comparing(Map.Entry<T, Integer>::getValue)
                    .thenComparing(Map.Entry::getKey));
            for (Map.Entry<T, Integer> entry : list) {
                double percentage = (100.0 / count) * entry.getValue();
                writer.println(entry.getKey() + ": " + entry.getValue() + " time(s), " + Math.round(percentage) + "%");
            }
        } catch (IOException e) {
            System.out.println("Error with output file: " + e.getMessage());
        }
    }

    public static void run(Scanner scanner, String[] args) {
        List<String> argumentsList = List.of(args);
        Map<String, String> arguments = parseArguments(argumentsList);
        if (arguments.get("-dataType") != null) {
            callMethod(scanner,
                    arguments.get("-dataType"),
                    arguments.get("-sortingType"),
                    arguments.get("-outputFile"),
                    arguments.get("-inputFile")
            );
        }
    }

    public static boolean isValidArgument(String argument) {
        return validArgs.contains(argument);
    }

    public static void callMethod(Scanner scanner, String dataType, String sortingType, String outputFile, String inputFile) {
        try {
            switch (dataType) {
                case "integer", "long": {
                    sortNumbers(scanner, sortingType, outputFile, inputFile);
                    break;
                }
                case "word": {
                    sortWords(scanner, sortingType, outputFile, inputFile);
                    break;
                }
                case "line": {
                    sortLines(scanner, sortingType, outputFile, inputFile);
                    break;
                }
            }
        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
        }
    }

    public static void sortNumbers(Scanner scanner, String sortingType, String outputFile, String inputFile) {
        ArrayList<Long> array = new ArrayList<>();
        if (inputFile != null) {
            String line;
            long number;
            try {
                BufferedReader bufferReader = new BufferedReader(new FileReader(inputFile));
                while ((line = bufferReader.readLine()) != null) {
                    String[] parts = line.split(" ");
                    for (String part : parts) {
                        try {
                            number = Long.parseLong(part);
                            array.add(number);
                        } catch (Exception e) {
                            System.out.println("An error occurred: " + e.getMessage());
                        }
                    }
                }
                array.sort(Long::compareTo);
            } catch (IOException e) {
                System.out.println("Error with input file: " + e.getMessage());
            }
            if (outputFile != null) {
                printArrayToFile("numbers", array, outputFile);
            } else {
                printArray("numbers", array);
            }
        } else {
            if (sortingType == null || sortingType.equals("natural")) {
                while (scanner.hasNext()) {
                    try {
                        long number = scanner.nextLong();
                        array.add(number);
                    } catch (InputMismatchException e) {
                        String invalidToken = scanner.next();
                        System.out.println("\"" + invalidToken + "\" is not a long. It will be skipped.");
                    }
                }
                array.sort(Long::compareTo);
                if (outputFile != null) {
                    printArrayToFile("numbers", array, outputFile);
                } else {
                    printArray("numbers", array);
                }
            } else if (sortingType.equals("byCount")) {
                Map<Long, Integer> map = new HashMap<>();
                int count = 0;
                while (scanner.hasNext()) {
                    try {
                        long number = scanner.nextLong();
                        if (map.containsKey(number)) {
                            map.put(number, map.get(number) + 1);
                        } else {
                            map.put(number, 1);
                        }
                        count++;
                    } catch (InputMismatchException e) {
                        String invalidToken = scanner.next();
                        System.out.println("\"" + invalidToken + "\" is not a long. It will be skipped.");
                    }
                }
                if (outputFile != null) {
                    printArrayByCountToFile(map, count, "numbers", outputFile);
                } else {
                    printArrayByCount(map, count, "numbers");
                }
            }
        }
    }

    public static void sortWords(Scanner scanner, String sortingType, String outputFile, String inputFile) {
        ArrayList<String> array = new ArrayList<>();
        if (inputFile != null) {
            String line;
            try {
                BufferedReader bufferReader = new BufferedReader(new FileReader(inputFile));
                while ((line = bufferReader.readLine()) != null) {
                    String[] parts = line.split(" ");
                    array.addAll(Arrays.asList(parts));
                }
                array.sort(String::compareTo);
            } catch (IOException e) {
                System.out.println("Error with input file: " + e.getMessage());
            }
            if (outputFile != null) {
                printArrayToFile("words", array, outputFile);
            } else {
                printArray("words", array);
            }
        } else {
            if (sortingType.equals("natural")) {
                while (scanner.hasNext()) {
                    array.add(scanner.next());
                }
                array.sort(String::compareTo);
                if (outputFile != null) {
                    printArrayToFile("words", array, outputFile);
                } else {
                    printArray("words", array);
                }
            } else if (sortingType.equals("byCount")) {
                Map<String, Integer> map = new HashMap<>();
                int count = 0;
                while (scanner.hasNext()) {
                    String word = scanner.next();
                    map.put(word, map.getOrDefault(word, 0) + 1);
                    count++;
                }
                if (outputFile != null) {
                    printArrayByCountToFile(map, count, "words", outputFile);
                } else {
                    printArrayByCount(map, count, "words");
                }
            }
        }
    }

    public static void sortLines(Scanner scanner, String sortingType, String outputFile, String inputFile) {
        ArrayList<String> array = new ArrayList<>();
        if (inputFile != null) {
            String line;
            try {
                BufferedReader bufferReader = new BufferedReader(new FileReader(inputFile));
                while ((line = bufferReader.readLine()) != null) {
                    array.add(line);
                }
                array.sort(String::compareTo);
            } catch (IOException e) {
                System.out.println("Error with input file: " + e.getMessage());
            }
            if (outputFile != null) {
                printArrayToFile("lines", array, outputFile);
            } else {
                printArray("lines", array);
            }
        } else {
            if (sortingType.equals("natural")) {
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    array.add(line);
                }
                array.sort(String::compareTo);
                if (outputFile != null) {
                    printArrayToFile("lines", array, outputFile);
                } else {
                    printArray("lines", array);
                }
            } else if (sortingType.equals("byCount")) {
                Map<String, Integer> map = new HashMap<>();
                int count = 0;
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    map.put(line, map.getOrDefault(line, 0) + 1);
                    count++;
                }
                if (outputFile != null) {
                    printArrayByCountToFile(map, count, "lines", outputFile);
                } else {
                    printArrayByCount(map, count, "lines");
                }
            }
        }
    }

    public static <T extends Comparable<T>> void printArrayByCount(Map<T, Integer> map, int count, String label) {
        System.out.println("Total " + label + ": " + count + ".");
        ArrayList<Map.Entry<T, Integer>> list = new ArrayList<>(map.entrySet());
        list.sort(Comparator.comparing(Map.Entry<T, Integer>::getValue)
                .thenComparing(Map.Entry::getKey));
        for (Map.Entry<T, Integer> entry : list) {
            double percentage = (100.0 / count) * entry.getValue();
            System.out.println(entry.getKey() + ": " + entry.getValue() + " time(s), " + Math.round(percentage) + "%");
        }
    }

    public static <T> void printArray(String type, ArrayList<T> array) {
        System.out.println("Total " + type + ": " + array.size() + ".");
        System.out.print("Sorted data: ");
        if (type.equals("lines")) {
            System.out.println();
            for (T t : array) {
                System.out.println(t);
            }
        } else {
            for (int i = 0; i < array.size(); i++) {
                if (i != array.size() - 1) {
                    System.out.print(array.get(i) + " ");
                } else {
                    System.out.print(array.get(i));
                }
            }
        }
    }

}
