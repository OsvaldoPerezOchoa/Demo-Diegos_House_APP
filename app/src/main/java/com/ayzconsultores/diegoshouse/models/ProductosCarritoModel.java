package com.ayzconsultores.diegoshouse.models;

// Modelo que representa un producto dentro del carrito de compras
public class ProductosCarritoModel {

    // ID del producto
    private String id_producto;

    // ID del carrito al que pertenece el producto
    private String id_carrito;

    // Cantidad de unidades del producto en el carrito
    private int cantidad;

    // Precio total del producto
    private double precio_total;

    private int puntos_total;

    // Constructor con parámetros para inicializar un producto en el carrito
    public ProductosCarritoModel(String id_producto, String id_carrito, int cantidad, double precio_total, int puntos_total) {
        this.id_producto = id_producto;     // Inicializamos el ID del producto
        this.id_carrito = id_carrito;       // Inicializamos el ID del carrito
        this.cantidad = cantidad;           // Inicializamos la cantidad de producto
        this.precio_total = precio_total;   // Inicializamos el precio total
        this.puntos_total = puntos_total;   // Inicializamos los puntos totales
    }

    // Constructor vacío requerido por Firebase para la deserialización
    public ProductosCarritoModel() {
    }

    // Getters y setters para cada campo

    public String getId_producto() {
        return id_producto;
    }

    public void setId_producto(String id_producto) {
        this.id_producto = id_producto;
    }

    public String getId_carrito() {
        return id_carrito;
    }

    public void setId_carrito(String id_carrito) {
        this.id_carrito = id_carrito;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public double getPrecio_total() {
        return precio_total;
    }

    public void setPrecio_total(double precio_total) {
        this.precio_total = precio_total;
    }

    public int getPuntos_total() {
        return puntos_total;
    }

    public void setPuntos_total(int puntos_total) {
        this.puntos_total = puntos_total;
    }
}
