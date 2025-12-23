package com.longoj.top.infrastructure.utils;

import cn.hutool.dfa.FoundWord;
import cn.hutool.dfa.WordTree;

public class KeyWordDetectUtil {

    private static WordTree WORD_TREE;
    static {
        String[] privacyAccessOps = {
                "/etc/passwd",       // Linux用户账户
                "/etc/shadow",       // Linux密码哈希
                "/etc/ssh/sshd_config",
                "C:\\Windows\\System32\\config\\SAM",  // Windows密码文件
                "~/.ssh/id_rsa",     // SSH私钥
                "web.config",        // Web服务器配置
                "application.properties",
                "application.properties",              // 环境变量文件
                "keyStore.jks",      // Java密钥库
                "wp-config.php"      // WordPress配置
        };
        String[] destructiveOps = {
                "rm -rf /",          // Linux删除根目录
                "del /F /S /Q C:\\*",
                "format C: /y",
                "dd if=/dev/zero of=/dev/sda",
                "chmod 777 / -R",    // 权限滥用
                "iptables --flush",  // 清除防火墙规则
                "echo 1 > /proc/sys/kernel/sysrq",  // 启用SysRq
                "mv /home/user /dev/null",
                "useradd attacker",  // 创建后门账户
                "netcat -l -p 4444 -e /bin/bash"  // 反弹shell
        };

        WORD_TREE = new WordTree();
        WORD_TREE.addWords("Files", "exec");
        WORD_TREE.addWords(privacyAccessOps);
        WORD_TREE.addWords(destructiveOps);
    }

    /**
     * 检查代码文件是否包含禁止词
     */
    public static boolean checkCodeFile(String code) {
        // 检查代码内部字符
        FoundWord foundWord = WORD_TREE.matchWord(code);
        if (foundWord != null) {
            System.out.println("包含禁止词：" + foundWord.getFoundWord());
            return true;
        }
        return false;
    }

}
