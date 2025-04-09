package com.ayzconsultores.diegoshouse.adapters;

import android.animation.ValueAnimator;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ayzconsultores.diegoshouse.R;
import com.ayzconsultores.diegoshouse.models.FAQModel;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

public class FAQAdapter extends FirestoreRecyclerAdapter<FAQModel, FAQAdapter.ViewHolder> {

    private final Context context; // Contexto de la actividad o fragmento que usa el adaptador

    public FAQAdapter(FirestoreRecyclerOptions<FAQModel> options, Context context) {
        super(options); // Pasa las opciones al constructor de FirestoreRecyclerAdapter
        this.context = context; // Asigna el contexto
    }

    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull FAQModel model) {
        // Configura los datos en el ViewHolder
        holder.questionTextView.setText(model.getTitulo()); // Asigna la pregunta
        holder.answerTextView.setText(model.getDescripcion()); // Asigna la respuesta

        // Configura el estado inicial del ícono
        holder.arrowIcon.setImageResource(holder.isExpanded ? R.drawable.ic_row_up : R.drawable.ic_row_down);

        // Listener para expandir o colapsar la respuesta
        holder.questionTextView.setOnClickListener(v -> {
            if (holder.isExpanded) {
                collapseView(holder.expandableLayout);
                holder.arrowIcon.setImageResource(R.drawable.ic_row_down); // Cambia a ícono de colapsado
            } else {
                expandView(holder.expandableLayout);
                holder.arrowIcon.setImageResource(R.drawable.ic_row_up); // Cambia a ícono de expandido
            }
            holder.isExpanded = !holder.isExpanded; // Cambia el estado expandido
        });
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_faq, parent, false);
        return new ViewHolder(view);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView questionTextView, answerTextView;
        LinearLayout expandableLayout;
        ImageView arrowIcon;
        boolean isExpanded = false; // Para rastrear si el layout está expandido

        public ViewHolder(View view) {
            super(view);
            questionTextView = view.findViewById(R.id.txt_titulo);
            answerTextView = view.findViewById(R.id.txt_description);
            expandableLayout = view.findViewById(R.id.expandableLayout);
            arrowIcon = view.findViewById(R.id.arrowIcon);
        }
    }


    private void expandView(View view) {
        view.setVisibility(View.VISIBLE);
        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int targetHeight = view.getMeasuredHeight(); // Medimos la altura real del contenido

        // Asegurar que la altura se mantiene al expandir
        view.getLayoutParams().height = 1; // Evitar mostrar todo de golpe
        view.requestLayout();

        ValueAnimator animator = ValueAnimator.ofInt(1, targetHeight);
        animator.setDuration(400);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addUpdateListener(animation -> {
            int value = (int) animation.getAnimatedValue();
            view.getLayoutParams().height = value;
            view.requestLayout();
        });

        animator.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                view.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT; // Se mantiene la altura del contenido
                view.requestLayout();
            }
        });

        animator.start();
    }

    private void collapseView(View view) {
        int initialHeight = view.getMeasuredHeight();

        ValueAnimator animator = ValueAnimator.ofInt(initialHeight, 1);
        animator.setDuration(300);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addUpdateListener(animation -> {
            int value = (int) animation.getAnimatedValue();
            view.getLayoutParams().height = value;
            view.requestLayout();
        });

        animator.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                view.setVisibility(View.GONE);
            }
        });

        animator.start();
    }


}
