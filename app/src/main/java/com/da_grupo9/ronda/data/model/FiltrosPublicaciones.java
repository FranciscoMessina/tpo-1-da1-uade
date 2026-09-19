package com.da_grupo9.ronda.data.model;

/** Filtros ya normalizados y validados para listar o guardar una búsqueda. */
public final class FiltrosPublicaciones {
    private final String query;
    private final String category;
    private final String condition;
    private final String zone;
    private final Double minPrice;
    private final Double maxPrice;
    private final String sort;

    private FiltrosPublicaciones(String query, String category, String condition, String zone,
                                 Double minPrice, Double maxPrice, String sort) {
        this.query = query;
        this.category = category;
        this.condition = condition;
        this.zone = zone;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.sort = sort;
    }

    public static FiltrosPublicaciones crear(String query, String category, String condition,
                                              String zone, String minPrice, String maxPrice,
                                              String sort) {
        String normalizedQuery = opcional(query);
        if (normalizedQuery != null && normalizedQuery.length() > 200) {
            throw new IllegalArgumentException("La búsqueda no puede superar los 200 caracteres");
        }

        Double min = precioOpcional(minPrice);
        Double max = precioOpcional(maxPrice);
        if (min != null && min < 0 || max != null && max < 0) {
            throw new IllegalArgumentException("Los precios no pueden ser negativos");
        }
        if (min != null && max != null && min > max) {
            throw new IllegalArgumentException("El precio mínimo no puede superar al máximo");
        }

        return new FiltrosPublicaciones(normalizedQuery, opcional(category), opcional(condition),
                opcional(zone), min, max, opcional(sort) != null ? sort.trim() : "recent");
    }

    private static String opcional(String value) {
        String trimmed = value == null ? "" : value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static Double precioOpcional(String value) {
        String normalized = opcional(value);
        if (normalized == null) return null;
        try {
            double price = Double.parseDouble(normalized);
            if (!Double.isFinite(price)) throw new NumberFormatException();
            return price;
        } catch (NumberFormatException error) {
            throw new IllegalArgumentException("Ingresá precios válidos");
        }
    }

    public String getQuery() { return query; }
    public String getCategory() { return category; }
    public String getCondition() { return condition; }
    public String getZone() { return zone; }
    public Double getMinPrice() { return minPrice; }
    public Double getMaxPrice() { return maxPrice; }
    public String getSort() { return sort; }
}
