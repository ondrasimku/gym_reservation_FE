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

    public User(String username, String password, int id_user, short is_instructor) {
        this.username = username;
        this.password = password;
        this.id_user = id_user;
        this.is_instructor = is_instructor;
    }

    public void addLesson(Lesson lesson) {
        this.lessons.add(lesson);
    }

}
