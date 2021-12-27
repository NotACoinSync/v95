package server;

import java.util.Calendar;

import client.inventory.Item;

public class DueyPackages {

    private String sender = null;
    private Item item = null;
    private int mesos = 0;
    private int day;
    private int month;
    private int year;
    private int packageId = 0;

    public DueyPackages(int pId, Item item) {
        this.item = item;
        packageId = pId;
    }

    public DueyPackages(int pId) { // Meso only package.
        this.packageId = pId;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String name) {
        sender = name;
    }

    public Item getItem() {
        return item;
    }

    public int getMesos() {
        return mesos;
    }

    public void setMesos(int set) {
        mesos = set;
    }

    public int getPackageId() {
        return packageId;
    }

    public long sentTimeInMilliseconds() {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, day);
        return cal.getTimeInMillis();
    }

    public void setSentTime(String sentTime) {
        day = Integer.parseInt(sentTime.substring(0, 2));
        month = Integer.parseInt(sentTime.substring(3, 5));
        year = Integer.parseInt(sentTime.substring(6, 10));
    }
}
