package com.example.plugin

import com.android.build.gradle.AppExtension
import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.api.BaseVariantOutput
import com.android.builder.model.SigningConfig
import org.gradle.api.Plugin
import org.gradle.api.Project;

/**
 * 加固包
 * 注意：
 *   1.导包，gradleApi
 *   2.乐固/360加固保
 */
class LDJiaGuPlugin implements Plugin<Project> {


    @Override
    void apply(Project project) {
        //创建扩展
        LDJiaGu ldJiaGu = project.extensions.create("LDJiaGu", LDJiaGu.class);
        //回调
        project.afterEvaluate {
            AppExtension android = project.extensions.android
            //获取变体
            android.applicationVariants.all {
                ApplicationVariant variant ->
                    //获取对应变体{debug/release}的签名信息
                    SigningConfig signingConfig = variant.signingConfig
                    //apk文件获取
                    variant.outputs.all {
                        BaseVariantOutput output ->
                            File apk = output.outputFile
                            //创建加固任务
                            JiaGuTask jiaGuTask = project.tasks.create("LDJiaGu${variant.baseName.capitalize()}",JiaGuTask)
                            jiaGuTask.ldJiaGu =  ldJiaGu
                            jiaGuTask.signingConfig =  signingConfig
                            jiaGuTask.apk = apk

                    }

            }
        }

    }
}
