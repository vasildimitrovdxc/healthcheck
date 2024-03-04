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

@WebServlet("/")
public class HealthCheckServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String configFilePath = "/opt/props/healthcheck.conf";
        BufferedReader reader = new BufferedReader(new FileReader(configFilePath));
        String targetUrl = "";
        int timeout = 0;
        String line;
        while ((line = reader.readLine()) != null) {
            String[] parts = line.split("=");
            if (parts.length == 2) {
                if (parts[0].trim().equals("targetUrl")) {
                    targetUrl = parts[1].trim();
                } else if (parts[0].trim().equals("timeout")) {
                    timeout = Integer.parseInt(parts[1].trim());
                }
            }
        }
        reader.close();

        HttpURLConnection connection = null;
        try {
            URL url = new URL(targetUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(timeout * 1000);
            connection.setReadTimeout(timeout * 1000);

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder responseBody = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    responseBody.append(inputLine);
                }
                in.close();

                response.getWriter().println(responseBody.toString());
            } else {
                response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            }
        } catch (IOException e) {
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
