package com.da_grupo9.ronda;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PublicacionRepository {

    private static List<Publicacion> publicaciones;

    public static List<Publicacion> getPublicaciones() {
        if (publicaciones == null) {
            inicializarDatos();
        }
        return publicaciones;
    }

    public static Publicacion getPublicacionById(int id) {
        for (Publicacion p : getPublicaciones()) {
            if (p.getId() == id) {
                return p;
            }
        }
        return null;
    }

    public static void agregarPublicacion(Publicacion publicacion) {
        getPublicaciones().add(0, publicacion);
    }

    public static int getProximoId() {
        int idMaximo = 0;

        for (Publicacion publicacion : getPublicaciones()) {
            if (publicacion.getId() > idMaximo) {
                idMaximo = publicacion.getId();
            }
        }

        return idMaximo + 1;
    }

    public static List<Publicacion> getPublicacionesPorVendedor(String email) {
        List<Publicacion> publicacionesDelVendedor = new ArrayList<>();

        for (Publicacion publicacion : getPublicaciones()) {
            if (publicacion.getVendedorEmail().equalsIgnoreCase(email)) {
                publicacionesDelVendedor.add(publicacion);
            }
        }

        return publicacionesDelVendedor;
    }

    public static void cambiarEstadoPublicacion(int id, String nuevoEstado) {
        Publicacion publicacion = getPublicacionById(id);

        if (publicacion != null) {
            publicacion.setEstadoPublicacion(nuevoEstado);
        }
    }

    private static void inicializarDatos() {
        publicaciones = new ArrayList<>();

        publicaciones.add(new Publicacion(
                1,
                "iPhone 15 128GB",
                "iPhone 15 en excelente estado, batería al 95%. Incluye caja original y cable de carga. Libre de iCloud y listo para usar con cualquier compañía.",
                850000,
                "Como nuevo",
                "Tecnología",
                "Palermo",
                5,
                "20/08/2026",
                "Juan Pérez",
                "juan@ronda.com",
                "★ 4.9 (42 ventas)",
                new ArrayList<>(Arrays.asList("📱 Foto Principal - iPhone 15", "📷 Vista Trasera / Cámaras", "🔋 Estado de Batería (95%)"))
        ));

        publicaciones.add(new Publicacion(
                2,
                "Bicicleta Mountain Bike",
                "Bicicleta rodado 29 con cuadro de aluminio, cambios Shimano de 21 velocidades, frenos a disco mecánico. Muy cuidada, ideal para ciudad y paseos de montaña.",
                350000,
                "Usado",
                "Deportes",
                "Belgrano",
                4,
                "18/08/2026",
                "Martina Gómez",
                "martina@ronda.com",
                "★ 4.7 (15 ventas)",
                new ArrayList<>(Arrays.asList("🚲 Cuadro completo R29", "⚙️ Transmisión Shimano", "🛑 Frenos a disco"))
        ));

        publicaciones.add(new Publicacion(
                3,
                "PlayStation 5",
                "Consola PS5 versión con lector de disco, 825GB SSD. Incluye 1 joystick DualSense original blanco y todos los cables. Excelente estado.",
                900000,
                "Como nuevo",
                "Tecnología",
                "Caballito",
                3,
                "16/08/2026",
                "Lucas Rossi",
                "lucas@ronda.com",
                "★ 5.0 (28 ventas)",
                new ArrayList<>(Arrays.asList("🎮 Consola PS5 y caja", "🕹️ Joystick DualSense", "🔌 Cables y base"))
        ));

        publicaciones.add(new Publicacion(
                4,
                "Monitor Samsung 24 pulgadas",
                "Monitor Full HD IPS de 24 pulgadas, tasa de refresco 75Hz, puertos HDMI y VGA. Sin píxeles quemados, se entrega con su fuente original.",
                250000,
                "Usado",
                "Tecnología",
                "Recoleta",
                2,
                "12/08/2026",
                "Camila Díaz",
                "camila@ronda.com",
                "★ 4.6 (9 ventas)",
                new ArrayList<>(Arrays.asList("🖥️ Pantalla encendida", "🔌 Puertos traseros HDMI"))
        ));

        publicaciones.add(new Publicacion(
                5,
                "Teclado mecánico Logitech",
                "Teclado mecánico switches Blue, retroiluminación RGB configurable, distribución en español con letra Ñ. Caja sellada sin abrir.",
                120000,
                "Nuevo",
                "Tecnología",
                "Villa Urquiza",
                1,
                "10/08/2026",
                "Agustín Álvarez",
                "agustin@ronda.com",
                "★ 4.8 (34 ventas)",
                new ArrayList<>(Arrays.asList("⌨️ Teclado en caja sellada", "🌈 Efectos de iluminación RGB"))
        ));

        publicaciones.add(new Publicacion(
                6,
                "Zapatillas Nike",
                "Zapatillas deportivas Nike Running talle 42 (27.5 cm de plantilla). Totalmente nuevas con etiquetas puestas en su caja original.",
                180000,
                "Nuevo",
                "Ropa",
                "Palermo",
                6,
                "22/08/2026",
                "Sofía Benítez",
                "sofia@ronda.com",
                "★ 4.9 (50 ventas)",
                new ArrayList<>(Arrays.asList("👟 Vista lateral zapatillas", "🏷️ Etiquetas y caja original", "📏 Suela y plantilla"))
        ));
    }
}
