package com.da_grupo9.ronda.data.model;

/** Cuerpo enviado al crear una pregunta sobre una publicación. */
public class QuestionRequest {
    private final String text;

    public QuestionRequest(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
