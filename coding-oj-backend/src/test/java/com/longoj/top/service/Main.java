package com.longoj.top.service;

import java.io.File;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        // 这里可以添加测试代码来验证 RabbitMQ 的连接和功能
        System.out.println("Hello, World!");
        String s = new Scanner(System.in).nextLine();// 等待用户输入以保持控制台打开
        System.out.println(s);

        File file = new File("./myjava.policy");
        System.out.println(file.getName() + " exists: " + file.exists());
    }
}
