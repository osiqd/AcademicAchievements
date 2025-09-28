package coursework.univer.service;

import coursework.univer.model.Discipline;
import javafx.collections.ObservableList;
import java.util.List;
import java.util.stream.Collectors;

public class RecommendationService {
    private final ObservableList<Discipline> disciplines;
    public RecommendationService(ObservableList<Discipline> disciplines) { this.disciplines = disciplines; }

    public List<String> overloadedSemesters(int maxCredits) {
        return disciplines.stream()
                .collect(Collectors.groupingBy(Discipline::getSemester,
                        Collectors.summingInt(Discipline::getCredits)))
                .entrySet().stream()
                .filter(e -> e.getValue() > maxCredits)
                .map(e -> "Семестр " + e.getKey() + " перегружен: " + e.getValue() + " кредитов")
                .collect(Collectors.toList());
    }

    public String gpaAdvice() {
        double gpa = disciplines.stream().mapToDouble(d -> d.getGrade() * d.getCredits()).sum() /
                disciplines.stream().mapToInt(Discipline::getCredits).sum();
        if (gpa < 3.0) return "Рассмотрите повторное изучение слабых дисциплин для повышения GPA.";
        else if (gpa < 4.0) return "Сфокусируйтесь на дисциплинах с низкими оценками.";
        else return "Ваша успеваемость отличная, поддерживайте текущий темп!";
    }
}
