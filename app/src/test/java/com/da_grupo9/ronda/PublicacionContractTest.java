package com.da_grupo9.ronda;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.da_grupo9.ronda.data.model.Publicacion;
import com.google.gson.Gson;
import org.junit.Test;

public class PublicacionContractTest {
    @Test public void deserializaDetalleSegunContrato() {
        String json = "{\"id\":\"550e8400-e29b-41d4-a716-446655440000\","
                + "\"title\":\"Notebook\",\"description\":\"Equipo\","
                + "\"category\":\"electronics\",\"itemCondition\":\"like_new\","
                + "\"price\":1250.5,\"status\":\"active\",\"isFavorite\":true,"
                + "\"seller\":{\"id\":\"seller-id\",\"name\":\"Ana\",\"ratingAverage\":4.8,\"ratingCount\":12},"
                + "\"images\":[{\"id\":\"image-id\",\"url\":\"https://example.test/a.jpg\",\"position\":0}]}";
        Publicacion item = new Gson().fromJson(json, Publicacion.class);

        assertEquals("550e8400-e29b-41d4-a716-446655440000", item.getId());
        assertEquals("Tecnología", item.getCategoria());
        assertEquals("Como nuevo", item.getEstado());
        assertEquals(1250.5, item.getPrecio(), 0.001);
        assertEquals("Ana", item.getVendedorNombre());
        assertEquals("https://example.test/a.jpg", item.getImagenes().get(0));
        assertTrue(item.isFavorite());
    }

    @Test public void listadoPublicoSinStatusSigueSiendoVisible() {
        String json = "{\"id\":\"publication-id\",\"title\":\"Notebook\","
                + "\"description\":\"Equipo\",\"price\":1250.5}";

        Publicacion item = new Gson().fromJson(json, Publicacion.class);

        assertTrue(item.isVisibleInPublicFeed());
    }

    @Test public void listadoPublicoNoMuestraEstadosNoActivosExplicitos() {
        Publicacion item = new Gson().fromJson("{\"status\":\"paused\"}", Publicacion.class);

        assertFalse(item.isVisibleInPublicFeed());
    }
}
