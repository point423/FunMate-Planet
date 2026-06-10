package com.zjgsu.pjt.backend.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;
import java.util.Arrays;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${upload.dir:uploads/}")
    private String uploadDir;

    @Value("${cors.allowed-origins:*}")
    private String allowedOrigins;

    @Autowired
    private AuthInterceptor authInterceptor;

     @Override
     public void addInterceptors(InterceptorRegistry registry) {
         registry.addInterceptor(authInterceptor)
                 .addPathPatterns("/api/**")
                 .excludePathPatterns("/api/auth/login", "/api/auth/register", "/api/ai/test"); // 放行测试接口
     }
//    @Override
//    public void addInterceptors(InterceptorRegistry registry) {
//        registry.addInterceptor(authInterceptor)
//                .addPathPatterns("/api/**")
//                .excludePathPatterns(
//                        "/api/auth/login",
//                        "/api/auth/register",
//                        "/api/ai/test",
//                        "/api/diaries/**"  // 放行日记接口以便测试上传，或者确保前端传了正确的 Token
//                );
//    }


    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String[] origins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toArray(String[]::new);
        if (origins.length == 0) {
            origins = new String[]{"*"};
        }

        registry.addMapping("/api/**")
                .allowedOriginPatterns(origins)
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("Authorization")
                .allowCredentials(false)
                .maxAge(3600);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String path = new File(uploadDir).getAbsolutePath();
        registry.addResourceHandler("/static/**")
                .addResourceLocations("file:" + path + File.separator);
    }
}
