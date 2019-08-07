package com.yangjian.entity;

import com.yangjian.util.Comment;

import java.io.Serializable;

public class Message implements Serializable {

    private String start;
    private String message;
    private String end;
    private Comment comment;

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public Comment getComment() {
        return comment;
    }

    public void setComment(Comment comment) {
        this.comment = comment;
    }

    @Override
    public String toString() {
        return "Message{" +
                "start='" + start + '\'' +
                ", message='" + message + '\'' +
                ", end='" + end + '\'' +
                ", comment=" + comment +
                '}';
    }
}
