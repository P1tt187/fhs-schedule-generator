package models.persistence.scheduletree;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @author fabian
 *         on 28.01.14.
 *         the root node of the scheduletree
 *         containing a list of Weekdays
 */
@Entity
@Table(name = "TBLROOT")
public class Root extends Node {
}
