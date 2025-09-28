package coursework.univer.dao;

import coursework.univer.model.Discipline;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class CSVDisciplineDAO {

    private final File file;

    public CSVDisciplineDAO(File file) {
        this.file = file;
    }

    public List<Discipline> getAll() {
        List<Discipline> list = new ArrayList<>();
        if (!file.exists()) return list;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] tokens = line.split(";");
                if (tokens.length < 8) continue; // проверка количества полей
                Discipline d = new Discipline(
                        tokens[0],                       // name
                        Integer.parseInt(tokens[1]),     // semester
                        Integer.parseInt(tokens[2]),     // credits
                        tokens[3],                       // teacher
                        tokens[4],                       // controlType
                        Double.parseDouble(tokens[5]),   // grade
                        tokens[6],                       // status
                        tokens[7]                        // date
                );
                list.add(d);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    public void saveAll(List<Discipline> disciplines) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            for (Discipline d : disciplines) {
                bw.write(String.join(";",
                        d.getName(),
                        String.valueOf(d.getSemester()),
                        String.valueOf(d.getCredits()),
                        d.getTeacher(),
                        d.getControlType(),
                        String.valueOf(d.getGrade()),
                        d.getStatus(),
                        d.getDate()
                ));
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
