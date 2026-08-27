package com.example.smaeandrstud3;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

public class BusqManual extends AppCompatActivity {

    private EditText TText;
    private Button BBuscar;
    private Button BMostrarTodo;
    private ImageButton BRegresar;
    private RecyclerView RWResultado;
    private DatabaseReference databaseReference;
    private AlimentoAdapter adapter;
    private List<Alimento> listaAlimentos;

    // Método para quitar acentos y convertir a minúsculas
    private String normalizarTexto(String texto) {
        if (texto == null) return "";
        return Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "")
                .toLowerCase();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_busq_manual);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        TText = findViewById(R.id.TText);
        BBuscar = findViewById(R.id.BBuscar);
        RWResultado = findViewById(R.id.RWResultado);
        BMostrarTodo = findViewById(R.id.BMostrarTodo);
        BRegresar = findViewById(R.id.BRegresar);

        listaAlimentos = new ArrayList<>();
        adapter = new AlimentoAdapter(listaAlimentos, new AlimentoAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Alimento alimento) {
                mostrarDetallesAlimento(alimento);
            }
        });
        RWResultado.setLayoutManager(new LinearLayoutManager(this));
        RWResultado.setAdapter(adapter);

        databaseReference = FirebaseDatabase.getInstance().getReference("SMAE");

        // Carga inicial de todos los datos
        realizarBusqueda("");

        BMostrarTodo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                realizarBusqueda("");
            }
        });

        BBuscar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String textoABuscar = TText.getText().toString().trim();
                realizarBusqueda(textoABuscar);
            }
        });

        BRegresar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intento = new Intent(BusqManual.this, MainActivity.class);
                startActivity(intento);
            }
        });
    }

    private void realizarBusqueda(String textoABuscar) {
        String busquedaNormalizada = normalizarTexto(textoABuscar);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listaAlimentos.clear();
                for (DataSnapshot alimentoSnapshot : snapshot.getChildren()) {
                    Alimento alimento = alimentoSnapshot.getValue(Alimento.class);
                    if (alimento != null) {
                        if (alimento.getNombre() == null) {
                            alimento.setNombre(alimentoSnapshot.getKey());
                        }

                        String nombreNormalizado = normalizarTexto(alimento.getNombre());

                        if (busquedaNormalizada.isEmpty() || nombreNormalizado.contains(busquedaNormalizada)) {
                            listaAlimentos.add(alimento);
                        }
                    }
                }
                adapter.notifyDataSetChanged();
                Log.d("Firebase", "Resultados: " + listaAlimentos.size());

                if (listaAlimentos.isEmpty() && !textoABuscar.isEmpty()) {
                    Toast.makeText(BusqManual.this, "No se encontraron resultados para: " + textoABuscar, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("Firebase", "Error al filtrar datos", error.toException());
            }
        });
    }

    private void mostrarDetallesAlimento(Alimento alimento) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(alimento.getNombre());
        
        String detalles = "Cantidad: " + alimento.getCantidad() + "\n" +
                          "Unidad: " + alimento.getUnidad() + "\n" +
                          "Peso bruto: " + alimento.getPesoBruto() + " g\n" +
                          "Peso neto: " + alimento.getPesoNeto() + " g\n" +
                          "Energía: " + alimento.getEnergia() + " kcal\n" +
                          "Proteína: " + alimento.getProteina() + " g\n" +
                          "Lípidos: " + alimento.getLipidos() + " g\n" +
                          "Hidratos de carbono: " + alimento.getHidratosDeCarbono() + " g\n" +
                          "Carga glicémica: " + alimento.getCargaGlicemica();
        
        builder.setMessage(detalles);
        builder.setPositiveButton("Cerrar", null);
        builder.show();
    }
}
