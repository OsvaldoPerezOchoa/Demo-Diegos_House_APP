package com.ayzconsultores.diegoshouse.models;

public class FAQModel {

    private String titulo;
    private String descripcion;

    public FAQModel() {
    }

    public FAQModel(String titulo, String descripcion) {
        this.titulo = titulo;
        this.descripcion = descripcion;
    }

    //Getter y setter para el campo "titulo"
    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    // Getter y setter para el campo "descripcion"
    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
