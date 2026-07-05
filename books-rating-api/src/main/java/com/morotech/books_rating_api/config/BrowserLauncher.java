package com.morotech.books_rating_api.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.net.URI;

/**
 * Opens the Swagger UI in the default browser once the app is up, so running the
 * project is a single step.
 */
@Component
public class BrowserLauncher {

    private static final Logger log = LoggerFactory.getLogger(BrowserLauncher.class);

    private final boolean enabled;
    private final String swaggerPath;

    public BrowserLauncher(@Value("${app.open-browser:true}") boolean enabled,
                           @Value("${app.swagger-path:/swagger-ui.html}") String swaggerPath) {
        this.enabled = enabled;
        this.swaggerPath = swaggerPath;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onReady(ApplicationReadyEvent event) {
        Environment env = event.getApplicationContext().getEnvironment();
        String port = env.getProperty("local.server.port", env.getProperty("server.port", "8080"));
        String contextPath = env.getProperty("server.servlet.context-path", "");
        String url = "http://localhost:" + port + contextPath + swaggerPath;

        log.info("Swagger UI available at {}", url);

        if (!enabled || GraphicsEnvironment.isHeadless()) {
            return;
        }
        openBrowser(url);
    }

    private void openBrowser(String url) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(URI.create(url));
                return;
            }
        } catch (Exception ignored) {
        }
        try {
            String os = System.getProperty("os.name", "").toLowerCase();
            ProcessBuilder pb;
            if (os.contains("win")) {
                pb = new ProcessBuilder("rundll32", "url.dll,FileProtocolHandler", url);
            } else if (os.contains("mac")) {
                pb = new ProcessBuilder("open", url);
            } else {
                pb = new ProcessBuilder("xdg-open", url);
            }
            pb.start();
        } catch (Exception e) {
            log.info("Could not auto-open a browser. Open the Swagger UI manually: {}", url);
        }
    }
}