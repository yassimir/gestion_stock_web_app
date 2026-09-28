package com.gharnata.service;

public class NombreEnLettres {

    private static final String[] UNITS = {
            "", "un", "deux", "trois", "quatre", "cinq", "six", "sept", "huit", "neuf",
            "dix", "onze", "douze", "treize", "quatorze", "quinze", "seize"
    };

    private static final String[] TENS = {
            "", "", "vingt", "trente", "quarante", "cinquante", "soixante"
    };

    public static String convertir(int n) {
        if (n < 0) return "moins " + convertir(-n);
        if (n == 0) return "zéro";
        if (n == 1_000_000) return "un million";

        if (n < 17) return UNITS[n];

        if (n < 20) return "dix-" + UNITS[n - 10];

        if (n < 70) {
            int unit = n % 10;
            String base = TENS[n / 10];
            if (unit == 1 && (n / 10) != 8) return base + "-et-un";
            else if (unit > 0) return base + "-" + UNITS[unit];
            else return base;
        }

        //if (n < 80) return "soixante-" + convertir(n - 60);
        if (n < 80) {
            if (n == 71) return "soixante-et-onze"; // cas spécial
            return "soixante-" + convertir(n - 60);
        }
        if (n < 100) return "quatre-vingt" + (n == 80 ? "s" : "-" + convertir(n - 80));

        if (n < 200) return "cent" + (n > 100 ? " " + convertir(n - 100) : "");
        if (n < 1000) {
            String prefix = UNITS[n / 100];
            return prefix + " cent" + ((n % 100 > 0) ? " " + convertir(n % 100) : "");
        }

        if (n < 2000) return "mille" + (n > 1000 ? " " + convertir(n - 1000) : "");
        if (n < 1_000_000) {
            int milliers = n / 1000;
            int reste = n % 1000;
            String milleStr = (milliers == 1 ? "mille" : convertir(milliers) + " mille");
            return milleStr + (reste > 0 ? " " + convertir(reste) : "");
        }

        return "nombre trop grand";
    }
}
