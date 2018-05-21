package models;

import io.ebean.Finder;
import io.ebean.Model;

import javax.persistence.*;

@Entity
public class InitialPath extends Model {
    @Id
    public Integer id;
    public String url;
    public String title;
    public Integer hops;

    public static Finder<Integer, InitialPath> find = new Finder<>(InitialPath.class);


}
