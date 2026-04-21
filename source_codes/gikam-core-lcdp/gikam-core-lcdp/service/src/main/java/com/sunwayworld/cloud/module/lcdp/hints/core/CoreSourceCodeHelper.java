package com.sunwayworld.cloud.module.lcdp.hints.core;

import java.util.Stack;

public class CoreSourceCodeHelper {

    /**
     * 压缩源码中括号占用多行的代码为一行
     *
     * @param code
     * @return
     */
    public static String compressParenthesesToSingleLine(String code) {
        String[] lineCodes = code.split("\n");
        StringBuilder formattedBuilder = new StringBuilder();

        String newLine = "";

        for (String line : lineCodes) {
            newLine += line;

            if (checkPairedBrackets(newLine)) {
                // 处理源码中参数前边跟着注释
                newLine = newLine.replaceAll(", //", ", ").replaceAll(",  //", ", ");
                formattedBuilder.append(newLine);
                formattedBuilder.append(System.lineSeparator());
                newLine = "";
            }

        }

        return formattedBuilder.toString();
    }

    // ------------------------------------------------------
    // 私有方法
    // ------------------------------------------------------
    private static boolean checkPairedBrackets(String code) {
        Stack<Character> stack = new Stack<>();

        for (char c : code.toCharArray()) {
            if (c == '(') {
                stack.push(c);
            } else if (c == ')') {
                if (stack.isEmpty()) {
                    // 右括号没有对应的左括号
                    return false;
                }
                stack.pop();
            }
        }

        // 如果栈为空，则表示所有括号都是成对的
        return stack.isEmpty();
    }


}
