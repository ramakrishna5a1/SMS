package com.messages.smsmessagesimporter.Utils;

public class SmsEntity {
    private String address;
    private String body;
    private long date;
    private String dateString;

    public SmsEntity(String address, String msg, long date, String dateString) {
        this.address = address;
        this.date = date;
        this.body = msg;
        this.dateString = dateString;
    }

    public String getAddress() {
        return address;
    }

    public long getDate() {
        return date;
    }

    public String getBody() {
        return body;
    }

    public String getDateString() {
        return dateString;
    }

    public void show() {
        System.out.println(address);
        System.out.println(body);
    }
}
