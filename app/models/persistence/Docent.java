package models.persistence;

import play.db.ebean.Model;

import javax.persistence.*;

/**
 * Created by fabian on 01.02.14.
 */
@Entity
@Table(name="TBLDOCENT")
public class Docent extends Model{

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name="ID")
    public Long id;

    @Column(name = "FIRSTNAME")
    public String firstName;

    @Column(name = "LASTNAME")
    public String lastName;
}
