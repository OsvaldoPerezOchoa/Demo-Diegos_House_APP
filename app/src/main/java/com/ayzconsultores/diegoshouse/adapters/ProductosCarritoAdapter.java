package com.ayzconsultores.diegoshouse.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.ayzconsultores.diegoshouse.R;
import com.ayzconsultores.diegoshouse.models.ProductosCarritoModel;
import com.ayzconsultores.diegoshouse.providers.AgregarProductosProvider;
import com.ayzconsultores.diegoshouse.providers.ProductosProvider;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.squareup.picasso.Picasso;

import java.util.Locale;

// Adaptador para mostrar los productos en el carrito de compras
public class ProductosCarritoAdapter extends FirestoreRecyclerAdapter<ProductosCarritoModel, ProductosCarritoAdapter.ViewHolder> {

    private final Context context;  // Contexto de la actividad o fragmento
    private final ProductosProvider productosProvider;  // Proveedor para obtener los detalles de los productos
    private final AgregarProductosProvider agregarProductosProvider;  // Proveedor para gestionar la eliminación de productos en el carrito

    // Constructor del adaptador, recibe las opciones de Firestore y el contexto
    public ProductosCarritoAdapter(FirestoreRecyclerOptions<ProductosCarritoModel> options, Context context) {
        super(options);  // Pasa las opciones a la clase base de FirestoreRecyclerAdapter
        this.context = context;  // Inicializa el contexto
        this.productosProvider = new ProductosProvider();  // Inicializa el proveedor de productos
        this.agregarProductosProvider = new AgregarProductosProvider();  // Inicializa el proveedor para agregar/eliminar productos
    }

    // Método para vincular los datos del modelo al ViewHolder en cada fila de la lista
    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull ProductosCarritoModel model) {
        String cantidadTexto = String.valueOf(model.getCantidad());
        String id_producto = model.getId_producto();
        holder.textViewCantidad.setText(cantidadTexto);

        // Agregar SnapshotListener para escuchar cambios en el producto
        productosProvider.getProductoTiemporeal(id_producto)
                .addSnapshotListener((documentSnapshot, e) -> {
                    if (e != null) {
                        Log.e("Firestore", "Error al obtener los datos del producto", e);
                        return;
                    }

                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        String nombre = documentSnapshot.getString("nombre");
                        Double precio = documentSnapshot.getDouble("precio");
                        Integer puntos = documentSnapshot.getLong("puntos") != null ? documentSnapshot.getLong("puntos").intValue() : 0;
                        String imagenUrl = documentSnapshot.getString("imagen");

                        holder.textViewNombre.setText(nombre);

                        // Determinar si se usa puntos o precio
                        boolean usaPuntos = puntos > 0;
                        if (usaPuntos) {
                            holder.textViewPrecio.setText(String.format(Locale.getDefault(), "%d puntos", puntos));
                        } else {
                            holder.textViewPrecio.setText(String.format(new Locale("es", "MX"), "$%.2f", precio != null ? precio : 0.0));
                        }

                        if (imagenUrl != null && !imagenUrl.isEmpty()) {
                            Picasso.get().load(imagenUrl).into(holder.imageViewProducto);
                        }

                        // Recalcular el total según se usen puntos o precio
                        if (usaPuntos) {
                            int puntosTotal = model.getCantidad() * puntos;
                            holder.textViewTotal.setText(String.format(Locale.getDefault(), "%d puntos", puntosTotal));
                        } else {
                            double precioTotal = model.getCantidad() * (precio != null ? precio : 0.0);
                            holder.textViewTotal.setText(String.format(new Locale("es", "MX"), "$%.2f", precioTotal));
                        }

                        // Configurar los botones para aumentar y disminuir la cantidad
                        holder.btn_plus.setOnClickListener(v -> {
                            int cantidad = model.getCantidad() + 1;
                            model.setCantidad(cantidad);

                            if (usaPuntos) {
                                int puntosTotal = cantidad * puntos;
                                actualizarCantidadEnFirestore(position, cantidad, puntos, true);
                                holder.textViewTotal.setText(String.format(Locale.getDefault(), "%d puntos", puntosTotal));
                            } else {
                                double precioTotal = cantidad * (precio != null ? precio : 0.0);
                                actualizarCantidadEnFirestore(position, cantidad, precio, false);
                                holder.textViewTotal.setText(String.format(new Locale("es", "MX"), "$%.2f", precioTotal));
                            }
                            holder.textViewCantidad.setText(String.valueOf(cantidad));
                        });

                        holder.btn_minus.setOnClickListener(v -> {
                            int cantidad = model.getCantidad() - 1;
                            if (cantidad > 0) {
                                model.setCantidad(cantidad);

                                if (usaPuntos) {
                                    int puntosTotal = cantidad * puntos;
                                    actualizarCantidadEnFirestore(position, cantidad, puntos, true);
                                    holder.textViewTotal.setText(String.format(Locale.getDefault(), "%d puntos", puntosTotal));
                                } else {
                                    double precioTotal = cantidad * (precio != null ? precio : 0.0);
                                    actualizarCantidadEnFirestore(position, cantidad, precio, false);
                                    holder.textViewTotal.setText(String.format(new Locale("es", "MX"), "$%.2f", precioTotal));
                                }
                                holder.textViewCantidad.setText(String.valueOf(cantidad));
                            } else {
                                confirmarEliminacionProducto(model, position);
                            }
                        });
                    }
                });
    }




    // Actualizar cantidad en Firestore
    private void actualizarCantidadEnFirestore(int position, int cantidad, double valor, boolean esPuntos) {
        DocumentSnapshot snapshot = getSnapshots().getSnapshot(position);

        if (esPuntos) {
            int puntosTotal = (int) (cantidad * valor);
            snapshot.getReference().update("cantidad", cantidad, "puntos_total", puntosTotal)
                    .addOnSuccessListener(aVoid -> Log.d("Firestore", "Actualización exitosa: cantidad=" + cantidad + ", puntos_total=" + puntosTotal))
                    .addOnFailureListener(e -> Log.e("Firestore", "Error al actualizar: " + e.getMessage()));
        } else {
            double precioTotal = cantidad * valor;
            snapshot.getReference().update("cantidad", cantidad, "precio_total", precioTotal)
                    .addOnSuccessListener(aVoid -> Log.d("Firestore", "Actualización exitosa: cantidad=" + cantidad + ", precio_total=" + precioTotal))
                    .addOnFailureListener(e -> Log.e("Firestore", "Error al actualizar: " + e.getMessage()));
        }
    }


    // Método para confirmar la eliminación de un producto del carrito
    private void confirmarEliminacionProducto(ProductosCarritoModel model, int position) {
        new AlertDialog.Builder(context)  // Crea una alerta de confirmación
                .setTitle("Eliminar producto")  // Título de la alerta
                .setMessage("¿Estás seguro de que deseas eliminar este producto del carrito?")  // Mensaje de la alerta
                .setPositiveButton("Sí", (dialog, id) -> agregarProductosProvider.BorrarProducto(model.getId_producto(), model.getId_carrito())  // Si se confirma, se elimina el producto
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(context, "Producto eliminado", Toast.LENGTH_SHORT).show();  // Muestra un mensaje de éxito
                            notifyItemRemoved(position);  // Elimina el item del RecyclerView
                            notifyItemRangeChanged(position, getItemCount());  // Notifica los cambios en la lista
                        })
                        .addOnFailureListener(e -> Toast.makeText(context, "Error al eliminar el producto", Toast.LENGTH_SHORT).show()))  // Mensaje en caso de error
                .setNegativeButton("No", null)  // Si no se confirma, no hace nada
                .show();  // Muestra la alerta
    }

    // Método para crear un ViewHolder a partir de una vista inflada
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflamos el layout 'cardview_carrito' para cada item en el RecyclerView
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_carrito, parent, false);
        return new ViewHolder(view);  // Devuelve el ViewHolder con la vista inflada
    }

    // Clase ViewHolder que contiene las vistas de cada elemento del carrito
    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView textViewNombre, textViewPrecio, textViewCantidad, textViewTotal;
        ImageView imageViewProducto;
        ImageButton btn_plus, btn_minus;

        // Constructor del ViewHolder, enlaza las vistas
        public ViewHolder(View view) {
            super(view);
            textViewNombre = view.findViewById(R.id.txt_nombre);
            textViewPrecio = view.findViewById(R.id.txt_precio);
            textViewCantidad = view.findViewById(R.id.txt_count);
            textViewTotal = view.findViewById(R.id.txt_preciototal);
            btn_plus = view.findViewById(R.id.img_plus);
            btn_minus = view.findViewById(R.id.img_minus);
            imageViewProducto = view.findViewById(R.id.img_producto);
        }
    }
}
