package com.example.plugin

import com.android.builder.model.SigningConfig
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class JiaGuTask extends DefaultTask {
    LDJiaGu ldJiaGu
    SigningConfig signingConfig
    File apk

    //配置分组
    JiaGuTask() {
        group = "LDPlugin"
    }
/**
 * 使用者双击执行时，调用的方法，只关心注解
 */
    @TaskAction
    def run() {
        //调用命令行工具
        //登录
        project.exec {
            it.commandLine("java", "-jar", ldJiaGu.jiaGuToolPath, "-login", ldJiaGu.accountName, ldJiaGu.accountPwd)
        }

        //导入签名
        if (signingConfig) {
            project.exec {
                it.commandLine("java", "-jar", ldJiaGu.jiaGuToolPath, "-importsign", signingConfig.storeFile.absolutePath,
                        signingConfig.storePassword, signingConfig.keyAlias, signingConfig.keyPassword)
            }
        }

        //加固命令
        project.exec {
            it.commandLine("java", "-jar", ldJiaGu.jiaGuToolPath, "-jiagu", apk.absolutePath,
                    apk.parent, "-autosign")
        }
    }
}