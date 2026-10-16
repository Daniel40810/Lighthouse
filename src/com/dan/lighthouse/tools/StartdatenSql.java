package com.dan.lighthouse.tools;

import com.dan.lighthouse.db.Startdaten;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * Schreibt {@code db/sql/04_startdaten.sql} aus {@link Startdaten}. Nach jeder Änderung an den mitgelieferten Daten
 * (Türme, Wetterlagen, Regiebücher, Fahrten, Presets der Packages) einmal laufen lassen; die Prüfung meldet sonst,
 * dass Datei und Code auseinanderlaufen.
 */
public final class StartdatenSql {
    public static void main(String[] args) throws Exception {
        File f = new File(args.length > 0 ? args[0] : "db/sql/04_startdaten.sql");
        String sql = Startdaten.sql();
        Files.writeString(f.toPath(), sql, StandardCharsets.UTF_8);
        System.out.println(f + ": " + sql.lines().count() + " Zeilen, " + Startdaten.tuerme().size() + " Türme, "
                + Startdaten.presets().size() + " Presets");
    }

    private StartdatenSql() { }
}
