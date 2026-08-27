package com.example.smaeandrstud3;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.provider.MediaStore;
import java.io.InputStream;
import android.net.Uri;
import android.graphics.BitmapFactory;

public class BusqImg extends AppCompatActivity {

    private static final String TAG = "BusqImg";
    private ImageButton BTomarImg;
    private ImageButton BSubirImg;
    private Button BMostrarTodo;
    private ImageButton BRegresar;
    private ImageView ivCapturedImage;
    private TextView tvResult;
    private RecyclerView rvAlimentosSugeridos;
    
    private Interpreter tflite;
    private List<String> labels;
    private final int imageSize = 224;

    private DatabaseReference databaseReference;
    private AlimentoAdapter adapter;
    private List<Alimento> listaAlimentos;

    private final ActivityResultLauncher<Void> takePictureLauncher = registerForActivityResult(
            new ActivityResultContracts.TakePicturePreview(),
            result -> {
                if (result != null) {
                    ivCapturedImage.setImageBitmap(result);
                    runInference(result);
                }
            }
    );

    private final ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(uri);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        if (bitmap != null) {
                            ivCapturedImage.setImageBitmap(bitmap);
                            runInference(bitmap);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error al cargar imagen de galería", e);
                        Toast.makeText(this, "Error al cargar imagen", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    takePictureLauncher.launch(null);
                } else {
                    Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_busq_img);
        
        BTomarImg = findViewById(R.id.BTomarImg);
        BSubirImg = findViewById(R.id.BSubirImg);
        BMostrarTodo = findViewById(R.id.BMostrarTodo);
        BRegresar = findViewById(R.id.BRegresar);
        ivCapturedImage = findViewById(R.id.ivCapturedImage);
        tvResult = findViewById(R.id.tvResult);
        rvAlimentosSugeridos = findViewById(R.id.rvAlimentosSugeridos);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Configuración RecyclerView
        listaAlimentos = new ArrayList<>();
        adapter = new AlimentoAdapter(listaAlimentos, this::mostrarDetallesAlimento);
        rvAlimentosSugeridos.setLayoutManager(new LinearLayoutManager(this));
        rvAlimentosSugeridos.setAdapter(adapter);

        // Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference("SMAE");

        BTomarImg.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                takePictureLauncher.launch(null);
            } else {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA);
            }
        });

        BSubirImg.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        loadModel();

        BMostrarTodo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buscarEnFirebase("");
            }
        });

        BRegresar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intento = new Intent(BusqImg.this, MainActivity.class);
                startActivity(intento);
            }
        });
    }

    private void loadModel() {
        try {
            MappedByteBuffer tfliteModel = FileUtil.loadMappedFile(this, "model_unquant.tflite");
            tflite = new Interpreter(tfliteModel);
            labels = FileUtil.loadLabels(this, "labels.txt");
            Log.d(TAG, "Modelo y etiquetas cargados exitosamente.");
        } catch (Exception e) {
            Log.e(TAG, "Error al cargar el modelo TFLite", e);
            Toast.makeText(this, "Error al cargar el modelo: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void runInference(Bitmap bitmap) {
        if (tflite == null || labels == null) {
            tvResult.setText("Modelo no cargado.");
            return;
        }

        tvResult.setText("Analizando...");

        ImageProcessor imageProcessor = new ImageProcessor.Builder()
                .add(new ResizeOp(imageSize, imageSize, ResizeOp.ResizeMethod.BILINEAR))
                .add(new NormalizeOp(0f, 255f))
                .build();

        TensorImage tensorImage = new TensorImage(tflite.getInputTensor(0).dataType());
        tensorImage.load(bitmap);
        tensorImage = imageProcessor.process(tensorImage);

        float[][] output = new float[1][labels.size()];
        tflite.run(tensorImage.getBuffer(), output);

        int maxIdx = 0;
        float maxProb = 0;
        for (int i = 0; i < output[0].length; i++) {
            if (output[0][i] > maxProb) {
                maxProb = output[0][i];
                maxIdx = i;
            }
        }

        String labelCompleto = labels.get(maxIdx);
        // Teachable Machine suele poner "0 Nombre", limpiamos el índice
        String labelLimpio = labelCompleto.replaceAll("^\\d+\\s+", "");
        
        String resultText = labelLimpio + " (" + 
                String.format(Locale.getDefault(), "%.1f%%", maxProb * 100) + ")";
        tvResult.setText(resultText);
        
        // Buscar en Firebase
        buscarEnFirebase(labelLimpio);
    }

    private void buscarEnFirebase(String queryText) {
        String busquedaNormalizada = normalizarTexto(queryText);

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
                        if (nombreNormalizado.contains(busquedaNormalizada)) {
                            listaAlimentos.add(alimento);
                        }
                    }
                }
                adapter.notifyDataSetChanged();
                if (listaAlimentos.isEmpty()) {
                    Toast.makeText(BusqImg.this, "No se encontraron recetas para: " + queryText, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error en Firebase", error.toException());
            }
        });
    }

    private String normalizarTexto(String texto) {
        if (texto == null) return "";
        return Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "")
                .toLowerCase();
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

    @Override
    protected void onDestroy() {
        if (tflite != null) {
            tflite.close();
        }
        super.onDestroy();
    }
}