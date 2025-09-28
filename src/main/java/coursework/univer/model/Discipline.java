package coursework.univer.model;

import javafx.beans.property.*;

public class Discipline {
    private final StringProperty name = new SimpleStringProperty();
    private final IntegerProperty semester = new SimpleIntegerProperty();
    private final IntegerProperty credits = new SimpleIntegerProperty();
    private final StringProperty teacher = new SimpleStringProperty();
    private final StringProperty controlType = new SimpleStringProperty();
    private final DoubleProperty grade = new SimpleDoubleProperty();
    private final StringProperty status = new SimpleStringProperty();
    private final StringProperty date = new SimpleStringProperty(); // новая дата

    public Discipline() {}

    public Discipline(String name, int semester, int credits, String teacher,
                      String controlType, double grade, String status, String date) {
        this.name.set(name);
        this.semester.set(semester);
        this.credits.set(credits);
        this.teacher.set(teacher);
        this.controlType.set(controlType);
        this.grade.set(grade);
        this.status.set(status);
        this.date.set(date);
    }

    // Property getters
    public StringProperty nameProperty() { return name; }
    public IntegerProperty semesterProperty() { return semester; }
    public IntegerProperty creditsProperty() { return credits; }
    public StringProperty teacherProperty() { return teacher; }
    public StringProperty controlTypeProperty() { return controlType; }
    public DoubleProperty gradeProperty() { return grade; }
    public StringProperty statusProperty() { return status; }
    public StringProperty dateProperty() { return date; }

    // обычные getters/setters
    public String getName() { return name.get(); }
    public void setName(String n) { name.set(n); }

    public int getSemester() { return semester.get(); }
    public void setSemester(int s) { semester.set(s); }

    public int getCredits() { return credits.get(); }
    public void setCredits(int c) { credits.set(c); }

    public String getTeacher() { return teacher.get(); }
    public void setTeacher(String t) { teacher.set(t); }

    public String getControlType() { return controlType.get(); }
    public void setControlType(String c) { controlType.set(c); }

    public double getGrade() { return grade.get(); }
    public void setGrade(double g) { grade.set(g); }

    public String getStatus() { return status.get(); }
    public void setStatus(String s) { status.set(s); }

    public String getDate() { return date.get(); }
    public void setDate(String d) { date.set(d); }
}
