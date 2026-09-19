package com.da_grupo9.ronda.data.model;

/** Cuerpo enviado al responder una pregunta recibida. */
public class AnswerQuestionRequest {
    private final String answer;

    public AnswerQuestionRequest(String answer) {
        this.answer = answer;
    }

    public String getAnswer() {
        return answer;
    }
}
