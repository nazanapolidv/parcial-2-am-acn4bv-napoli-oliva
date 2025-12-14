package com.example.parcial_1_am_acn4bv_napoli_oliva;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.bumptech.glide.Glide;

import android.text.Editable;
import android.text.TextWatcher;
import java.text.NumberFormat;
import java.util.Locale;

public class DetallePeliculaActivity extends AppCompatActivity {
    // Precio por entrada ($12000)
    private static final double PRECIO_ENTRADA = 12000;
    private EditText inputCantidadEntradas;
    private TextView txtCostoTotal;
    private Button btnConfirmarReserva;
    private Pelicula peliculaActual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_pelicula);

        TextView txtTitulo = findViewById(R.id.detalleTitulo);
        TextView txtDatos = findViewById(R.id.detalleDatos);
        TextView txtDescripcion = findViewById(R.id.detalleDescripcion);
        ImageView imgPoster = findViewById(R.id.detalleImagen);
        Button btnVolver = findViewById(R.id.btnVolver);
        Button btnFavorito = findViewById(R.id.btnFavorito);
        peliculaActual = (Pelicula) getIntent().getSerializableExtra("PELICULA_SELECCIONADA");

        if (peliculaActual != null) {

            txtTitulo.setText(peliculaActual.getTitulo());
            txtDatos.setText(peliculaActual.getGenero() + " (" + peliculaActual.getAnio() + ")");

            Glide.with(this)
                    .load(peliculaActual.getUrlImagen())
                    .into(imgPoster);

            // urlDescripcion de Firebase
            txtDescripcion.setText(peliculaActual.getUrlDescripcion());

            btnVolver.setOnClickListener(v -> finish());

            // Agregar a favoritos com validacion
            btnFavorito.setOnClickListener(v -> {
                btnFavorito.setEnabled(false);
                btnFavorito.setText("Verificando...");

                FirebaseFirestore db = FirebaseFirestore.getInstance();

                // Ahora se valida si existe esa pelicula en la lista con id
                db.collection("favoritos")
                        .whereEqualTo("id", peliculaActual.getId())
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                if (!task.getResult().isEmpty()) {
                                    Toast.makeText(this, "Ya agregaste esta película", Toast.LENGTH_SHORT).show();
                                    btnFavorito.setText("Ya agregada");
                                } else {
                                    guardarEnFirebase(db, peliculaActual, btnFavorito);
                                }
                            } else {
                                Toast.makeText(this, "Error al verificar", Toast.LENGTH_SHORT).show();
                                btnFavorito.setEnabled(true);
                                btnFavorito.setText("Reintentar");
                            }
                        });
            });

            // Reservar entradas
            inputCantidadEntradas = findViewById(R.id.inputCantidadEntradas);
            txtCostoTotal = findViewById(R.id.txtCostoTotal);
            btnConfirmarReserva = findViewById(R.id.btnConfirmarReserva);

            // calculo de entrada, por defecto 1
            calcularCostoTotal(1);

            // listener para el calculo dinamico
            inputCantidadEntradas.addTextChangedListener(new TextWatcher() {
                @Override
                public void afterTextChanged(Editable s) { }
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    String strCantidad = s.toString();
                    int cantidad = strCantidad.isEmpty() ? 0 : Integer.parseInt(strCantidad);
                    calcularCostoTotal(cantidad);
                }
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) { }
            });
            btnConfirmarReserva.setOnClickListener(v -> confirmarReserva());

        } else {
            // si la pelicula es nula, se cierra
            Toast.makeText(this, "Error: No se pudo cargar la información de la película.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void guardarEnFirebase(FirebaseFirestore db, Pelicula p, Button btn) {
        btn.setText("Guardando...");
        db.collection("favoritos")
                .add(p)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "¡Tus favoritos se actualizaron!", Toast.LENGTH_SHORT).show();
                    btn.setText("Agregado a Favoritos");
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    btn.setEnabled(true);
                    btn.setText("Intentar de nuevo");
                });
    }

    // calcular valor total de la reserva
    private void calcularCostoTotal (int cantidad){
        if (cantidad < 0 || cantidad > 99){
            cantidad = 0;
        }

        double costoTotal = cantidad * PRECIO_ENTRADA;

        // formato de la moneda
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "AR"));
        String costoFormateado = currencyFormat.format(costoTotal);

        txtCostoTotal.setText("Costo Total: " + costoFormateado);
    }

    private void confirmarReserva (){
        String strCantidad = inputCantidadEntradas.getText().toString();
        int cantidad = strCantidad.isEmpty() ? 0 : Integer.parseInt(strCantidad);

        if (cantidad <= 0) {
            Toast.makeText(this, "Selecciona al menos una entrada.", Toast.LENGTH_SHORT).show();
            return;
        }

        // costo total del TextView
        String costoTotal = txtCostoTotal.getText().toString().replace("Costo Total: ", "");

        String mensaje = "¡Reserva confirmada! Película: '" + peliculaActual.getTitulo() + "' (" + cantidad + " entradas). Total a pagar: " + costoTotal;

        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
    }
}