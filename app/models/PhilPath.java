package models;

import io.ebean.Finder;
import io.ebean.Model;

import javax.persistence.*;

@Entity
public class PhilPath extends Model {
    @Id
    public Integer id;
    public Integer initpathid;
    public String title;
    public String url;

    public static Finder<Integer, PhilPath> find = new Finder<>(PhilPath.class);
}
