package com.example.smaeandrstud3;

import com.google.firebase.database.PropertyName;

public class Alimento {
    private String nombre;
    private String cantidad;
    private String unidad;
    private String pesoBruto;
    private String pesoNeto;
    private String energia;
    private String proteina;
    private String lipidos;
    private String hidratosDeCarbono;
    private String cargaGlicemica;

    public Alimento() {}

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    @PropertyName("Cantidad sugerida")
    public String getCantidad() { return cantidad; }
    @PropertyName("Cantidad sugerida")
    public void setCantidad(String cantidad) { this.cantidad = cantidad; }

    @PropertyName("Unidad")
    public String getUnidad() { return unidad; }
    @PropertyName("Unidad")
    public void setUnidad(String unidad) { this.unidad = unidad; }

    @PropertyName("Peso bruto (g)")
    public String getPesoBruto() { return pesoBruto; }
    @PropertyName("Peso bruto (g)")
    public void setPesoBruto(String pesoBruto) { this.pesoBruto = pesoBruto; }

    @PropertyName("Peso neto (g)")
    public String getPesoNeto() { return pesoNeto; }
    @PropertyName("Peso neto (g)")
    public void setPesoNeto(String pesoNeto) { this.pesoNeto = pesoNeto; }

    @PropertyName("Energia (kcal)")
    public String getEnergia() { return energia; }
    @PropertyName("Energia (kcal)")
    public void setEnergia(String energia) { this.energia = energia; }

    @PropertyName("Proteina (g)")
    public String getProteina() { return proteina; }
    @PropertyName("Proteina (g)")
    public void setProteina(String proteina) { this.proteina = proteina; }

    @PropertyName("Lipidos (g)")
    public String getLipidos() { return lipidos; }
    @PropertyName("Lipidos (g)")
    public void setLipidos(String lipidos) { this.lipidos = lipidos; }

    @PropertyName("Hidratos de carbono (g)")
    public String getHidratosDeCarbono() { return hidratosDeCarbono; }
    @PropertyName("Hidratos de carbono (g)")
    public void setHidratosDeCarbono(String hidratosDeCarbono) { this.hidratosDeCarbono = hidratosDeCarbono; }

    @PropertyName("Carga glicemica")
    public String getCargaGlicemica() { return cargaGlicemica; }
    @PropertyName("Carga glicemica")
    public void setCargaGlicemica(String cargaGlicemica) { this.cargaGlicemica = cargaGlicemica; }

}
