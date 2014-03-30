package models.persistence;

import logic.helpers.ScheduleHelper;
import models.persistence.participants.Course;
import models.persistence.scheduletree.Root;

import javax.persistence.*;

/**
 * @author fabian
 *         on 20.03.14.
 */
@Entity
@Table(name = "TBLSCHEDULE")
public class Schedule extends AbstractEntity {

    @OneToOne(targetEntity = Root.class)
    private Root root;

    public Root getRoot() {
        return root;
    }

    public void setRoot(Root root) {
        this.root = root;
    }

    /**
     * Semester of this Schedule
     */
    @ManyToOne(targetEntity = Semester.class, cascade = CascadeType.MERGE)
    private Semester semester;

    public Semester getSemester() {
        return semester;
    }

    public void setSemester(Semester semester) {
        this.semester = semester;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Schedule schedule = (Schedule) o;

        if (root != null ? !root.equals(schedule.root) : schedule.root != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return root != null ? root.hashCode() : 0;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("Schedule{");
        sb.append("root=").append(root);
        sb.append(", semester=").append(semester);
        sb.append('}');
        return sb.toString();
    }

    public Schedule filter(Course course) {
/*
        Schedule result = new Schedule();
        Root copyRoot = new Root();
        result.setRoot(copyRoot);
        copyRoot.setChildren(new LinkedList<>());
        List<Weekday> copyDays = root.getChildren().stream().map(node -> {
            Weekday day = (Weekday) node;
            Weekday newDay = new Weekday();
            newDay.setParent(copyRoot);
            newDay.setSortIndex(day.getSortIndex());
            newDay.setName(day.getName());
            newDay.setChildren(new LinkedList<>());
            List<Timeslot> timeslotList = day.getChildren().stream().map(n -> {
                Timeslot ts = (Timeslot) n;
                Timeslot newTS = new Timeslot(ts.getStartHour(), ts.getStopHour(), ts.getStopHour(), ts.getStopMinute());
                newTS.setParent(newDay);
                newTS.setLectures(new LinkedList<>());
                return newTS;
            }).collect(Collectors.toList());
            newDay.getChildren().addAll(timeslotList);
            Collections.sort(newDay.getChildren(), (o1, o2) -> {
                Timeslot ts1 = (Timeslot) o1;
                Timeslot ts2 = (Timeslot) o2;
                return ts1.compareTo(ts2);
            });
            return newDay;
        }).collect(Collectors.toList());

        copyRoot.getChildren().addAll(copyDays);
        Collections.sort(copyRoot.getChildren(), (o1, o2) -> {
            Weekday w1 = (Weekday) o1;
            Weekday w2 = (Weekday) o2;
            return w1.compareTo(w2);
        });
*/
/*
        root.getChildren().stream().forEach(node -> {
            Weekday day = (Weekday) node;
            List<List<AbstractLecture>> lectures = new LinkedList<>();

                day.getChildren().stream().forEach(n -> {
                Timeslot ts = (Timeslot) n;
                List<AbstractLecture> lects = ts.getLectures().stream().filter(abstractLecture -> {

                    List<Course> courses = abstractLecture.getParticipants().stream().map(Participant::getCourse).collect(Collectors.toList());

                    return abstractLecture.getParticipants().contains(course) || courses.contains(course);
                }).collect(Collectors.toCollection(LinkedList::new));
                System.err.println( course.getName()+ " lects - " + lects.size());
                lectures.add(lects);
            });

            //System.err.println("course - " + course.getShortName() + " lectures - " + lectures);
            Root r = copyRoot;
            Weekday copyWeekday = (Weekday) copyRoot.getChildren().stream().filter(weekday -> ((Weekday) weekday).getSortIndex().equals(day.getSortIndex())).findFirst().get();
            copyWeekday.setParent(r);
            for (int i = 0; i < lectures.size() - 1; i++) {
                Timeslot ts = (Timeslot) copyWeekday.getChildren().get(i);
                ts.getLectures().addAll(lectures.get(i));
                //System.err.println("day " + ((Weekday) ts.getParent()).getSortIndex() + " placed slots - " + ts.getLectures());
            }


        });*/

        return ScheduleHelper.filterCourse(this,course);
    }
}
