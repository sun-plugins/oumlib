package dev.oum.oumlib.math;

import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;

import java.util.Map;

public final class MathEval {

    private MathEval() {
    }

    @Contract(pure = true)
    public static double eval(@NonNull String expr) {
        return eval(expr, Map.of());
    }

    @Contract(pure = true)
    public static double eval(@NonNull String expr, @NonNull Map<String, Double> variables) {
        return new Parser(expr, variables).parse();
    }

    private static class Parser {
        private final String str;
        private final Map<String, Double> vars;
        private int pos = -1;
        private int ch;

        Parser(String str, Map<String, Double> vars) {
            this.str = str;
            this.vars = vars;
        }

        void nextChar() {
            ch = (++pos < str.length()) ? str.charAt(pos) : -1;
        }

        boolean eat(int charToEat) {
            while (ch == ' ') nextChar();
            if (ch == charToEat) {
                nextChar();
                return true;
            }
            return false;
        }

        double parse() {
            nextChar();
            double x = parseExpression();
            if (pos < str.length()) throw new IllegalArgumentException("Unexpected character: " + (char) ch);
            return x;
        }

        double parseExpression() {
            double x = parseTerm();
            for (; ; ) {
                if (eat('+')) x += parseTerm();
                else if (eat('-')) x -= parseTerm();
                else return x;
            }
        }

        double parseTerm() {
            double x = parseFactor();
            for (; ; ) {
                if (eat('*')) x *= parseFactor();
                else if (eat('/')) {
                    double divisor = parseFactor();
                    x = divisor == 0.0 ? 0.0 : x / divisor;
                } else if (eat('%')) {
                    double divisor = parseFactor();
                    x = divisor == 0.0 ? 0.0 : x % divisor;
                } else return x;
            }
        }

        double parseFactor() {
            if (eat('+')) return parseFactor();
            if (eat('-')) return -parseFactor();

            double x;
            int startPos = this.pos;
            if (eat('(')) {
                x = parseExpression();
                eat(')');
            } else if ((ch >= '0' && ch <= '9') || ch == '.') {
                while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                x = Double.parseDouble(str.substring(startPos, this.pos));
            } else if (ch >= 'a' && ch <= 'z' || ch >= 'A' && ch <= 'Z' || ch == '_') {
                while (ch >= 'a' && ch <= 'z' || ch >= 'A' && ch <= 'Z' || ch >= '0' && ch <= '9' || ch == '_')
                    nextChar();
                String name = str.substring(startPos, this.pos);
                if (eat('(')) {
                    double arg = parseExpression();
                    if (name.equals("sin")) x = Math.sin(arg);
                    else if (name.equals("cos")) x = Math.cos(arg);
                    else if (name.equals("tan")) x = Math.tan(arg);
                    else if (name.equals("sqrt")) x = Math.sqrt(arg);
                    else if (name.equals("abs")) x = Math.abs(arg);
                    else if (name.equals("round")) x = Math.round(arg);
                    else if (name.equals("floor")) x = Math.floor(arg);
                    else if (name.equals("ceil")) x = Math.ceil(arg);
                    else if (name.equals("min") || name.equals("max") || name.equals("clamp")) {
                        if (eat(',')) {
                            double arg2 = parseExpression();
                            if (name.equals("min")) x = Math.min(arg, arg2);
                            else if (name.equals("max")) x = Math.max(arg, arg2);
                            else {
                                eat(',');
                                double arg3 = parseExpression();
                                x = Math.max(arg2, Math.min(arg3, arg));
                            }
                        } else {
                            throw new IllegalArgumentException("Function " + name + " expects multiple arguments");
                        }
                    } else {
                        throw new IllegalArgumentException("Unknown function: " + name);
                    }
                    eat(')');
                } else {
                    x = vars.getOrDefault(name, 0.0);
                }
            } else {
                throw new IllegalArgumentException("Unexpected character: " + (char) ch);
            }

            if (eat('^')) x = Math.pow(x, parseFactor());

            return x;
        }
    }
}
