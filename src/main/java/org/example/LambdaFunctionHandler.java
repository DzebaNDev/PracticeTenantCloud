package org.example;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class LambdaFunctionHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final String OPENAI_API_KEY = System.getenv("OPENAI_API_KEY");
    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        try {
            JSONObject requestBody = new JSONObject(input.getBody());
            JSONArray listings = requestBody.optJSONArray("items");
            if (listings == null) {
                return createErrorResponse("Invalid request: 'items' field is missing or not an array");
            }

            JSONArray results = new JSONArray();
            for (int i = 0; i < listings.length(); i++) {
                JSONObject listing = listings.getJSONObject(i);
                String prompt = generatePrompt(listing);
                String generatedText = callOpenAi(prompt);

                JSONObject result = new JSONObject();
                result.put("listingId", listing.optInt("listingId", -1));
                result.put("generatedDescription", generatedText);
                results.put(result);
            }

            return new APIGatewayProxyResponseEvent().withStatusCode(200).withBody(results.toString());
        } catch (Exception e) {
            return createErrorResponse("Error processing request: " + e.getMessage());
        }
    }

    private String generatePrompt(JSONObject listing) {
        String description = listing.optString("description", "Опис відсутній").replaceAll("<[^>]*>", "");
        String community = listing.optString("communityTitle", "Невідомо");

        List<String> amenities = jsonArrayToList(listing.optJSONArray("amenities"));
        List<String> customAmenities = jsonArrayToList(listing.optJSONArray("customAmenities"));
        List<String> communityAmenities = jsonArrayToList(listing.optJSONArray("communityCustomAmenities"));

        return String.format(
                "Опис нерухомості: Назва: %s, Адреса: %s, %s, %s, %s. %s. Ціна: $%d. Площа: %d кв. футів. Спальні: %d, Ванні: %d. Спільнота: %s. Зручності: %s. Додаткові зручності: %s. Зручності комплексу: %s.",
                listing.optString("title", "Без назви"),
                listing.optString("address1", "Адреса невідома"),
                listing.optString("city", "Місто невідоме"),
                listing.optString("state", "Штат невідомий"),
                listing.optString("zip", "ZIP-код невідомий"),
                description,
                listing.optInt("minPrice", 0),
                listing.optInt("minSquareFeet", 0),
                listing.optInt("minBedrooms", 0),
                listing.optInt("minBathrooms", 0),
                community,
                String.join(", ", amenities),
                String.join(", ", customAmenities),
                String.join(", ", communityAmenities)
        );
    }

    private String callOpenAi(String prompt) {
        JSONObject requestBody = new JSONObject()
                .put("model", "gpt-3.5-turbo")
                .put("messages", new JSONArray().put(new JSONObject().put("role", "user").put("content", prompt)))
                .put("max_tokens", 400)
                .put("temperature", 0.7);

        HttpResponse<JsonNode> response = Unirest.post(OPENAI_API_URL)
                .header("Authorization", "Bearer " + OPENAI_API_KEY)
                .header("Content-Type", "application/json")
                .body(requestBody.toString())
                .asJson();

        JSONObject responseObject = new JSONObject(response.getBody().toString());
        if (responseObject.has("error")) {
            throw new RuntimeException("OpenAI API Error: " + responseObject.getJSONObject("error").toString());
        }

        return responseObject.getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content").trim();
    }

    private List<String> jsonArrayToList(JSONArray jsonArray) {
        return jsonArray == null ? List.of("Невідомо") : IntStream.range(0, jsonArray.length())
                .mapToObj(jsonArray::optString)
                .collect(Collectors.toList());
    }

    private APIGatewayProxyResponseEvent createErrorResponse(String message) {
        return new APIGatewayProxyResponseEvent().withStatusCode(400).withBody(new JSONObject().put("error", message).toString());
    }
}
