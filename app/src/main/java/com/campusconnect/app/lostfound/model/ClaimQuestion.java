package com.campusconnect.app.lostfound.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class ClaimQuestion implements Serializable {

    @SerializedName("id")
    private int id;

    @SerializedName("question_text")
    private String questionText;

    @SerializedName("correct_answer")
    private String correctAnswer;

    public ClaimQuestion() {
    }

    public ClaimQuestion(String questionText, String correctAnswer) {
        this.questionText = questionText;
        this.correctAnswer = correctAnswer;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }
}
