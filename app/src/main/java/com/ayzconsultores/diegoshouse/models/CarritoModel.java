package com.ayzconsultores.diegoshouse.models;

// Importamos la clase Timestamp de Firebase para manejar fechas y horas
import com.google.firebase.Timestamp;

// Modelo que representa un carrito en la aplicación
public class CarritoModel {

    // Campo que indica el estado del carrito (ejemplo: "activo", "finalizado")
    private String estado;

    // ID del usuario asociado al carrito
    private String id_usuario;

    // Total acumulado en el carrito (por ejemplo, el precio total de los productos)
    private double total;

    // Fecha de creación del carrito
    private Timestamp fecha_creacion;

    // Fecha de creación del carrito
    private Timestamp fecha_compra;

    // Puntos asociados al carrito (pueden ser usados en un sistema de recompensas)
    private int puntos;

    private int puntos_usados;

    private String nombre_usuario;

    // Constructor vacío requerido por Firebase y otros frameworks de serialización
    public CarritoModel() {
    }

    // Constructor con parámetros para inicializar un carrito
    public CarritoModel(String estado, String id_usuario, double total, Timestamp fecha_creacion, int puntos, String nombre_usuario, Timestamp fecha_compra, int puntos_usados) {
        this.estado = estado;               // Inicializamos el estado del carrito
        this.id_usuario = id_usuario;       // Asignamos el ID del usuario
        this.total = total;                 // Asignamos el total del carrito
        this.fecha_creacion = fecha_creacion; // Establecemos la fecha de creación
        this.puntos = puntos;               // Establecemos los puntos asociados
        this.nombre_usuario = nombre_usuario; // Asignamos el nombre del usuario asociado al carrito
        this.fecha_compra = fecha_compra; // Establecemos la fecha de compra
        this.puntos_usados = puntos_usados; // Establecemos los puntos usados
    }

    // Getter y setter para el campo "estado"
    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    // Getter y setter para el campo "id_usuario"
    public String getId_usuario() {
        return id_usuario;
    }

    public void setId_usuario(String id_usuario) {
        this.id_usuario = id_usuario;
    }

    // Getter y setter para el campo "total"
    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    // Getter y setter para el campo "fecha_creacion"
    public Timestamp getFecha_creacion() {
        return fecha_creacion;
    }

    public void setFecha_creacion(Timestamp fecha_creacion) {
        this.fecha_creacion = fecha_creacion;
    }

    // Getter y setter para el campo "puntos"
    public int getPuntos() {
        return puntos;
    }

    public void setPuntos(int puntos) {
        this.puntos = puntos;
    }

    // Getter y setter para el campo "nombre_usuario"
    public String getNombre_usuario() {
        return nombre_usuario;
    }

    public void setNombre_usuario(String nombre_usuario) {
        this.nombre_usuario = nombre_usuario;
    }

    // Getter y setter para el campo "fecha_compra"
    public Timestamp getFecha_compra() {
        return fecha_compra;
    }

    public void setFecha_compra(Timestamp fecha_compra) {
        this.fecha_compra = fecha_compra;
    }

    // Getter y setter para el campo "puntos_usados"
    public int getPuntos_usados() {
        return puntos_usados;
    }

    public void setPuntos_usados(int puntos_usados) {
        this.puntos_usados = puntos_usados;
    }

}
