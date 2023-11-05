package ericholbert.databaze.core;

import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ericholbert.databaze.ConsolePrinter;
import ericholbert.databaze.Constants;
import ericholbert.databaze.database.DatabaseController;

public final class CoreController {

    public static void launch() {
        Scanner s = new Scanner(System.in);
        while (true) {
            String input = s.nextLine();
            passInput(input);
        }
    }

    public static void testLaunch(String... batchInput) {
        for (String command : batchInput) {
            System.out.println(command);
            passInput(command);
        }
    }

    /*
    Vraci true, pokud je zadan existujici command a je dodrzen minimalni pocet arguments. Uspesnost volaneho prikazu
    nema na vracenou hodnotu vliv.
     */
    public static boolean executeCommand(String command, String option, String[] arguments) {
        switch (command) {
            case Constants.UK -> System.exit(0);
            case Constants.VSE + Constants.NAST -> {
                String[] columnOne = {"Cesta k uloženým databázím [0]", "Šířka okna [1]"};
                String[] columnTwo = {
                        CorePreferences.getRootPath().toString(),
                        Integer.toString(CorePreferences.getWindowWidth())
                };
                ConsolePrinter.printTwoColumns(columnOne, columnTwo);
            }
            case Constants.NAST -> {
                if (arguments == null)
                    return false;
                switch (arguments[0]) {
                    case "0" -> {
                        if (arguments.length < 2)
                            return false;
                        if (!CorePreferences.isRootPathValid(arguments[1]))
                            ConsolePrinter.printLog("Cesta neexistuje nebo je chybná.");
                        else
                            CorePreferences.setRootPath(arguments[1]);
                    }
                    case "1" -> {
                        if (arguments.length < 3 && !arguments[1].matches("\\d+")) {
                            return false;
                        }
                        if (!CorePreferences.setWindowWidth(Integer.parseInt(arguments[1])))
                            ConsolePrinter.printLog("Minimální povolená šířka je 80.");
                    }
                    default -> { return false; }
                }
            }
            case Constants.INFO -> {
                String[] columnOne = {"Verze", "Vytvořeno", "Změněno"};
                String[] columnTwo = {"1.4.2", "09-2021", "10-2023"};
                ConsolePrinter.printTwoColumns(columnOne, columnTwo);
            }
            case Constants.DOK -> {
                ConsolePrinter.printPlainText("Do databáze (" + Constants.DAT + "), jichž může být v programu vícero a mezi nimiž lze libovolně přepínat, jsou ukládany jednotlivé záznamy (" + Constants.ZAZ + "), které jsou děleny na jednotlivé skupiny parametrů (" + Constants.SKUP + ") a které nakonec sestávají ze samotných parametrů (" + Constants.PAR + "). Podobu parametru mají také soubory (" + Constants.SOUB + "), jsou-li tyto do databáze uloženy.", true);
                ConsolePrinter.printPlainText("Záznamy a jejich obsah je tvořen, měněn a mazán pomocí příkazů, jejichž syntaxe je 'příkaz alterace_příkazu alterace_argumentu_argument', přičemž je-li alterace nebo argument v hranatých závorkách ([]), není povinný, a jsou-li za argumentem tři tečky (...), může jich být v příkazu obsaženo vícero.", true);
                ConsolePrinter.printPlainText("Má-li být zavolán tentýž příkaz včetně jeho alterace vícekrát, ale vždy s jinými argumenty, lze jej napsat pouze jedenkrát a argumenty či jejich skupiny oddělit čárkou (,).", true);
                ConsolePrinter.printPlainText("Databáze a nastavení je třeba mazat ručně. To platí rovněž pro přejmenování databáze nebo souboru.", true);
                ConsolePrinter.printPlainText("Příkazy volatelné okamžitě:", true);
                String[] columnOneA = {
                        Constants.UK,
                        Constants.VSE + Constants.NAST,
                        Constants.NAST + " index " + Constants.NAST,
                        Constants.INFO,
                        Constants.DOK,
                        Constants.VSE + Constants.DAT,
                        Constants.NAC + Constants.DAT + " " + Constants.DAT,
                        Constants.PRID + Constants.DAT + " " + Constants.DAT
                };
                String[] columnTwoA = {
                        "Ukončí program bez zavření příkazového řádku.",
                        "Zobrazí nastavení programu.",
                        "Dle zadaného indexu změní některé nastavení programu. To nemusí být před změnou zobrazeno.",
                        "Zobrazí základní informace o programu.",
                        "Zobrazí dokumentaci.",
                        "Zobrazí všechny uložené databáze.",
                        "Načte databázi.",
                        "Vytvoří databázi."
                };
                ConsolePrinter.printTwoColumns(columnOneA, columnTwoA);
                ConsolePrinter.printPlainText("Příkazy volatelné po načtení nebo vytvoření databáze:", true);
                String[] columnOneB = {
                        Constants.DAT,
                        Constants.UL + Constants.DAT,
                        Constants.NAC + Constants.ZAZ + " [-" + Constants.ZHRUBA + Constants.INDEX + Constants.DOHROMADY + Constants.VICE + Constants.SHROMAZDIT + "] [" + Constants.HIDE +"][" + Constants.REVERSE + "][" + Constants.PAR + "...]",
                        Constants.PRID + Constants.ZAZ + " [-" + Constants.ULOZIT + Constants.SHROMAZDIT + "] [" + Constants.SKUP + "...]",
                        Constants.PRID + Constants.ZAZ + " -" + Constants.KOPIROVAT + " id"
                };
                String[] columnTwoB = {
                        "Zobrazí záhlaví aktivní databáze.",
                        "Uloží aktivní databázi.",
                        "Najde všechny záznamy v databázi (1); " +
                                "záznamy, které obsahují všechny zadané parametry nehledě na jejich diakritiku, velká písmena a celistvost (2.1); " +
                                "záznamy, které obsahují všechny zadané parametry, přičemž jejich skupiny parametrů a parametry budou opatřeny indexy, které se stanou jejich substitucí (2.2); " +
                                "všechny neopakující se skupiny parametrů a parametry vyhledaných záznamů, přičemž parametry budou vypsány s počtem svých výskytů (2.3); " +
                                "záznamy, které obsahují alespoň jeden zadaný parametr (2.4); " +
                                "všechny příslušným příkazem shromážděné záznamy, přičemž shromáždění trvá do ukončení programu (2.5); " +
                                "záznamy, které obsahují všechny zadané parametry, přičemž ty alterované, jsou-li to skupiny parametrů, budou spolu se svými parametry skryty (3); " +
                                "záznamy, které obsahují všechny zadané parametry kromě těch, které jsou alterované (4); " +
                                "anebo ty, které obsahují všechny zadané parametry (6). " +
                                "Záznamy zůstávájí nalezené do dalšího hledání.",
                        "Přidá záznam s dříve uloženými skupinami parametrů (1); " +
                                "se zadanými skupinami parametrů, které posléze uloží (2.1); " +
                                "se zadanými nebo načtenými skupinami parametrů, přičemž záznam bude spolu s dalšími takto volanými do ukončení programu shromážděn, aby s nimi mohl být příslušným příkazem posléze nalezen (2.2); " +
                                "anebo se zadanými skupinami parametrů (3).",
                        "Přidá záznam, který je kopií viditelného obsahu záznamu s příslušným ID."
                };
                ConsolePrinter.printTwoColumns(columnOneB, columnTwoB);
                ConsolePrinter.printPlainText("Příkazy volatelné po vyhledání alespoň jednoho záznamu. Má-li být při zapnutém substituování parametrů upraven číselný parametr, lze tak učinit pomocí příslušné alterace (" + Constants.SKIP + "):", true);
                String[] columnOneC = {
                        Constants.SMAZ + Constants.ZAZ + " [id]",
                        Constants.PRID + Constants.SKUP + " [id] " + Constants.SKUP,
                        Constants.SMAZ + Constants.SKUP + " [id] " + Constants.SKUP,
                        Constants.PRID + Constants.PAR + " [id] " + Constants.SKUP + " " + Constants.PAR,
                        Constants.SMAZ + Constants.PAR + " [id] " + Constants.PAR,
                        Constants.ZMEN + Constants.PAR + " [id] stary_" + Constants.PAR + " novy_" + Constants.PAR,
                        Constants.ZMEN + Constants.SKUP + " [...]",
                        Constants.PRID + Constants.SOUB + " [-" + Constants.PREJMENOVAT + "] [id] " + Constants.SKUP + " cesta_k_" + Constants.SOUB,
                        Constants.NAC + Constants.SOUB + " [id] " + Constants.SKUP,
                        Constants.SMAZ + Constants.SOUB +  " [id] " + Constants.SKUP
                };
                String[] columnTwoC = {
                        "Smaže všechny záznamy v databázi (1); " +
                                "anebo ten s příslušným ID (2).",
                        "Přidá skupinu parametrů všem vyhledaným záznamům (1); " +
                                "anebo tomu s příslušným ID (2). " +
                                "Záznam nemůže obsahovat dvě stejně pojmenované skupiny parametrů.",
                        "Smaže [...] (1); " +
                                "[...] (2). " +
                                "Budou-li smazány všechny skupiny parametrů, smaže se i celý záznam.",
                        "Přidá parametr do příslušné skupiny parametrů všech vyhledaných záznamů (1); " +
                                "anebo toho s příslušným ID (2). " +
                                "Záznam nemůže obsahovat dva stejné parametry.",
                        "Smaže parametr všem vyhledaným záznamům (1); " +
                                "anebo tomu s příslušným ID (2). ",
                        "Změní [...] (1); " +
                                "[...] (2).",
                        "[...]",
                        "Přidá soubor do příslušné skupiny parametrů všem vyhledaným záznamům (1); " +
                                "přejmenuje jej tak, aby byl název jedinečný (2.1); " +
                                "anebo jej přidá tomu s příslušným ID (3). " +
                                "V jedné databázi nemůže být vícero souborů stejného názvu.",
                        "Načte soubory nacházející se v příslušné skupině parametrů všech vyhledaných záznamů (1); " +
                                "anebo záznamu s příslušným ID (2).",
                        "Přesune do koše [...] (1); " +
                                "[...] (2). " +
                                "Následně je třeba smazat parametry, které představovaly reference k těmto souborům."
                };
                ConsolePrinter.printTwoColumns(columnOneC, columnTwoC);
            }
            default -> { return DatabaseController.executeCommand(command, option, arguments); }
        }
        return true;
    }

    private static void passInput(String input) {
        if (input.equals("") || input.contains("^")) {
            ConsolePrinter.underlineCommand(1);
            ConsolePrinter.printLog("Byl zadán nepovolený znak.");
            return;
        }
        //https://stackoverflow.com/questions/1757065/splitting-a-comma-separated-string-but-ignoring-commas-in-quotes
        String[] batchArguments = input.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
        ArrayList<String> subcommands = getSubcommands(batchArguments[0]);
        String command = getCommand(subcommands);
        if (batchArguments.length > 1 && !Constants.isCommandSuitableForBatch(command)) {
            ConsolePrinter.underlineCommand(input.length());
            ConsolePrinter.printLog("Příkaz nemůže být dávkován.");
            return;
        }
        String option = getOption(subcommands);
        String[] arguments = getArguments(subcommands, option);
        for (int i = 0; i < batchArguments.length; i++) {
            if (i > 0) {
                batchArguments[i] = batchArguments[i].trim();
                arguments = getSubcommands(batchArguments[i]).toArray(new String[0]);
                if (option != null)
                    batchArguments[i] = command + " " + option + " " + batchArguments[i];
                else
                    batchArguments[i] = command + " " + batchArguments[i];
                ConsolePrinter.printPlainText(batchArguments[i], false);
            }
            ConsolePrinter.underlineCommand(batchArguments[i].length());
            ConsolePrinter.setFoundItemPrinting(i == batchArguments.length - 1 |
                    command.equals(Constants.PRID + Constants.ZAZ));
            if (!executeCommand(command, option, arguments))
                ConsolePrinter.printLog("Chyba zadání.");
        }
    }

    private static String getCommand(ArrayList<String> subcommands) {
        if (subcommands.size() > 0)
            return subcommands.get(0);
        else
            return null;
    }

    private static String getOption(ArrayList<String> subcommands) {
        String option;
        if (subcommands.size() > 1 && (option = subcommands.get(1)).matches("-\\w+"))
            return option;
        else
            return null;
    }

    private static String[] getArguments(ArrayList<String> subcommands, String option) {
        int firstArgumentIndex = 1;
        if (option != null)
            firstArgumentIndex = 2;
        String[] arguments;
        if (subcommands.size() > firstArgumentIndex) {
            arguments = new String[subcommands.size() - firstArgumentIndex];
            for (int i = firstArgumentIndex; i < subcommands.size(); i++)
                arguments[i - firstArgumentIndex] = subcommands.get(i);
            return arguments;
        } else
            return null;
    }

    //Neni podmineno tim, co input obsahuje: vzdy jej rozdeli podle mezer s vynechanim mezer mezi uvozovkami.
    private static ArrayList<String> getSubcommands(String input) {
        ArrayList<String> subcommands = new ArrayList<>();
        String regex = "\"([^\"]*)\"|(\\S+)";
        Matcher m = Pattern.compile(regex).matcher(input);
        while (m.find()) {
            if (m.group(1) != null)
                subcommands.add(m.group(1));
            else
                subcommands.add(m.group(2));
        }
        return subcommands;
    }

}
