package models.persistence.scheduletree;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * Created by fabian on 28.01.14.
 */
@Entity
@DiscriminatorValue("NROOT")
public class Root extends Node{
}
