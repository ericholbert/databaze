package ericholbert.databaze;

public final class Constants {

    //Klicova slova prikazu
    public static final String UK = "uk";
    public static final String NAST = "nast";
    public static final String INFO = "info";
    public static final String DOK = "dok";
    public static final String DAT = "dat";
    public static final String ZAZ = "zaz";
    public static final String SKUP = "skup";
    public static final String PAR = "par";
    public static final String SOUB = "soub";

    //Zpusoby zachazeni s prikazy
    public static final String VSE = "vse";
    public static final String NAC = "nac";
    public static final String UL = "ul";
    public static final String PRID = "prid";
    public static final String SMAZ = "smaz";
    public static final String ZMEN = "zmen";

    //Alterace
    public static final String SHROMAZDIT = "s";
    public static final String VICE = "v";
    public static final String ULOZIT = "u";
    public static final String ZHRUBA = "z";
    public static final String DOHROMADY = "d";
    public static final String PREJMENOVAT = "p";
    public static final String INDEX = "i";
    public static final String KOPIROVAT = "k";
    public static final String HIDE = "(";
    public static final String REVERSE = "!";
    public static final String SKIP = "_";

    public static boolean isCommandSuitableForBatch(String command) {
        String regex = String.format(".*(%s|%s|%s)", SKUP, PAR, SOUB);
        return command.matches(regex) || command.equals(SMAZ + ZAZ);
    }

}
