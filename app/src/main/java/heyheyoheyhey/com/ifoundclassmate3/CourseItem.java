package heyheyoheyhey.com.ifoundclassmate3;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Av23 on 2015-02-15.
 */
public class CourseItem extends ScheduleItem implements Parcelable {
    ArrayList<ScheduleTime> scheduleTimeArrayList = new ArrayList<>();

    private String id; // eg CS 456
    private String section; // eg LEC 002
    private Date startDate;
    private Date endDate;
    private boolean[] daysOfWeek; // sunday is index 0
    private int startHours;
    private int startMins;
    private int endHours;
    private int endMins;
    private int length; // in minutes

    private String description;

    // necessary for parcelable
    public int describeContents() {
        return 0;
    }

    // writes to parcel
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.id);
        out.writeString(this.section);
        if (startDate != null) out.writeLong(this.startDate.getTime());
        else out.writeLong(-1L);
        if (endDate != null) out.writeLong(this.endDate.getTime());
        else out.writeLong(-1L);
        out.writeBooleanArray(daysOfWeek);
        out.writeInt(startHours);
        out.writeInt(startMins);
        out.writeInt(endHours);
        out.writeInt(endMins);
        out.writeInt(length);

    }

    public static final Parcelable.Creator<CourseItem> CREATOR = new Parcelable.Creator<CourseItem>() {
        public CourseItem createFromParcel(Parcel in) {
            return new CourseItem(in);
        }
        public CourseItem[] newArray(int size) {
            return new CourseItem[size];
        }
    };

    public CourseItem(Parcel in) {
        this.id = in.readString();
        this.section = in.readString();
        long tempStart = in.readLong();
        if (tempStart != -1L) startDate = new Date(tempStart);
        else startDate = null;
        long tempEnd = in.readLong();
        if (tempEnd != -1L) endDate = new Date(tempEnd);
        else endDate = null;
        this.daysOfWeek = new boolean[7];
        in.readBooleanArray(daysOfWeek);
        startHours = in.readInt();
        startMins = in.readInt();
        endHours = in.readInt();
        endMins = in.readInt();
        this.length = in.readInt();
        updateScheduleTimeArray();
    }

    // pull from disk
    public CourseItem(String savedString) {

        String[] fields = savedString.split(":");
        id = fields[0];
        section = fields[1];
        long tempStart = Long.parseLong(fields[2]);
        if (tempStart == -1L) startDate = null;
        else startDate = new Date(tempStart);
        long tempEnd = Long.parseLong(fields[3]);
        if (tempEnd == -1L) endDate = null;
        else endDate = new Date(tempEnd);
        String[] days = fields[4].split(",");
        this.daysOfWeek = new boolean[7];
        for (int i = 0; i < daysOfWeek.length; i++) {
            daysOfWeek[i] = Boolean.parseBoolean(days[i]);
        }
        startHours = Integer.parseInt(fields[5]);
        startMins = Integer.parseInt(fields[6]);
        endHours = Integer.parseInt(fields[7]);
        endMins = Integer.parseInt(fields[8]);
        length = Integer.parseInt(fields[9]);
        /*
        int nextPos = savedString.indexOf(":");
        int startPos = 0;
        this.id = savedString.substring(startPos, nextPos);
        startPos = nextPos + 1;
        nextPos = savedString.indexOf(":", startPos);
        long temp = Long.parseLong(savedString.substring(startPos, nextPos));
        this.startDate = new Date();
        startDate.setTime(temp);
        startPos = nextPos + 1;
        nextPos = savedString.indexOf(":", startPos);
        temp = Long.parseLong(savedString.substring(startPos, nextPos));
        this.endDate = new Date();
        endDate.setTime(temp);
        this.daysOfWeek = new boolean[7];
        for (int i = 0; i < daysOfWeek.length; i++) {
            startPos = nextPos + 1;
            nextPos = savedString.indexOf(",", startPos);
            daysOfWeek[i] = Boolean.parseBoolean(savedString.substring(startPos, nextPos));
        }
        startPos = nextPos + 1;
        nextPos = savedString.indexOf(":", startPos);
        this.startTime = Integer.parseInt(savedString.substring(startPos, nextPos));
        startPos = nextPos + 1;
        this.length = Integer.parseInt(savedString.substring(startPos));
*/
        System.out.println("COURSEITEM: There is " + id + " course @ from " + startDate + " to " + endDate + " @ " + startHours + ":" + startMins + " for " + length);
        updateScheduleTimeArray();
    }

    public CourseItem(String id, String section, Date startDate, Date endDate, boolean daysOfWeek[], int startHours, int startMins, int endHours, int endMins) {
        this.id = id;
        this.section = section;
        this.startDate = startDate;
        this.endDate = endDate;
        this.daysOfWeek = new boolean[7];
        for (int i = 0; i < daysOfWeek.length; i++) {
            this.daysOfWeek[i] = daysOfWeek[i];
        }
        this.startHours = startHours;
        this.startMins = startMins;
        this.endHours = endHours;
        this.endMins = endMins;
        this.length = (endHours - startHours) * 60 + (endMins - startMins);
        updateScheduleTimeArray();
    }

    public void setDescription(String title, String location, String prof) {
        this.description = title + "\n" + location + "\n" + prof + "\n";
    }

    private void updateScheduleTimeArray() {
        if (startDate == null || endDate == null || startHours == -1 || startMins == -1 || endHours == -1 || endMins == -1) return;

        Calendar temp = Calendar.getInstance();
        temp.setTime(startDate);
        Calendar end = Calendar.getInstance();
        end.setTime(endDate);
        while (temp.before(end)) {
            if (daysOfWeek[temp.get(Calendar.DAY_OF_WEEK) - 1]) {
                // NOTE: daysOfWeek indexed at 1
                ScheduleTime newTime = new ScheduleTime(startHours, startMins, endHours, endMins, length, temp.get(Calendar.DAY_OF_MONTH), temp.get(Calendar.MONTH), temp.get(Calendar.YEAR));
                newTime.setEventName(this.id);
                if (this.description != null) newTime.setDescription(this.description);
                this.scheduleTimeArrayList.add(newTime);
            }
            temp.add(Calendar.DATE, 1);
        }
    }

    @Override
    public ArrayList<ScheduleTime> getScheduleForDay(int day, int month, int year) {
        ArrayList<ScheduleTime> retVal = new ArrayList<>();
        for (ScheduleTime scheduleTime: scheduleTimeArrayList) {
            if (scheduleTime.day == day && scheduleTime.month == month && scheduleTime.year == year) {
                retVal.add(scheduleTime);
            }
        }
        return retVal;
    }

    @Override
    public String getSaveString() {
        String retVal = "COURSEITEM\n";
        retVal += this.id + ":" + this.section + ":" + this.startDate.getTime() + ":" + this.endDate.getTime() + ":";
        for (int i = 0; i < this.daysOfWeek.length; i++) {
            retVal += this.daysOfWeek[i] + ",";
        }
        retVal += ":" + this.startHours + ":" + this.startMins + ":" + this.endHours + ":" + this.endMins + ":" + this.length + "\n";
        return retVal;
    }

    public String getId() {
        return this.id;
    }

    public String getSection() {
        return this.section;
    }

    public int getStartHours() { return this.startHours; }
    public int getStartMins() { return this.startMins; }
    public int getEndHours() { return this.endHours; }
    public int getEndMins() { return this.endMins; }
}