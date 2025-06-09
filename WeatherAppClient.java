import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;

// Main Weather API Client Class
public class WeatherApiClient {
    private static final String API_KEY = "your_api_key_here"; // Replace with your OpenWeatherMap API key
    private static final String BASE_URL = "http://api.openweathermap.org/data/2.5/weather";
    
    private ObjectMapper objectMapper;
    
    public WeatherApiClient() {
        this.objectMapper = new ObjectMapper();
    }
    
    public static void main(String[] args) {
        WeatherApiClient client = new WeatherApiClient();
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("=== Weather Data Fetcher ===");
        System.out.print("Enter city name: ");
        String cityName = scanner.nextLine();
        
        try {
            WeatherData weatherData = client.fetchWeatherData(cityName);
            client.displayWeatherData(weatherData);
        } catch (Exception e) {
            System.err.println("Error fetching weather data: " + e.getMessage());
        }
        
        scanner.close();
    }
    
    /**
     * Fetches weather data from OpenWeatherMap API
     */
    public WeatherData fetchWeatherData(String cityName) throws IOException {
        String urlString = String.format("%s?q=%s&appid=%s&units=metric", 
                                       BASE_URL, cityName, API_KEY);
        
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
        try {
            // Set request method and headers
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            
            // Check response code
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new IOException("HTTP error code: " + responseCode);
            }
            
            // Read response
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(connection.getInputStream())
            );
            StringBuilder response = new StringBuilder();
            String line;
            
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            
            // Parse JSON response
            return objectMapper.readValue(response.toString(), WeatherData.class);
            
        } finally {
            connection.disconnect();
        }
    }
    
    /**
     * Displays weather data in a structured format
     */
    public void displayWeatherData(WeatherData data) {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("         WEATHER INFORMATION");
        System.out.println("=".repeat(50));
        
        System.out.printf(" Location: %s, %s%n", data.name, data.sys.country);
        System.out.printf(" Temperature: %.1f°C (feels like %.1f°C)%n", 
                         data.main.temp, data.main.feelsLike);
        System.out.printf("  Condition: %s - %s%n", 
                         data.weather[0].main, data.weather[0].description);
        System.out.printf(" Humidity: %d%%%n", data.main.humidity);
        System.out.printf("  Wind: %.1f m/s%n", data.wind.speed);
        System.out.printf(" Pressure: %d hPa%n", data.main.pressure);
        
        if (data.main.tempMin != data.main.tempMax) {
            System.out.printf(" Temperature Range: %.1f°C - %.1f°C%n", 
                             data.main.tempMin, data.main.tempMax);
        }
        
        if (data.visibility > 0) {
            System.out.printf("  Visibility: %.1f km%n", data.visibility / 1000.0);
        }
        
        System.out.println("=".repeat(50));
    }
}

// Data classes for JSON mapping
@JsonIgnoreProperties(ignoreUnknown = true)
class WeatherData {
    public String name;
    public Main main;
    public Weather[] weather;
    public Wind wind;
    public Sys sys;
    public int visibility;
}

@JsonIgnoreProperties(ignoreUnknown = true)
class Main {
    public double temp;
    public double humidity;
    public double pressure;
    
    @com.fasterxml.jackson.annotation.JsonProperty("feels_like")
    public double feelsLike;
    
    @com.fasterxml.jackson.annotation.JsonProperty("temp_min")
    public double tempMin;
    
    @com.fasterxml.jackson.annotation.JsonProperty("temp_max")
    public double tempMax;
}

@JsonIgnoreProperties(ignoreUnknown = true)
class Weather {
    public String main;
    public String description;
    public String icon;
}

@JsonIgnoreProperties(ignoreUnknown = true)
class Wind {
    public double speed;
    public double deg;
}

@JsonIgnoreProperties(ignoreUnknown = true)
class Sys {
    public String country;
    public long sunrise;
    public long sunset;
}
