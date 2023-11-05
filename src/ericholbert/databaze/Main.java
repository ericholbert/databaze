package ericholbert.databaze;

import ericholbert.databaze.core.CoreController;
import ericholbert.databaze.database.DatabaseController;

/*
SEZNAM ZMEN
-opravena chyba, ktera pri pridani nazvu skupiny do jine skupiny zpusobila pridani skupiny
-zmenen nazev baliku programu
-opraveny neaktualnosti v dokumentaci
 */
final class Main {

    public static void main(String[] args) {
        ConsolePrinter.printProgramHeader();
        if (!DatabaseController.loadLastDatabase()) {
            ConsolePrinter.printPlainText("Příkaz '" + Constants.DOK + "' zobrazí dokumentaci.", true);
        }
        CoreController.launch();
    }

}
