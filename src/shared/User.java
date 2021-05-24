package shared;

import java.io.Serializable;
import java.util.ArrayList;
import shared.Lesson;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    private String username;
    private String password;
    private int id_user;
    private short is_instructor;
    private ArrayList<Lesson> lessons;
    private ArrayList<Lesson> upcomingLessons;

    public User(String username, String password, int id_user, short is_instructor) {
        this.username = username;
        this.password = password;
        this.id_user = id_user;
        this.is_instructor = is_instructor;
        this.lessons = new ArrayList<Lesson>();
        this.upcomingLessons = new ArrayList<Lesson>();
    }

    public ArrayList<Lesson> getLessons() {
        return this.lessons;
    }

    public ArrayList<Lesson> getUpcomingLessons() {
        return this.upcomingLessons;
    }

    public void addLesson(Lesson lesson) {
        this.lessons.add(lesson);
    }
    public void addUpcomingLesson(Lesson lesson) { this.upcomingLessons.add(lesson); }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public int getId_user() {
        return id_user;
    }

    public short getIs_instructor() {
        return is_instructor;
    }
}
