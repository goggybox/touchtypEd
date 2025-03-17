package com.example.touchtyped.server;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import com.sun.net.httpserver.HttpServer;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.DispatcherServlet;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * 排名服务器应用程序入口点
 */
@Configuration
@EnableWebMvc
@ComponentScan(basePackages = "com.example.touchtyped.server")
public class RankingServerApplication {
    
    @Bean
    public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter() {
        return new MappingJackson2HttpMessageConverter();
    }
    
    public static void main(String[] args) {
        try {
            // 创建Spring上下文
            AnnotationConfigApplicationContext context = 
                    new AnnotationConfigApplicationContext(RankingServerApplication.class);
            
            // 创建DispatcherServlet并设置Spring上下文
            DispatcherServlet dispatcherServlet = new DispatcherServlet();
            dispatcherServlet.setApplicationContext(context);
            
            // 创建HTTP服务器，监听8080端口
            HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
            
            // 添加上下文处理器，将所有请求转发给DispatcherServlet
            server.createContext("/api", httpExchange -> {
                // 将请求传递给Spring MVC处理
                // 这里需要自定义实现，将HttpExchange转为Spring的HttpServletRequest和HttpServletResponse
                // 简化起见，可以直接处理简单的REST API
                String response = "{\"message\":\"Hello from Ranking Server\"}";
                httpExchange.sendResponseHeaders(200, response.length());
                httpExchange.getResponseBody().write(response.getBytes());
                httpExchange.close();
            });
            
            // 启动服务器
            server.start();
            
            System.out.println("排名服务器已启动，监听端口: 8080");
            System.out.println("按Ctrl+C停止服务器");
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
} 