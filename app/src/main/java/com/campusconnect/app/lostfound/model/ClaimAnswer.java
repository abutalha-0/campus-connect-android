package com.campusconnect.app.lostfound.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class ClaimAnswer implements Serializable {

    @SerializedName("id")
    private int id;

    @SerializedName("question")
    private int questionId;

    @SerializedName("question_text")
    private String questionText;

    @SerializedName("answer_text")
    private String answerText;

    public ClaimAnswer() {
    }

    public ClaimAnswer(int questionId, String answerText) {
        this.questionId = questionId;
        this.answerText = answerText;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getQuestionId() {
        return questionId;
    }

    public void setQuestionId(int questionId) {
        this.questionId = questionId;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public String getAnswerText() {
        return answerText;
    }

    public void setAnswerText(String answerText) {
        this.answerText = answerText;
    }
}
