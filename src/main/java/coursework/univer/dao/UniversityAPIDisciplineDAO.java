package coursework.univer.dao;

import coursework.univer.model.Discipline;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class UniversityAPIDisciplineDAO {

    private final String apiUrl;

    public UniversityAPIDisciplineDAO(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    public List<Discipline> loadDisciplines() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(response.body(), new TypeReference<List<Discipline>>() {});
    }
}
