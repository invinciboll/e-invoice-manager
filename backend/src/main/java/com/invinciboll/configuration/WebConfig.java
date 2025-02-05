package com.invinciboll.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Autowired
    private AppConfig appConfig;

    private static final Logger logger = LoggerFactory.getLogger(WebConfig.class);

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String frontendPort = appConfig.getFrontendPort();
        String frontendHost = appConfig.getFrontendHost();
        String frontendURL = "https://" + frontendHost;

        logger.info("Frontend URL configured for CORS: {}", frontendURL);

        registry.addMapping("/**")
                .allowedOrigins(frontendURL)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String dirName = appConfig.getTempfilesDir();

        // Map "/tempfiles/**" to the directory on the filesystem
        registry.addResourceHandler("/" + dirName + "/**")
                .addResourceLocations("file:" + dirName + "/")
                .setCachePeriod(3600) // Optional: Set caching
                .resourceChain(true); // Enable resource chaining

        String outDirName = appConfig.getOutputDir();
        registry.addResourceHandler("/" + outDirName + "/**")
                .addResourceLocations("file:" + outDirName + "/")
                .setCachePeriod(3600) // Optional: Set caching
                .resourceChain(true); // Enable resource chaining


    }
}
