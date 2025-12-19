package com.longoj.top.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class Test {

    public static void main(String[] args) throws IOException {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        // 提交一个任务

        String[] cmd = new String[]{
                "cmd.exe", "/c",
                "java -Xmx256m -Dfile.encoding=UTF-8 " +
                        "-Djava.security.manager -Djava.security.policy==myjava.policy " +
                        "-cp " + "target/test-classes" + " " +
                        "com.longoj.top.service.Main < D:\\code\\resume-project\\codingoj-backend\\src\\test\\java\\com\\longoj\\top\\service\\hey.txt"
        };

        Process process = Runtime.getRuntime().exec(cmd);
        // 3. 运行得到结果
        List<String> outputList = new ArrayList<>();
        try {
            process.waitFor(5, TimeUnit.SECONDS);

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.err.println("执行成功！");
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String outputLine;
                while ((outputLine = bufferedReader.readLine()) != null) {
                    outputList.add(outputLine);
                }
                bufferedReader.close();
            } else {
                System.err.println("执行失败！");
                // 分批获取进程的错误输出
                BufferedReader errorBufferedReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                StringBuilder errorOutputStringBuilder = new StringBuilder();

                // 逐行读取
                String errorCompileOutputLine;
                while ((errorCompileOutputLine = errorBufferedReader.readLine()) != null) {
                    errorOutputStringBuilder.append(errorCompileOutputLine);
                }
                // 以UTF-8编码打印错误信息
                String errorMessage = errorOutputStringBuilder.toString();
                byte[] bytes = errorMessage.getBytes(StandardCharsets.UTF_8);
                System.err.write(bytes, 0, bytes.length);
                System.err.println(); // 添加一个换行符
                errorBufferedReader.close();
            }

        } catch (InterruptedException e) {
            process.destroy();
            throw new RuntimeException(e);
        }
    }

}
