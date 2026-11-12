package com.dd.KalkulatorDuaDimensi;

import java.math.BigDecimal;
import java.math.MathContext;

final class ShapeCalculator {
    private static final BigDecimal TWO = BigDecimal.valueOf(2);
    private static final MathContext DISPLAY_PRECISION = new MathContext(12);
    private static final BigDecimal PI = BigDecimal.valueOf(Math.PI);
    private static final BigDecimal PENTAGON_FACTOR =
            BigDecimal.valueOf(Math.sqrt(25 + 10 * Math.sqrt(5)) / 4);
    private static final BigDecimal HEXAGON_FACTOR = BigDecimal.valueOf(3 * Math.sqrt(3) / 2);

    private ShapeCalculator() {}

    static Measurements calculate(Shape shape, BigDecimal[] dimensions) {
        if (dimensions.length != shape.labels.length) {
            throw new IllegalArgumentException("Jumlah ukuran tidak sesuai dengan bangun.");
        }
        for (BigDecimal dimension : dimensions) {
            if (dimension == null || dimension.signum() <= 0) {
                return null;
            }
        }

        BigDecimal first = dimensions[0];
        switch (shape) {
            case PERSEGI:
                return new Measurements(first.pow(2), first.multiply(BigDecimal.valueOf(4)), first);
            case PERSEGI_PANJANG:
                return new Measurements(first.multiply(dimensions[1]),
                        first.add(dimensions[1]).multiply(TWO), first, dimensions[1]);
            case SEGITIGA:
                return triangle(first, dimensions[1], dimensions[2]);
            case TRAPESIUM:
                return trapezoid(first, dimensions[1], dimensions[2], dimensions[3]);
            case LAYANG_LAYANG:
                BigDecimal halfAcrossSquared = dimensions[2].divide(TWO).pow(2);
                BigDecimal upperSide = sqrt(first.pow(2).add(halfAcrossSquared));
                BigDecimal lowerSide = sqrt(dimensions[1].pow(2).add(halfAcrossSquared));
                return new Measurements(first.add(dimensions[1]).multiply(dimensions[2]).divide(TWO),
                        upperSide.add(lowerSide).multiply(TWO).round(DISPLAY_PRECISION),
                        first, dimensions[1], dimensions[2]);
            case JAJAR_GENJANG:
                if (dimensions[2].compareTo(dimensions[1]) > 0) {
                    return null;
                }
                return new Measurements(first.multiply(dimensions[2]),
                        first.add(dimensions[1]).multiply(TWO), first, dimensions[2]);
            case BELAH_KETUPAT:
                BigDecimal sideSquared = first.divide(TWO).pow(2)
                        .add(dimensions[1].divide(TWO).pow(2));
                return new Measurements(first.multiply(dimensions[1]).divide(TWO),
                        sqrt(sideSquared).multiply(BigDecimal.valueOf(4)).round(DISPLAY_PRECISION),
                        first, dimensions[1]);
            case LINGKARAN:
                return new Measurements(PI.multiply(first.pow(2)).round(DISPLAY_PRECISION),
                        TWO.multiply(PI).multiply(first).round(DISPLAY_PRECISION), first);
            case SEGI_LIMA:
                return new Measurements(PENTAGON_FACTOR.multiply(first.pow(2)).round(DISPLAY_PRECISION),
                        first.multiply(BigDecimal.valueOf(5)), first);
            case SEGI_ENAM:
                return new Measurements(HEXAGON_FACTOR.multiply(first.pow(2)).round(DISPLAY_PRECISION),
                        first.multiply(BigDecimal.valueOf(6)), first);
            default:
                throw new IllegalArgumentException("Bangun tidak dikenali.");
        }
    }

    private static Measurements triangle(BigDecimal a, BigDecimal b, BigDecimal c) {
        BigDecimal perimeter = a.add(b).add(c);
        BigDecimal s = perimeter.divide(TWO);
        if (s.compareTo(a) <= 0 || s.compareTo(b) <= 0 || s.compareTo(c) <= 0) {
            return null;
        }
        BigDecimal areaSquared = s.multiply(s.subtract(a))
                .multiply(s.subtract(b)).multiply(s.subtract(c));
        return new Measurements(sqrt(areaSquared).round(DISPLAY_PRECISION), perimeter);
    }

    private static Measurements trapezoid(BigDecimal top, BigDecimal bottom,
            BigDecimal left, BigDecimal right) {
        BigDecimal baseDifference = top.subtract(bottom).abs();
        BigDecimal s = baseDifference.add(left).add(right).divide(TWO);
        if (baseDifference.signum() == 0 || s.compareTo(baseDifference) <= 0
                || s.compareTo(left) <= 0 || s.compareTo(right) <= 0) {
            return null;
        }

        // Segitiga dari selisih sisi sejajar dan kedua kaki memiliki tinggi yang sama.
        BigDecimal areaSquared = s.multiply(s.subtract(baseDifference))
                .multiply(s.subtract(left)).multiply(s.subtract(right));
        BigDecimal height = sqrt(areaSquared).multiply(TWO)
                .divide(baseDifference, MathContext.DECIMAL128);
        BigDecimal area = top.add(bottom).multiply(height).divide(TWO);
        return new Measurements(area.round(DISPLAY_PRECISION),
                top.add(bottom).add(left).add(right), top, bottom, height.round(DISPLAY_PRECISION));
    }

    private static BigDecimal sqrt(BigDecimal value) {
        return BigDecimal.valueOf(Math.sqrt(value.doubleValue()));
    }

    static final class Measurements {
        final BigDecimal area;
        final BigDecimal perimeter;
        final BigDecimal[] areaInputs;

        Measurements(BigDecimal area, BigDecimal perimeter, BigDecimal... areaInputs) {
            this.area = area;
            this.perimeter = perimeter;
            this.areaInputs = areaInputs;
        }
    }
}
