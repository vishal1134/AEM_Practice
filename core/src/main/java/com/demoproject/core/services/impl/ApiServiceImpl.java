package com.demoproject.core.services.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.osgi.service.component.annotations.Component;

import com.demoproject.core.services.ApiService;

@Component(service = ApiService.class)
public class ApiServiceImpl implements ApiService {

    private static final String API_URL =
            "https://dummyjson.com/products/1";

    @Override
    public String getData() {

        HttpURLConnection connection = null;

        try {
            URL url = new URL(API_URL);

            connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");

            int responseCode = connection.getResponseCode();

            if (responseCode != HttpURLConnection.HTTP_OK) {
                return "API request failed. Response code: "
                        + responseCode;
            }

            BufferedReader reader =
                    new BufferedReader(
                            new InputStreamReader(
                                    connection.getInputStream()
                            )
                    );

            StringBuilder response = new StringBuilder();

            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            reader.close();

            return response.toString();

        } catch (IOException e) {

            return "Error while calling API: "
                    + e.getMessage();

        } finally {

            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}