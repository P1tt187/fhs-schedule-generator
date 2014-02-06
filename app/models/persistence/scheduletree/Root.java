package models.persistence.scheduletree;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Created by fabian on 28.01.14.
 */
@Entity
@Table(name = "TBLROOT")
/**
 * the root node of the scheduletree
 * containing a list of Weekdays
 */
public class Root extends Node {
}
