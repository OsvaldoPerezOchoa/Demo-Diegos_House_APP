package com.ayzconsultores.diegoshouse.models;

// Importamos la clase Timestamp de Firebase para manejar fechas y horas
import com.google.firebase.Timestamp;

// Modelo que representa un comentario asociado a un producto
public class ComentarioModel {

    // ID del usuario que realiza el comentario
    private String id_usuario;

    // ID del producto al que se refiere el comentario
    private String id_producto;

    // Contenido del comentario
    private String comentario;

    // Calificación asignada al producto (puede ser un valor entre 1 y 5, por ejemplo)
    private float calificacion;

    // Fecha y hora en que se realizó el comentario
    Timestamp fecha_comentario;

    // Constructor vacío requerido por Firebase y otros frameworks de serialización
    public ComentarioModel() {
    }

    // Constructor con parámetros para inicializar un comentario
    public ComentarioModel(String id_usuario, String id_producto, String comentario, float calificacion, Timestamp fecha_comentario) {
        this.id_usuario = id_usuario;                // Inicializamos el ID del usuario
        this.id_producto = id_producto;             // Inicializamos el ID del producto
        this.comentario = comentario;               // Asignamos el contenido del comentario
        this.calificacion = calificacion;           // Establecemos la calificación del producto
        this.fecha_comentario = fecha_comentario;   // Establecemos la fecha y hora del comentario
    }

    // Getter y setter para el campo "id_usuario"
    public String getId_usuario() {
        return id_usuario;
    }

    public void setId_usuario(String id_usuario) {
        this.id_usuario = id_usuario;
    }

    // Getter y setter para el campo "id_producto"
    public String getId_producto() {
        return id_producto;
    }

    public void setId_producto(String id_producto) {
        this.id_producto = id_producto;
    }

    // Getter y setter para el campo "comentario"
    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    // Getter y setter para el campo "calificacion"
    public float getCalificacion() {
        return calificacion;
    }

    public void setCalificacion(float calificacion) {
        this.calificacion = calificacion;
    }

    // Getter y setter para el campo "fecha_comentario"
    public Timestamp getFecha_comentario() {
        return fecha_comentario;
    }

    public void setFecha_comentario(Timestamp fecha_comentario) {
        this.fecha_comentario = fecha_comentario;
    }
}
