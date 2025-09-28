package coursework.univer.service;

import coursework.univer.model.Discipline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.List;

public class AcademicService {
    private final ObservableList<Discipline> disciplines = FXCollections.observableArrayList();
    public ObservableList<Discipline> getDisciplines() { return disciplines; }
    public void setDisciplines(List<Discipline> list) { disciplines.setAll(list); }

    public double calculateGPA() {
        int totalCredits = disciplines.stream().mapToInt(Discipline::getCredits).sum();
        double totalPoints = disciplines.stream().mapToDouble(d -> d.getGrade() * d.getCredits()).sum();
        return totalCredits == 0 ? 0 : totalPoints / totalCredits;
    }

    public double forecastGPA(double futureAverage, int futureCredits) {
        int currentCredits = disciplines.stream().mapToInt(Discipline::getCredits).sum();
        double currentPoints = disciplines.stream().mapToDouble(d -> d.getGrade() * d.getCredits()).sum();
        return (currentPoints + futureAverage * futureCredits) / (currentCredits + futureCredits);
    }
}
