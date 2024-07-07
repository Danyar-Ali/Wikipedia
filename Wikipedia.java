import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Wikipedia {
    private static final String WIKIPEDIA_API_URL = "https://en.wikipedia.org/w/api.php";
    private static final String FORMAT = "json";
    private static final String ACTION_QUERY = "query";
    private static final String PROP_EXTRACTS = "extracts";
    private static final String EX_INTRO = "1";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter your query: ");
        String query = scanner.nextLine();

        if (!query.isEmpty()) {
            String answer = getWikipediaSummary(query);
            if (answer != null) {
                System.out.println("According to Wikipedia:");
                System.out.println(answer);
            } else {
                System.out.println("No results found on Wikipedia.");
            }
        } else {
            System.out.println("Please enter a valid query.");
        }
    }

    private static String getWikipediaSummary(String query) {
        try {
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);

            String apiUrl = String.format("%s?action=%s&format=%s&prop=%s&titles=%s&exintro=%s",
                    WIKIPEDIA_API_URL, ACTION_QUERY, FORMAT, PROP_EXTRACTS, encodedQuery, EX_INTRO);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonObject jsonObject = JsonParser.parseString(response.body()).getAsJsonObject();
                JsonObject pages = jsonObject.getAsJsonObject("query").getAsJsonObject("pages");

                String pageId = pages.keySet().iterator().next();

                JsonObject page = pages.getAsJsonObject(pageId);
                JsonElement extractElement = page.get("extract");
                if (extractElement != null) {
                    String extract = extractElement.getAsString();

                    extract = cleanExtract(extract);

                    return extract;
                } else {
                    System.err.println("No 'extract' found in Wikipedia response.");
                }
            } else {
                System.err.println("Request failed with HTTP error code: " + response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Error sending request: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Exception occurred: " + e.getMessage());
        }
        return null;
    }

    private static String cleanExtract(String extract) {
        extract = extract.replaceAll("\\<.*?\\>", "");

        extract = extract.trim().replaceAll("\\s+", " ");

        extract = extract.replaceAll("(?m)^\\s*$", "");

        int endIndex = extract.indexOf(".", extract.indexOf(".") + 1);
        if (endIndex != -1) {
            extract = extract.substring(0, endIndex + 1);
        }

        return extract;
    }
}
