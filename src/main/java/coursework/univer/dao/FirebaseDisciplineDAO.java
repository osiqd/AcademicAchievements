package coursework.univer.dao;

import coursework.univer.model.Discipline;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FirebaseDisciplineDAO {

    private static final String FIREBASE_URL = "";

    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    public FirebaseDisciplineDAO() {}

    /** Сохраняет список дисциплин в Firebase */
    public void saveDisciplines(List<Discipline> disciplines) throws Exception {
        Map<String, Discipline> dataToSave = disciplines.stream()
                .collect(Collectors.toMap(
                        d -> "discipline_" + d.getName().replaceAll("\\s+", "_") + "_" + d.getSemester(),
                        d -> d
                ));

        String json = mapper.writeValueAsString(dataToSave);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(FIREBASE_URL))
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .header("Content-Type", "application/json")
                .build();

        client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    /** Загружает список дисциплин из Firebase */
    public List<Discipline> getDisciplines() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(FIREBASE_URL))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        String body = response.body();

        if (body == null || body.equals("null") || body.isBlank()) {
            return List.of();
        }

        Map<String, Discipline> map = mapper.readValue(body, new TypeReference<>() {});
        return map.values().stream().toList();
    }
}
