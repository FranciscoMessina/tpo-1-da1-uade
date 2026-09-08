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

    public static void actualizarPublicacion(
            int id,
            String titulo,
            String descripcion,
            double precio,
            String estado,
            String categoria,
            String zona) {

        Publicacion publicacion = getPublicacionById(id);

        if (publicacion != null) {
            publicacion.setTitulo(titulo);
            publicacion.setDescripcion(descripcion);
            publicacion.setPrecio(precio);
            publicacion.setEstado(estado);
            publicacion.setCategoria(categoria);
            publicacion.setZona(zona);
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
                new ArrayList<>(Arrays.asList(
                        "https://images.unsplash.com/photo-1695048133142-1a20484d2569?w=800",
                        "https://images.unsplash.com/photo-1510557880182-3d4d3cba35a5?w=800",
                        "https://images.unsplash.com/photo-1592750475338-74b7b21085ab?w=800"
                ))
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
                new ArrayList<>(Arrays.asList(
                        "https://images.unsplash.com/photo-1485965120184-e220f721d03e?w=800",
                        "https://images.unsplash.com/photo-1532298229144-0ec0c57515c7?w=800",
                        "https://images.unsplash.com/photo-1576435728678-68d0fbf94e91?w=800"
                ))
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
                new ArrayList<>(Arrays.asList(
                        "https://images.unsplash.com/photo-1606813907291-d86efa9b94db?w=800",
                        "https://images.unsplash.com/photo-1607604276583-eef5d076aa5f?w=800",
                        "https://images.unsplash.com/photo-1622297845775-5ff3fef71d13?w=800"
                ))
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
                new ArrayList<>(Arrays.asList(
                        "https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?w=800",
                        "https://images.unsplash.com/photo-1585792180666-f7347c490ee2?w=800"
                ))
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
                new ArrayList<>(Arrays.asList(
                        "https://images.unsplash.com/photo-1587829741301-dc798b83add3?w=800",
                        "https://images.unsplash.com/photo-1618384887929-16ec33fab9ef?w=800"
                ))
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
                new ArrayList<>(Arrays.asList(
                        "https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=800",
                        "https://images.unsplash.com/photo-1600185365926-3a2ce3cdb9eb?w=800",
                        "https://images.unsplash.com/photo-1595950653106-6c9ebd614d3a?w=800"
                ))
        ));
    }
}
