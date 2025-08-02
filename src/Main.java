import java.math.BigInteger;
import java.util.*;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        run(scanner);
        scanner.close();
    }

    public static void run(Scanner scanner) {
        Map<String, BigInteger> map = new HashMap<>();
        while (true) {
            String input = scanner.nextLine();

            if (input.startsWith("/")) {
                switch (input) {
                    case "/exit" -> {
                        System.out.println("Bye!");
                        return;
                    }
                    case "/help" -> printHelp();
                    default -> System.out.println("Unknown command");
                }
            } else if (!input.isEmpty()) {
                if (input.chars().filter(ch -> ch == '=').count() > 1) {
                    System.out.println("Invalid assignment");
                    continue;
                }

                if (input.contains("=")) {
                    String[] parts = input.split("=", 2);
                    if (parts.length != 2) {
                        System.out.println("Invalid assignment");
                        continue;
                    }
                    String left = parts[0].trim();
                    String right = parts[1].trim();

                    if (!left.matches("[a-zA-Z]+")) {
                        System.out.println("Invalid identifier");
                        continue;
                    }

                    if (right.matches("-?\\d+")) {
                        map.put(left, new BigInteger(right));
                    } else if (right.matches("[a-zA-Z]+")) {
                        if (map.containsKey(right)) {
                            map.put(left, map.get(right));
                        } else {
                            System.out.println("Unknown variable");
                        }
                    } else {
                        System.out.println("Invalid assignment");
                    }
                    continue;
                }

                try {
                    if (input.matches("-?\\d+")) {
                        System.out.println(Integer.parseInt(input));
                    } else if (input.matches("[a-zA-Z]+")) {
                        if (map.containsKey(input)) {
                            System.out.println(map.get(input));
                        } else {
                            System.out.println("Unknown variable");
                        }
                    } else {
                        List<String> postfix = infixToPostfix(input, map);
                        BigInteger result = evaluatePostfix(postfix, map);
                        System.out.println(result);
                    }
                } catch (IllegalArgumentException e) {
                    System.out.println(e.getMessage());
                }

            }
        }
    }

    public static void printHelp() {
        System.out.println("""
                This is Smart Calculator
                The program calculates the sum of numbers, even the declared ones.
                Example #1: 2 * (4 / 5) + 6 ^ 2
                Example #2: 9 +++ 10 -- 8
                Example #3: a = 3
                            a + a
                
                Commands:
                /help, /exit""");
    }

    public static List<String> infixToPostfix(String input, Map<String, BigInteger> variables) {
        List<String> output = new ArrayList<>();
        Deque<String> stack = new ArrayDeque<>();
        List<String> tokens = tokenize(input);

        for (String token : tokens) {
            if (token.matches("[a-zA-Z]+")) {
                if (!variables.containsKey(token)) {
                    throw new IllegalArgumentException("Unknown variable: " + token);
                }
                output.add(token);
            } else if (token.matches("\\d+")) {
                output.add(token);
            } else if (token.equals("(")) {
                stack.push(token);
            } else if (token.equals(")")) {
                while (!stack.isEmpty() && !stack.peek().equals("(")) {
                    output.add(stack.pop());
                }
                if (stack.isEmpty() || !stack.pop().equals("(")) {
                    throw new IllegalArgumentException("Invalid expression");
                }
            } else if ("+-*/^".contains(token)) {
                while (!stack.isEmpty() && !"(".equals(stack.peek()) &&
                        (getPrecedence(token) < getPrecedence(stack.peek()) ||
                                (getPrecedence(token) == getPrecedence(stack.peek()) &&
                                        !isRightAssociative(token)))) {
                    output.add(stack.pop());
                }
                stack.push(token);
            } else {
                throw new IllegalArgumentException("Invalid token: " + token);
            }
        }

        while (!stack.isEmpty()) {
            String op = stack.pop();
            if (op.equals("(") || op.equals(")")) {
                throw new IllegalArgumentException("Invalid expression");
            }
            output.add(op);
        }

        return output;
    }

    public static List<String> tokenize(String input) {
        input = input.replaceAll("\\s+", "");

        List<String> tokens = new ArrayList<>();
        StringBuilder current = new StringBuilder();

        for (int i = 0; i < input.length(); ) {
            char ch = input.charAt(i);

            if (Character.isLetterOrDigit(ch)) {
                current.setLength(0);
                while (i < input.length() && Character.isLetterOrDigit(input.charAt(i))) {
                    current.append(input.charAt(i++));
                }
                tokens.add(current.toString());
            } else if (ch == '+' || ch == '-') {
                current.setLength(0);
                current.append(ch);
                i++;
                while (i < input.length() && (input.charAt(i) == '+' || input.charAt(i) == '-')) {
                    current.append(input.charAt(i++));
                }

                String sequence = current.toString();
                if (sequence.contains("-")) {
                    long minusCount = sequence.chars().filter(c -> c == '-').count();
                    tokens.add((minusCount % 2 == 0) ? "+" : "-");
                } else {
                    tokens.add("+");
                }
            } else if (ch == '*' || ch == '/' || ch == '^' || ch == '(' || ch == ')') {
                tokens.add(String.valueOf(ch));
                i++;
            } else {
                throw new IllegalArgumentException("Invalid character: " + ch);
            }
        }

        return tokens;
    }

    public static int getPrecedence(String operator) {
        return switch (operator) {
            case "^" -> 3;
            case "*", "/" -> 2;
            case "+", "-" -> 1;
            default -> 0;
        };
    }

    public static boolean isRightAssociative(String operator) {
        return operator.equals("^");
    }

    public static BigInteger evaluatePostfix(List<String> postfix, Map<String, BigInteger> variables) {
        Deque<BigInteger> stack = new ArrayDeque<>();

        for (String token : postfix) {
            if (token.matches("\\d+")) {
                stack.push(new BigInteger(token));
            } else if (token.matches("[a-zA-Z]+")) {
                if (!variables.containsKey(token)) {
                    throw new IllegalArgumentException("Unknown variable: " + token);
                }
                stack.push(variables.get(token));
            } else if ("+-*/^".contains(token)) {
                if (stack.size() < 2) {
                    throw new IllegalArgumentException("Invalid expression");
                }
                BigInteger b = stack.pop();
                BigInteger a = stack.pop();
                BigInteger result = switch (token) {
                    case "+" -> a.add(b);
                    case "-" -> a.subtract(b);
                    case "*" -> a.multiply(b);
                    case "/" -> a.divide(b);
                    case "^" -> a.pow(b.intValue());
                    default -> throw new IllegalArgumentException("Unknown operator: " + token);
                };
                stack.push(result);
            } else {
                throw new IllegalArgumentException("Invalid token in postfix: " + token);
            }
        }

        if (stack.size() != 1) {
            throw new IllegalArgumentException("Invalid expression");
        }

        return stack.pop();
    }

}
