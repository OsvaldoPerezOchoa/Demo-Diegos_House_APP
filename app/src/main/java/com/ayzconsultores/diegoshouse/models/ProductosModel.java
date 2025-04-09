package com.ayzconsultores.diegoshouse.models;

// Modelo que representa un producto en la tienda
public class ProductosModel {

    // Atributo que almacena la URL de la imagen del producto
    private String imagen;

    // Atributo que almacena el precio del producto (ahora es un Double para representar valores decimales correctamente)
    private Double precio;

    // Atributo que almacena una descripción del producto
    private String descripcion;

    // Atributo que almacena el nombre del producto
    private String nombre;

    // Atributo que almacena la categoría a la que pertenece el producto
    private String categoria;

    // Atributo que almacena el estado del producto
    private String estado;

    // Atributo que almacena el estado del producto
    private Double puntos;

    // Constructor vacío requerido por Firebase para la deserialización
    public ProductosModel() {

    }

    // Constructor con parámetros para inicializar un producto con los valores dados
    public ProductosModel(String imagen, Double precio, String descripcion, String nombre, String categoria, String estado, Double puntos) {
        this.imagen = imagen;         // Inicializa la imagen del producto
        this.precio = precio;         // Inicializa el precio del producto (ahora es un Double)
        this.descripcion = descripcion;  // Inicializa la descripción del producto
        this.nombre = nombre;         // Inicializa el nombre del producto
        this.categoria = categoria;   // Inicializa la categoría del producto
        this.estado = estado;         // Inicializa el estado del producto
        this.puntos = puntos;         // Inicializa los puntos del producto
    }

    // Getter y Setter para el atributo "estado"
    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    // Getter y Setter para el atributo "imagen"
    public String getImagen() {
        return imagen;
    }


    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    // Getter y Setter para el atributo "precio"
    public Double getPrecio() {
        return precio;
    }

    public void setPrecio(Double precio) {
        this.precio = precio;
    }

    // Getter y Setter para el atributo "descripcion"
    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    // Getter y Setter para el atributo "nombre"
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    // Getter y Setter para el atributo "categoria"
    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    // Getter y Setter para el atributo "puntos"
    public Double getPuntos() {
        return puntos;
    }

    public void setPuntos(Double puntos) {
        this.puntos = puntos;
    }

}
