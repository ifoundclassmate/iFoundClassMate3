package heyheyoheyhey.com.ifoundclassmate3.support;

import android.content.Intent;
import android.os.AsyncTask;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import heyheyoheyhey.com.ifoundclassmate3.CourseItem;
import heyheyoheyhey.com.ifoundclassmate3.MainActivity;
import heyheyoheyhey.com.ifoundclassmate3.User;

/**
 * Created by Av23 on 2015-02-25.
 */
public class ServerFunction extends AsyncTask<Void, Void, Boolean> {
    public interface ServerTaskListener {
        void onPostExecuteConcluded(boolean result, Object retVal);
    }

    private String mTask;
    private User mUser;
    private CourseItem mCourseItem;
    private ServerTaskListener mTaskListener;

    private ArrayList<CourseItem> retCourseItems;

    public ServerFunction(String task) {
        mTask = task;
    }

    final public void setListener(ServerTaskListener listener) {
        mTaskListener = listener;
    }

    public void setUser(User user) {
        mUser = user;
    }

    public void setCourseItem(CourseItem courseItem) {
        mCourseItem = courseItem;
    }



    @Override
    protected Boolean doInBackground(Void... params) {
        boolean success = false;
        if (mTask.equals(ServerUtils.TASK_ADD_COURSE)) {
            if (mUser == null || mCourseItem == null) return false;
            success = addCourse();
        } else if (mTask.equals(ServerUtils.TASK_RETRIEVE_COURSES)) {
            if (mUser == null) return false;
            success = retrieveCourses();
        }
        return success;

    }

    @Override
    protected void onPostExecute(final Boolean success) {

        if (success) {
            if (mTask.equals(ServerUtils.TASK_RETRIEVE_COURSES)) {
                if (mTaskListener != null) {
                    mTaskListener.onPostExecuteConcluded(success, retCourseItems);
                }
            }

        }
    }

    private boolean addCourse() {
        try {
            Socket clientSocket = new Socket(ServerUtils.TEMP_IP, ServerUtils.PORT_COURSE);
            DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
            BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String toServer = ServerUtils.TASK_ADD_COURSE + "\n" + mUser.getId() + "\n";
            toServer += mCourseItem.getId() + "\n" + mCourseItem.getSection() + "\n";
            outToServer.writeBytes(toServer);
            String authResult = inFromServer.readLine();

            // password is incorrect
            if (authResult.equals("0")) return false;

                // user already exists or new user
            else if (authResult.equals("1")) {
                // obtain user id from server
                System.out.println("Server response OK: adding course");
            }
            clientSocket.close();
        } catch (ConnectException e) {
            System.out.println("Not found server...");

            return false;
        } catch (IOException e) {
            return false;
        }

        return true;
    }

    private boolean retrieveCourses() {
        retCourseItems = new ArrayList<>();
        try {
            Socket clientSocket = new Socket(ServerUtils.TEMP_IP, ServerUtils.PORT_COURSE);
            DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
            BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String toServer = ServerUtils.TASK_RETRIEVE_COURSES + "\n" + mUser.getId() + "\n";
            outToServer.writeBytes(toServer);
            String authResult = inFromServer.readLine();

            // password is incorrect
            if (authResult.equals("0")) return false;

                // user already exists or new user
            else if (authResult.equals("1")) {
                // obtain user id from server
                System.out.println("Server response OK: getting courses...");
                int numCourses = Integer.parseInt(inFromServer.readLine());
                for (int i = 0; i < numCourses; i++) {
                    String subject = inFromServer.readLine();
                    String course = inFromServer.readLine();
                    String section = inFromServer.readLine();
                    // retrieve from waterloo api course details.
                    CourseItem toAdd = getWaterlooCourse(subject, course, section);
                    if (toAdd != null) {
                        retCourseItems.add(toAdd);
                    } else return false;
                }
            }
            clientSocket.close();
        } catch (ConnectException e) {
            System.out.println("Not found server...");

            return false;
        } catch (IOException e) {
            return false;
        }

        return true;
    }



    CourseItem getWaterlooCourse(String subject, String course, String section) {
        URL url;
        HttpURLConnection connection = null;
        InputStream is = null;
        try {
            url = new URL("https://api.uwaterloo.ca/v2/courses/" + subject + "/" + course + "/schedule.json?key=d580b4f96aa6be2d53e8eb2aeb576c8e");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            is = connection.getInputStream();
            BufferedReader theReader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String str = "";
            String reply;
            while ((reply = theReader.readLine()) != null) {
                str += reply;
            }
            JSONObject obj = new JSONObject(str);
            for (int i = 0; i < obj.getJSONArray("data").length(); i++) {
                JSONObject res = obj.getJSONArray("data").getJSONObject(i);
                String currentSection = (String) res.get("section");
                if (!section.equals(currentSection)) continue;
                System.out.println(section);

                JSONObject schedule = res.getJSONArray("classes").getJSONObject(0);
                System.out.println(schedule.toString());
                JSONObject dates = schedule.getJSONObject("date");
                int startTimeHours = -1;
                int startTimeMins = -1;
                if (!dates.isNull("start_time")) {
                    String startTime = (String) dates.get("start_time");
                    String[] test = startTime.split(":");
                    startTimeHours = Integer.parseInt(test[0]);
                    startTimeMins = Integer.parseInt(test[1]);
                }

                int endTimeHours = -1;
                int endTimeMins = -1;
                if (!dates.isNull("end_time")) {
                    String endTime = (String) dates.get("end_time");
                    String[] endSplit = endTime.split(":");
                    endTimeHours = Integer.parseInt(endSplit[0]);
                    endTimeMins = Integer.parseInt(endSplit[1]);
                }

                Date start = new Date();
                Date end = new Date();
                Calendar calendarStart = Calendar.getInstance();
                Calendar calendarEnd = Calendar.getInstance();
                Calendar calendarNow = Calendar.getInstance();
                calendarStart.set(Calendar.HOUR_OF_DAY, 0);
                calendarEnd.set(Calendar.HOUR_OF_DAY, 0);

                if (!dates.isNull("start_date")) {
                    String[] startDates = ((String) dates.get("start_date")).split("/");
                    calendarStart.set(Calendar.MONTH, Integer.parseInt(startDates[0]));
                    calendarStart.set(Calendar.DAY_OF_MONTH, Integer.parseInt(startDates[1]));
                } else {
                    if (calendarNow.get(Calendar.MONTH) <= 3) {
                        //TODO: hardcoded start/end
                        calendarStart.set(Calendar.MONTH, 0);
                        calendarStart.set(Calendar.DAY_OF_MONTH, 1);
                    }
                }
                start = calendarStart.getTime();
                if (!dates.isNull("end_date")) {
                    String[] endDates = ((String) dates.get("end_date")).split("/");
                    calendarEnd.set(Calendar.MONTH, Integer.parseInt(endDates[0]));
                    calendarEnd.set(Calendar.DAY_OF_MONTH, Integer.parseInt(endDates[1]));
                } else {
                    if (calendarNow.get(Calendar.MONTH) <= 3) {
                        //TODO: hardcoded start/end
                        calendarEnd.set(Calendar.MONTH, 3);
                        calendarEnd.set(Calendar.DAY_OF_MONTH, 10);
                    }
                }
                end = calendarEnd.getTime();

                boolean daysOfWeek[] = new boolean[7];
                if (!dates.isNull("weekdays")) {
                    String weekdays = (String) dates.get("weekdays");
                    if (weekdays.contains("M")) daysOfWeek[1] = true;
                    if (weekdays.contains("T") && (weekdays.indexOf("T") != weekdays.indexOf("Th")))
                        daysOfWeek[2] = true;
                    if (weekdays.contains("W")) daysOfWeek[3] = true;
                    if (weekdays.contains("Th")) daysOfWeek[4] = true;
                    if (weekdays.contains("F")) daysOfWeek[5] = true;
                    if (weekdays.contains("S")) daysOfWeek[6] = true;
                }
                CourseItem courseItem = new CourseItem(subject + " " + course, section, start, end, daysOfWeek, startTimeHours, startTimeMins, endTimeHours, endTimeMins);
                System.out.println("Course: " + courseItem.getId());

                return courseItem;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }
}
