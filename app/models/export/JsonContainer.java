package models.export;

import models.persistence.location.HouseEntity;
import models.persistence.subject.AbstractSubject;
import models.persistence.template.WeekdayTemplate;

import java.util.List;

/**
 * @author fabian
 *         on 24.03.14.
 */
public class JsonContainer {

    private List<HouseEntity> houses;

    private List<AbstractSubject> subjects;

    private List<WeekdayTemplate> weekdayTemplates;

    public List<WeekdayTemplate> getWeekdayTemplates() {
        return weekdayTemplates;
    }

    public void setWeekdayTemplates(List<WeekdayTemplate> weekdayTemplates) {
        this.weekdayTemplates = weekdayTemplates;
    }

    public List<HouseEntity> getHouses() {
        return houses;
    }

    public void setHouses(List<HouseEntity> houses) {
        this.houses = houses;
    }

    public List<AbstractSubject> getSubjects() {
        return subjects;
    }

    public void setSubjects(List<AbstractSubject> subjects) {
        this.subjects = subjects;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("JsonContainer{");
        sb.append("houses=").append(houses);
        sb.append(", subjects=").append(subjects);
        sb.append(", weekdayTemplates=").append(weekdayTemplates);
        sb.append('}');
        return sb.toString();
    }
}
