package client;

public class Lesson {

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
}
