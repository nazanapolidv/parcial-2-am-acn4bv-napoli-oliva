package com.example.parcial_1_am_acn4bv_napoli_oliva;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.text.Editable;
import android.text.TextWatcher;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import androidx.core.content.ContextCompat;
import java.text.NumberFormat;
import java.util.Locale;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.HashMap;
import java.util.Map;

public class DetallePeliculaActivity extends AppCompatActivity {
    // Precio por entrada ($12000)
    private static final double PRECIO_ENTRADA = 12000;
    private EditText inputCantidadEntradas;
    private TextView txtCostoTotal;
    private Button btnConfirmarReserva;
    private Pelicula peliculaActual;
    private FirebaseAuth mAuth;
    private String currentUserId;
    private String favoriteDocId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_pelicula);

        // favoritos
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        if (user == null){
            Toast.makeText(this, "Debe estar logueado para ver detalles.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        currentUserId = user.getUid();

        // variables
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

            // validar si la pelicula ya es favorita
            checkIfFavoriteAndSetButton();

            btnFavorito.setOnClickListener(v -> {
                toggleFavoriteStatus();
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
                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String strCantidad = s.toString();
                    int cantidad = strCantidad.isEmpty() ? 0 : Integer.parseInt(strCantidad);
                    calcularCostoTotal(cantidad);
                }
            });
            btnConfirmarReserva.setOnClickListener(v -> confirmarReserva());

        } else {
            // si la pelicula es nula, se cierra
            Toast.makeText(this, "Error: No se pudo cargar la información de la película.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void guardarEnFirebase(FirebaseFirestore db, Pelicula p) {
        Button btnFavorito = findViewById(R.id.btnFavorito);
        btnFavorito.setText("Guardando...");

        Map<String, Object> data = new HashMap<>();

        // camapos de la pelicula
        data.put("id", p.getId());
        data.put("titulo", p.getTitulo());
        data.put("genero", p.getGenero());
        data.put("anio", p.getAnio());
        data.put("urlImagen", p.getUrlImagen());
        data.put("urlDescripcion", p.getUrlDescripcion());

        // campo para validar
        data.put("userId", currentUserId);

        db.collection("favoritos")
                .add(data) // Se guarda el mapa con todos los datos y el userId
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "¡Agregado a Favoritos!", Toast.LENGTH_SHORT).show();
                    checkIfFavoriteAndSetButton(); // Recarga el estado (botón QUITAR)
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al guardar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    btnFavorito.setEnabled(true);
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

    // Validar si es favorito o no
    private void checkIfFavoriteAndSetButton (){
        if (currentUserId == null || peliculaActual == null) return;

        Button btnFavorito = findViewById(R.id.btnFavorito);
        btnFavorito.setText("Verificando...");
        btnFavorito.setEnabled(false);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // consulta si el id del usuario y el de la pelicula coincidan
        db.collection("favoritos")
                .whereEqualTo("id", peliculaActual.getId())
                .whereEqualTo("userId", currentUserId)
                .get()
                .addOnCompleteListener(task -> {
                    btnFavorito.setEnabled(true);
                    if (task.isSuccessful() && !task.getResult().isEmpty()){
                        // Si la consulta esta ok y esta en favoritos
                        favoriteDocId = task.getResult().getDocuments().get(0).getId();
                        btnFavorito.setText("QUITAR de Favoritos");
                        int colorInteractivo = ContextCompat.getColor(this, R.color.colorInteractivo);
                        btnFavorito.setBackgroundTintList(ColorStateList.valueOf(colorInteractivo));
                    } else {
                        // no es favorito
                        favoriteDocId = null;
                        btnFavorito.setText("AGREGAR a Favoritos");
                        int colorAcento = ContextCompat.getColor(this, R.color.colorAcento);
                        btnFavorito.setBackgroundTintList(ColorStateList.valueOf(colorAcento));
                    }
                });
    }

    // Modificar estado de agregar o quitar de favoritos
    private void toggleFavoriteStatus (){
        if (currentUserId == null || peliculaActual == null) return;

        Button btnFavorito = findViewById(R.id.btnFavorito);
        btnFavorito.setEnabled(false);
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (favoriteDocId != null){
            // la pelicula es favorita
            db.collection("favoritos").document(favoriteDocId)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Eliminado de favoritos.", Toast.LENGTH_SHORT).show();
                        checkIfFavoriteAndSetButton();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error al eliminar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        btnFavorito.setEnabled(true);
                    });
        } else {
            // no es favorita
            guardarEnFirebase(db, peliculaActual);
        }
    }
}