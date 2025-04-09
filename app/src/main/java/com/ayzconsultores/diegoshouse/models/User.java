package com.ayzconsultores.diegoshouse.models;

// Modelo que representa a un usuario en el sistema
public class User {

    // Atributo que almacena el ID único del usuario
    private String id;

    // Atributo que almacena el nombre del usuario
    private String nombre;

    // Atributo que almacena el correo electrónico del usuario
    private String email;

    // Atributo que almacena el número de teléfono del usuario
    private String telefono;

    private  int puntos;

    // Constructor vacío requerido por Firebase para la deserialización
    public User(){

    }

    // Constructor con parámetros para inicializar un objeto User con los valores dados
    public User(String id, String nombre, String email, String telefono, int puntos) {
        this.id = id;              // Inicializa el ID del usuario
        this.nombre = nombre;      // Inicializa el nombre del usuario
        this.email = email;        // Inicializa el correo electrónico del usuario
        this.telefono = telefono;  // Inicializa el teléfono del usuario
        this.puntos = puntos;
    }

    // Getter y Setter para el atributo "id"
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    // Getter y Setter para el atributo "nombre"
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    // Getter y Setter para el atributo "email"
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    // Getter y Setter para el atributo "telefono"
    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public int getPuntos() {
        return puntos;
    }

    public void setPuntos(int puntos) {
        this.puntos = puntos;
    }
}
