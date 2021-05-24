package shared;

import java.io.Serializable;
import java.util.Date;

public class Lesson implements Serializable {

    private int id_lesson;
    private String date;
    private String name;
    private String text;

    public Lesson(int id_lesson, String date, String name, String text) {
        this.id_lesson = id_lesson;
        this.date = date;
        this.name = name;
        this.text = text;
    }

    public int getID() {
        return id_lesson;
    }

    public String getDate() {
        return date;
    }

    public String getName() {
        return name;
    }

    public String getText() {
        return text;
    }
}
