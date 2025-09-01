package com.ryanshiun.seniorscare.confing;


import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // 靜態資源映射設定（圖片）
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String uploadPath = new File("uploads/images").getAbsolutePath() + File.separator;
        System.out.println("實際映射路徑: " + uploadPath);

        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:" + uploadPath);
    }

}