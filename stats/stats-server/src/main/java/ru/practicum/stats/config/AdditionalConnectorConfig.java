package ru.practicum.stats.config;

import org.apache.catalina.connector.Connector;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация дополнительного HTTP-коннектора Tomcat на порту 8080.
 *
 * Основной сервис статистики должен продолжать слушать порт 9090 (как в application.yml),
 * а дополнительный коннектор будет слушать 8080 для ручек, которые ожидает Postman.
 */
@Configuration
public class AdditionalConnectorConfig {

    @Bean
    public ServletWebServerFactory servletContainer() {
        TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory();
        // Явно задаём основной порт 9090 (соответствует server.port)
        tomcat.setPort(9090);
        // Добавляем дополнительный коннектор на 8080
        tomcat.addAdditionalTomcatConnectors(additionalHttpConnector());
        return tomcat;
    }

    private Connector additionalHttpConnector() {
        Connector connector = new Connector(TomcatServletWebServerFactory.DEFAULT_PROTOCOL);
        connector.setPort(8080);
        return connector;
    }
}
