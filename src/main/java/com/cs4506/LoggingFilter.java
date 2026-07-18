package com.cs4506;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Logging filter that captures every incoming request and its response,
 * then writes a log entry to a file.
 */
public class LoggingFilter implements Filter {

    private static final String LOG_FILE_PATH = "logs/webapp.log";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Ensure logs directory exists at the webapp root
        System.out.println("[LoggingFilter] Initialized – logging every request.");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        long startTime = System.currentTimeMillis();

        // Capture the request details
        String method = httpRequest.getMethod();
        String uri = httpRequest.getRequestURI();
        String queryString = httpRequest.getQueryString();
        String clientIp = httpRequest.getRemoteAddr();

        // Wrap the response so we can capture the body content
        ResponseWrapper wrappedResponse = new ResponseWrapper(httpResponse);

        try {
            // Pass the request/response through the filter chain (to the servlet)
            chain.doFilter(request, wrappedResponse);
        } finally {
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            int status = httpResponse.getStatus();
            String responseBody = wrappedResponse.getContent();

            // Build the log message
            String logEntry = String.format(
                "[%s] %s %s | IP: %s | Status: %d | Time: %d ms%n  Response: %s%n%n",
                new Date(), method, queryString != null ? uri + "?" + queryString : uri,
                clientIp, status, duration, responseBody
            );

            // Append to log file
            appendToLog(logEntry);

            // Write the captured body back to the real response
            byte[] content = wrappedResponse.getBytes();
            httpResponse.setContentLength(content.length);
            httpResponse.getOutputStream().write(content);
        }
    }

    private void appendToLog(String message) {
        try {
            // Use the Tomcat base directory for the log file
            String logDir = System.getProperty("catalina.base");
            if (logDir == null || logDir.isEmpty()) {
                logDir = ".";
            }
            String fullPath = logDir + "/logs/webapp.log";

            java.nio.file.Path path = java.nio.file.Paths.get(fullPath);
            java.nio.file.Files.createDirectories(path.getParent());

            java.nio.file.Files.writeString(path, message,
                java.nio.file.StandardOpenOption.CREATE,
                java.nio.file.StandardOpenOption.APPEND);
        } catch (Exception e) {
            // Fallback: print to System.out if file logging fails
            System.out.print(message);
        }
    }

    @Override
    public void destroy() {
        System.out.println("[LoggingFilter] Destroyed.");
    }

    /**
     * A wrapper around HttpServletResponse that captures the response body
     * so the filter can read it before sending it to the client.
     */
    private static class ResponseWrapper extends javax.servlet.http.HttpServletResponseWrapper {

        private final StringWriter stringWriter = new StringWriter();
        private final PrintWriter printWriter = new PrintWriter(stringWriter);
        private byte[] bodyBytes;

        public ResponseWrapper(HttpServletResponse response) {
            super(response);
        }

        @Override
        public PrintWriter getWriter() {
            return printWriter;
        }

        @Override
        public javax.servlet.ServletOutputStream getOutputStream() {
            return new javax.servlet.ServletOutputStream() {
                @Override
                public void write(int b) throws IOException {
                    printWriter.write(b);
                }

                @Override
                public boolean isReady() {
                    return true;
                }
            };
        }

        public String getContent() {
            printWriter.flush();
            return stringWriter.getBuffer().toString();
        }

        public byte[] getBytes() {
            if (bodyBytes == null) {
                printWriter.flush();
                bodyBytes = stringWriter.getBuffer().toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
            }
            return bodyBytes;
        }
    }
}
