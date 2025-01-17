package com.invinciboll.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:5173") // Frontend URL
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String dirName = AppConfig.getInstance().getProperty("tempfiles.dir", "tempfiles");

        // Map "/tempfiles/**" to the directory on the filesystem
        registry.addResourceHandler("/" + dirName + "/**")
                .addResourceLocations("file:" + dirName + "/")
                .setCachePeriod(3600) // Optional: Set caching
                .resourceChain(true); // Enable resource chaining
    }
}
