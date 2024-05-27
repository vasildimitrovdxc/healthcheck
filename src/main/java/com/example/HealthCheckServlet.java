package com.example;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.InputStreamReader;

@WebServlet("/*")
public class HealthCheckServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String configFilePath = "/opt/service/properties/healthcheck.conf";
        BufferedReader reader = new BufferedReader(new FileReader(configFilePath));
        String[] targetUrls = {};
        int timeout = 0;
        String line;
        boolean allTargetsOK = true;
        StringBuilder responseBody = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            String[] parts = line.split("=");
            if (parts.length == 2) {
                if (parts[0].trim().equals("targetUrls")) {
                    targetUrls = parts[1].trim().split(",");
                } else if (parts[0].trim().equals("timeout")) {
                    timeout = Integer.parseInt(parts[1].trim());
                }
            }
        }
        reader.close();

        for (String targetUrl : targetUrls) {
            HttpURLConnection connection = null;
            try {
                URL url = new URL(targetUrl);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(timeout * 1000);
                connection.setReadTimeout(timeout * 1000);

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    responseBody.append(targetUrl).append(": OK!\n");
                } else {
                    allTargetsOK = false;
                    responseBody.append(targetUrl).append(": Not OK!\n");
                }
            } catch (IOException e) {
                allTargetsOK = false;
                responseBody.append(targetUrl).append(": Not OK! Error: ").append(e.getMessage()).append("\n");
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }

        if (allTargetsOK) {
            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        }

        response.getWriter().println(responseBody.toString()); // Print the response body
    }
}
