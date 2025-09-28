package coursework.univer.controller;

import coursework.univer.dao.CSVDisciplineDAO;
import coursework.univer.dao.UniversityAPIDisciplineDAO;
import coursework.univer.dao.FirebaseDisciplineDAO;
import coursework.univer.model.Discipline;
import coursework.univer.service.AcademicService;
import coursework.univer.service.RecommendationService;

import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AcademicController {

    @FXML private TableView<Discipline> table;
    @FXML private TableColumn<Discipline, String> nameColumn;
    @FXML private TableColumn<Discipline, Number> semesterColumn;
    @FXML private TableColumn<Discipline, Number> creditsColumn;
    @FXML private TableColumn<Discipline, String> teacherColumn;
    @FXML private TableColumn<Discipline, String> controlColumn;
    @FXML private TableColumn<Discipline, Number> gradeColumn;
    @FXML private TableColumn<Discipline, String> statusColumn;
    @FXML private TableColumn<Discipline, String> dateColumn;
    @FXML private BarChart<String, Number> chart;
    @FXML private TextField filterField;
    @FXML private Button addButton, editButton, deleteButton, gpaButton, forecastButton;
    @FXML private Button importButton, exportButton, loadAPIButton, saveFirebaseButton, loadFirebaseButton, recommendButton;
    @FXML private ComboBox<String> chartTypeBox;

    private final AcademicService service = new AcademicService();
    private final CSVDisciplineDAO dao = new CSVDisciplineDAO(new File("disciplines.csv"));
    private final UniversityAPIDisciplineDAO apiDAO = new UniversityAPIDisciplineDAO("http://localhost:8080/api/disciplines");
    private final FirebaseDisciplineDAO firebaseDAO = new FirebaseDisciplineDAO();
    private FilteredList<Discipline> filteredData;

    @FXML
    public void initialize() {
        setupTable();
        setupChartBox();
        loadFromCSV();
        setupFiltering();
        setupButtons();
    }

    private void setupTable() {
        nameColumn.setCellValueFactory(cell -> cell.getValue().nameProperty());
        semesterColumn.setCellValueFactory(cell -> cell.getValue().semesterProperty());
        creditsColumn.setCellValueFactory(cell -> cell.getValue().creditsProperty());
        teacherColumn.setCellValueFactory(cell -> cell.getValue().teacherProperty());
        controlColumn.setCellValueFactory(cell -> cell.getValue().controlTypeProperty());
        statusColumn.setCellValueFactory(cell -> cell.getValue().statusProperty());
        dateColumn.setCellValueFactory(cell -> cell.getValue().dateProperty());

        // Кастомный CellFactory для gradeColumn
        gradeColumn.setCellValueFactory(cell -> cell.getValue().gradeProperty());
        gradeColumn.setCellFactory(col -> new TableCell<Discipline, Number>() {
            @Override
            protected void updateItem(Number grade, boolean empty) {
                super.updateItem(grade, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                } else {
                    Discipline d = (Discipline) getTableRow().getItem();
                    if ("В процессе".equalsIgnoreCase(d.getStatus())) {
                        setText("-");
                    } else {
                        setText(String.valueOf(grade.doubleValue()));
                    }
                }
            }
        });
    }

    private void setupChartBox() {
        chartTypeBox.getItems().addAll("Кредиты","Оценки","Кредиты + Оценки","GPA по предметам","Прогноз GPA");
        chartTypeBox.setValue("Кредиты");
        chartTypeBox.valueProperty().addListener((obs, oldVal, newVal) -> updateChartSafe());
    }

    private void loadFromCSV() {
        service.setDisciplines(dao.getAll());
        setupTableData();
        updateChartSafe();
    }

    private void setupTableData() {
        filteredData = new FilteredList<>(service.getDisciplines(), p -> true);
        SortedList<Discipline> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sortedData);
    }

    private void setupFiltering() {
        filterField.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredData.setPredicate(d -> {
                if (newVal == null || newVal.isBlank()) return true;
                String filter = newVal.toLowerCase();
                return d.getName().toLowerCase().contains(filter)
                        || d.getTeacher().toLowerCase().contains(filter)
                        || d.getControlType().toLowerCase().contains(filter)
                        || d.getStatus().toLowerCase().contains(filter)
                        || d.getDate().toLowerCase().contains(filter);
            });
            updateChartSafe();
        });
    }

    private void setupButtons() {
        addButton.setOnAction(e -> showDisciplineDialog(null));
        editButton.setOnAction(e -> {
            Discipline d = table.getSelectionModel().getSelectedItem();
            if (d != null) showDisciplineDialog(d);
        });
        deleteButton.setOnAction(e -> {
            Discipline d = table.getSelectionModel().getSelectedItem();
            if (d != null) {
                service.getDisciplines().remove(d);
                dao.saveAll(service.getDisciplines());
                refreshTable();
            }
        });
        gpaButton.setOnAction(e -> showGPA());
        forecastButton.setOnAction(e -> forecastGPA());
        importButton.setOnAction(e -> importCSV());
        exportButton.setOnAction(e -> exportCSV());
        loadAPIButton.setOnAction(e -> loadFromAPI());
        saveFirebaseButton.setOnAction(e -> saveToFirebase());
        loadFirebaseButton.setOnAction(e -> loadFromFirebase());
        recommendButton.setOnAction(e -> showRecommendations());
    }

    private void refreshTable() {
        setupTableData();
        updateChartSafe();
    }

    // --------------------- Firebase ---------------------
    private void saveToFirebase() {
        try {
            firebaseDAO.saveDisciplines(service.getDisciplines());
            showAlert("Firebase", "Данные успешно сохранены в Firebase!");
        } catch (Exception e) {
            showError("Ошибка сохранения в Firebase", e);
        }
    }

    private void loadFromFirebase() {
        try {
            List<Discipline> disciplines = firebaseDAO.getDisciplines();
            service.setDisciplines(disciplines);
            dao.saveAll(disciplines);
            refreshTable();
            showAlert("Firebase", "Данные успешно загружены из Firebase!");
        } catch (Exception e) {
            showError("Ошибка загрузки из Firebase", e);
        }
    }

    // --------------------- API ---------------------
    private void loadFromAPI() {
        try {
            List<Discipline> disciplines = apiDAO.loadDisciplines();
            service.setDisciplines(disciplines);
            dao.saveAll(disciplines);
            refreshTable();
        } catch (Exception e) {
            showError("Ошибка загрузки с API", e);
        }
    }

    // --------------------- CSV ---------------------
    private void importCSV() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Импорт CSV");
        File file = chooser.showOpenDialog(table.getScene().getWindow());
        if (file != null) {
            CSVDisciplineDAO importDao = new CSVDisciplineDAO(file);
            service.setDisciplines(importDao.getAll());
            dao.saveAll(service.getDisciplines());
            refreshTable();
        }
    }

    private void exportCSV() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Экспорт CSV");
        File file = chooser.showSaveDialog(table.getScene().getWindow());
        if (file != null) {
            CSVDisciplineDAO exportDao = new CSVDisciplineDAO(file);
            exportDao.saveAll(service.getDisciplines());
        }
    }

    // --------------------- Диалог ---------------------
    private void showDisciplineDialog(Discipline d) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(d == null ? "Добавить дисциплину" : "Редактировать дисциплину");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField nameField = new TextField(d != null ? d.getName() : "");
        TextField semField = new TextField(d != null ? String.valueOf(d.getSemester()) : "");
        TextField creditsField = new TextField(d != null ? String.valueOf(d.getCredits()) : "");
        TextField teacherField = new TextField(d != null ? d.getTeacher() : "");
        TextField dateField = new TextField(d != null ? d.getDate() : "");

        ComboBox<String> controlBox = new ComboBox<>();
        controlBox.getItems().addAll("Экзамен", "Зачёт", "Курсовая", "Проект");
        controlBox.setValue(d != null ? d.getControlType() : "Экзамен");

        ComboBox<String> gradeBox = new ComboBox<>();
        gradeBox.getItems().addAll("5","4","3","2","Незачёт");
        gradeBox.setValue(d != null ? (d.getGrade() == 0 ? "Незачёт" : String.valueOf(d.getGrade())) : "5");

        ComboBox<String> statusBox = new ComboBox<>();
        statusBox.getItems().addAll("Сдано","Не сдано","В процессе");
        statusBox.setValue(d != null ? d.getStatus() : "Сдано");

        dialog.getDialogPane().setContent(new VBox(5,
                new Label("Название:"), nameField,
                new Label("Семестр:"), semField,
                new Label("Кредиты:"), creditsField,
                new Label("Преподаватель:"), teacherField,
                new Label("Тип контроля:"), controlBox,
                new Label("Оценка:"), gradeBox,
                new Label("Статус:"), statusBox,
                new Label("Дата:"), dateField
        ));

        dialog.setResultConverter(bt -> {
            if (bt == ButtonType.OK) {
                Discipline newD = new Discipline(
                        nameField.getText(),
                        Integer.parseInt(semField.getText()),
                        Integer.parseInt(creditsField.getText()),
                        teacherField.getText(),
                        controlBox.getValue(),
                        "В процессе".equalsIgnoreCase(statusBox.getValue()) ? 0 : (gradeBox.getValue().equals("Незачёт") ? 0 : Double.parseDouble(gradeBox.getValue())),
                        statusBox.getValue(),
                        dateField.getText()
                );
                if (d != null) service.getDisciplines().remove(d);
                service.getDisciplines().add(newD);
                dao.saveAll(service.getDisciplines());
                refreshTable();
            }
            return null;
        });
        dialog.showAndWait();
    }

    // --------------------- Рекомендации ---------------------
    private void showRecommendations() {
        StringBuilder sb = new StringBuilder("Рекомендации по дисциплинам:\n\n");
        service.getDisciplines().forEach(d -> {
            sb.append(d.getName()).append(":\n");
            if (d.getCredits() > 5) sb.append("- Большая нагрузка, распределяйте время эффективно.\n");
            if (d.getGrade() < 3 && !"В процессе".equalsIgnoreCase(d.getStatus())) sb.append("- Низкая оценка, требуется повторное изучение материала.\n");
            else if (d.getGrade() >= 4) sb.append("- Оценка хорошая, продолжайте в том же духе.\n");
            if ("В процессе".equalsIgnoreCase(d.getStatus()) || "Не сдано".equalsIgnoreCase(d.getStatus())) {
                sb.append("- Дисциплина не завершена, не забывайте о дедлайнах.\n");
                sb.append("- Дата выполнения: ").append(d.getDate()).append("\n");
            }
            sb.append("\n");
        });
        showAlert("Рекомендации", sb.toString());
    }

    private void showGPA() {
        showAlert("GPA", String.format("%.2f", service.calculateGPA()));
    }

    private void forecastGPA() {
        TextInputDialog dialog = new TextInputDialog("0");
        dialog.setTitle("Прогноз GPA");
        dialog.setHeaderText("Введите среднюю оценку для будущих дисциплин:");
        dialog.showAndWait().ifPresent(input -> {
            double futureAvg = Double.parseDouble(input);
            int futureCredits = 10;
            showAlert("Прогноз GPA", String.format("%.2f", service.forecastGPA(futureAvg, futureCredits)));
        });
    }

    // --------------------- График ---------------------
    private void updateChartSafe() {
        try { updateChart(); } catch (Exception ignored) { chart.getData().clear(); }
    }

    private void updateChart() {
        if (chartTypeBox == null || chartTypeBox.getValue() == null) return;
        String type = chartTypeBox.getValue();
        chart.getData().clear();
        Map<String, List<Discipline>> grouped = filteredData.stream().collect(Collectors.groupingBy(Discipline::getName));

        switch (type) {
            case "Кредиты" -> {
                XYChart.Series<String, Number> series = new XYChart.Series<>();
                series.setName("Кредиты");
                grouped.forEach((name, list) -> series.getData().add(new XYChart.Data<>(name, list.stream().mapToInt(Discipline::getCredits).sum())));
                chart.getData().add(series);
            }
            case "Оценки" -> {
                XYChart.Series<String, Number> series = new XYChart.Series<>();
                series.setName("Оценки");
                grouped.forEach((name, list) -> {
                    double avg = list.stream()
                            .filter(d -> !"В процессе".equalsIgnoreCase(d.getStatus()))
                            .mapToDouble(Discipline::getGrade)
                            .average().orElse(0);
                    series.getData().add(new XYChart.Data<>(name, avg));
                });
                chart.getData().add(series);
            }
            case "Кредиты + Оценки" -> {
                XYChart.Series<String, Number> creditsSeries = new XYChart.Series<>();
                creditsSeries.setName("Кредиты");
                XYChart.Series<String, Number> gradesSeries = new XYChart.Series<>();
                gradesSeries.setName("Оценки");
                grouped.forEach((name, list) -> {
                    creditsSeries.getData().add(new XYChart.Data<>(name, list.stream().mapToInt(Discipline::getCredits).sum()));
                    double avg = list.stream()
                            .filter(d -> !"В процессе".equalsIgnoreCase(d.getStatus()))
                            .mapToDouble(Discipline::getGrade)
                            .average().orElse(0);
                    gradesSeries.getData().add(new XYChart.Data<>(name, avg));
                });
                chart.getData().addAll(creditsSeries, gradesSeries);
            }
            case "GPA по предметам" -> {
                XYChart.Series<String, Number> series = new XYChart.Series<>();
                series.setName("GPA");
                grouped.forEach((name, list) -> {
                    double avg = list.stream()
                            .filter(d -> !"В процессе".equalsIgnoreCase(d.getStatus()))
                            .mapToDouble(Discipline::getGrade)
                            .average().orElse(0);
                    series.getData().add(new XYChart.Data<>(name, avg));
                });
                chart.getData().add(series);
            }
            case "Прогноз GPA" -> {
                XYChart.Series<String, Number> series = new XYChart.Series<>();
                series.setName("Прогноз GPA");
                grouped.forEach((name, list) -> {
                    double avg = list.stream()
                            .filter(d -> !"В процессе".equalsIgnoreCase(d.getStatus()))
                            .mapToDouble(Discipline::getGrade)
                            .average().orElse(0);
                    series.getData().add(new XYChart.Data<>(name, service.forecastGPA(avg, 10)));
                });
                chart.getData().add(series);
            }
        }
    }

    // --------------------- Утилиты ---------------------
    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.setTitle(title);
        alert.showAndWait();
    }

    private void showError(String title, Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR, e.getMessage(), ButtonType.OK);
        alert.setTitle(title);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.showAndWait();
    }
}
