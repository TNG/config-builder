# Playbook Vortrag

## Intro

 * Main Methode implementieren
    * ConfigBuilder
    * JsonHelper

## Default values

 * `@DefaultValue` Annotation für characterName hinzufügen
 * `@DefaultValue` Annotation für difficulty hinzufügen

## Command line values

 * Config-Klasse
     * `@CommandLineValue` Annotation für `characterName` hinzufügen
     * `@CommandLineValue` Annotation für `difficulty` hinzufügen

 * Skript
     * Kommandozeile schreiben

## Property values

 * Config-Klasse
     * `@PropertyValue` Annotation für `difficulty` hinzufügen
     * `@PropertyValue` Annotation für `player.color` hinzufügen

 * Properties-Datei
     * `difficulty` setzen
     * `player.color` setzen

## Environment and system property values

 * Config-Klasse
     * SystemPropertyValue für `language` hinzufügen
     * EnvironmentVariableValue für `currentDirectory` hinzufügen.

## Transformer for Color

 * Config-Klasse
     * StringToColorTransformer-Klasse implementieren

 * Property File
     * `player.color = red`

## Transformer for Set&lt;Cheat&gt;

 * Config-Klasse
     * StringToCheatTransformer-Klasse implementieren

 * Property File
     * `cheats.active = GOD_MODE,WALLHACK

## Validation JSR303

 * Config-Klasse
     * Annotation `@Size(min=2, max=14)` an `characterName`
     * Annotation `@NotNull` an `currentDirectory

 * DefaultValue ändern, so dass Validation zuschlägt

## Validation method

 * Config-Klasse
     * Methode `validate` implementieren:

        characterName darf nicht 'god' sein

     * laufen lassen -> geht

     * DefaultValue für `characterName` auf `god` setzen

     * laufen lassen -> Exception fliegt

## File Names, Suffixes &amp; Extensions

 * Config-Klasse
     * drei Annotations an die Klasse anfügen

## Locations

 * Config-Klasse
     * Location annotation an die Klasse anfügen

## Custom Annotation

 * Config-Klasse
     * Custom Annotation `@FileContentValue` an die Klasse anfügen
