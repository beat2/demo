package com.example.demo;

import com.example.demo.config.WebserviceServerConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.ws.transport.http.MessageDispatcherServlet;

@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

//	@Bean
//	public ServletRegistrationBean<MessageDispatcherServlet> createWebServiceMessageDispatcherServlet() {
//		ServletRegistrationBean<MessageDispatcherServlet> servletRegistration = new ServletRegistrationBean<>();
//
//		MessageDispatcherServlet servlet = new MessageDispatcherServlet();
//
//		AnnotationConfigWebApplicationContext springWsLocalConfig = new AnnotationConfigWebApplicationContext();
//		springWsLocalConfig.register(WebserviceServerConfig.class);
//
//		servlet.setApplicationContext(springWsLocalConfig);
//
//		servlet.setTransformWsdlLocations(true);
//		servletRegistration.setName("MessageDispatcherServlet");
//		servletRegistration.setServlet(servlet);
//		servletRegistration.addUrlMappings("/ws");
//		servletRegistration.setLoadOnStartup(1);
//
//		return servletRegistration;
//	}
}
