package tn.esprit.tools;

import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class WeatherService {
    private static final String API_KEY = "f9dc6fb51bae374ad0545e06d4c84c23";
    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/weather";

    // Major Tunisian cities for farmers
    private static final Map<String, String> TUNISIAN_CITIES = new HashMap<>() {{
        put("Tunis", "Tunis");
        put("Sfax", "Sfax");
        put("Sousse", "Sousse");
        put("Kairouan", "Kairouan");
        put("Bizerte", "Bizerte");
        put("Ariana", "Ariana");
        put("Gafsa", "Gafsa");
        put("Nabeul", "Nabeul");
        put("Jendouba", "Jendouba");
        put("Beja", "Beja");
        put("Kasserine", "Kasserine");
        put("Siliana", "Siliana");
        put("Mahdia", "Mahdia");
        put("Monastir", "Monastir");
        put("Zaghouan", "Zaghouan");
        put("Manouba", "Manouba");
        put("Tataouine", "Tataouine");
        put("Medenine", "Medenine");
        put("Kebili", "Kebili");
        put("Tozeur", "Tozeur");
        put("El Kef", "El Kef");
    }};

    public static String getWeatherForCity(String city) {
        try {
            String urlString = BASE_URL + "?q=" + city + ",TN&appid=" + API_KEY + "&units=metric";
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            return parseWeatherData(response.toString(), city);
        } catch (Exception e) {
            e.printStackTrace();
            return "Unable to fetch weather data for " + city;
        }
    }

    private static String parseWeatherData(String json, String city) {
        JSONObject obj = new JSONObject(json);
        JSONObject main = obj.getJSONObject("main");
        double temp = main.getDouble("temp");
        int humidity = main.getInt("humidity");
        JSONObject weather = obj.getJSONArray("weather").getJSONObject(0);
        String description = weather.getString("description");

        return String.format("""
            Weather in %s:
            Temperature: %.1f°C
            Humidity: %d%%
            Conditions: %s
            """, city, temp, humidity, description);
    }

    public static Map<String, String> getTunisianCities() {
        return TUNISIAN_CITIES;
    }

    public static String getAgriculturalAdvice(double temp, int humidity, String conditions) {
        StringBuilder advice = new StringBuilder("🌱 Agricultural Advice:\n");

        // Temperature-based advice
        if (temp > 35) {
            advice.append("⚠️ Extreme heat: Irrigate in early morning or evening\n");
            advice.append("💧 Increase water supply for sensitive crops\n");
        } else if (temp > 25) {
            advice.append("☀️ Warm weather: Good for most crops\n");
        } else if (temp < 10) {
            advice.append("❄️ Cold weather: Protect sensitive plants\n");
            advice.append("🌿 Consider covering crops overnight\n");
        } else {
            advice.append("🌤 Moderate temperatures: Ideal growing conditions\n");
        }

        // Humidity-based advice
        if (humidity > 80) {
            advice.append("💦 High humidity: Watch for fungal diseases\n");
            advice.append("🍄 Apply fungicide if needed\n");
        } else if (humidity < 30) {
            advice.append("🏜️ Low humidity: Increase irrigation frequency\n");
        }

        // Conditions-based advice
        if (conditions.toLowerCase().contains("rain")) {
            advice.append("🌧️ Rain expected: Delay fertilizer application\n");
            advice.append("🚜 Prepare drainage for heavy rains\n");
        } else if (conditions.toLowerCase().contains("wind")) {
            advice.append("🌬️ Windy conditions: Secure young plants\n");
        } else if (conditions.toLowerCase().contains("sun")) {
            advice.append("☀️ Sunny weather: Good for drying crops\n");
        }

        // General seasonal advice
        advice.append("\n🌾 Current season tips:\n");
        advice.append("- Check soil moisture regularly\n");
        advice.append("- Monitor for pests and diseases\n");
        advice.append("- Plan harvest schedules accordingly\n");

        return advice.toString();
    }
}