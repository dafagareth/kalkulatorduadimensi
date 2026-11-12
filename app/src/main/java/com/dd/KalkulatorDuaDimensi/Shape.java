package com.dd.KalkulatorDuaDimensi;

enum Shape {
    PERSEGI(R.string.square, R.string.square_description,
            R.string.square_area, R.string.square_perimeter, 0, R.string.side_label),
    PERSEGI_PANJANG(R.string.rectangle, R.string.rectangle_description,
            R.string.rectangle_area, R.string.rectangle_perimeter, 0,
            R.string.length_label, R.string.width_label),
    SEGITIGA(R.string.triangle, R.string.triangle_description,
            R.string.triangle_area, R.string.triangle_perimeter, R.string.invalid_triangle,
            R.string.side_a_label, R.string.side_b_label, R.string.side_c_label),
    TRAPESIUM(R.string.trapezoid, R.string.trapezoid_description,
            R.string.trapezoid_area, R.string.trapezoid_perimeter, R.string.invalid_trapezoid,
            R.string.top_label, R.string.bottom_label, R.string.left_label, R.string.right_label),
    LAYANG_LAYANG(R.string.kite, R.string.kite_description,
            R.string.kite_area, R.string.kite_perimeter, 0,
            R.string.upper_label, R.string.lower_label, R.string.across_label),
    JAJAR_GENJANG(R.string.parallelogram, R.string.parallelogram_description,
            R.string.parallelogram_area, R.string.parallelogram_perimeter,
            R.string.invalid_parallelogram,
            R.string.base_label, R.string.sloping_side_label, R.string.height_label),
    BELAH_KETUPAT(R.string.rhombus, R.string.rhombus_description,
            R.string.rhombus_area, R.string.rhombus_perimeter, 0,
            R.string.first_diagonal_label, R.string.second_diagonal_label),
    LINGKARAN(R.string.circle, R.string.circle_description,
            R.string.circle_area, R.string.circle_perimeter, 0, R.string.radius_label),
    SEGI_LIMA(R.string.pentagon, R.string.pentagon_description,
            R.string.pentagon_area, R.string.pentagon_perimeter, 0, R.string.side_label),
    SEGI_ENAM(R.string.hexagon, R.string.hexagon_description,
            R.string.hexagon_area, R.string.hexagon_perimeter, 0, R.string.side_label);

    final int title;
    final int description;
    final int areaResult;
    final int perimeterResult;
    final int invalidShape;
    final int[] labels;

    Shape(int title, int description, int areaResult, int perimeterResult,
            int invalidShape, int... labels) {
        this.title = title;
        this.description = description;
        this.areaResult = areaResult;
        this.perimeterResult = perimeterResult;
        this.invalidShape = invalidShape;
        this.labels = labels;
    }

    static Shape fromKey(String key) {
        for (Shape shape : values()) {
            if (shape.name().equals(key)) {
                return shape;
            }
        }
        return null;
    }
}
