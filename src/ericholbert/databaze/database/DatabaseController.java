package ericholbert.databaze.database;

import ericholbert.databaze.ConsolePrinter;
import ericholbert.databaze.Constants;
import ericholbert.databaze.core.CoreController;
import ericholbert.databaze.core.CorePreferences;

public final class DatabaseController {

    private static Database database;
    private static ItemsController itemsController;
    private static final UniqueParameters uniqueParameters = new UniqueParameters();
    private static boolean alternativeCheckMethod;
    private static boolean addIndexes;

    public static boolean loadLastDatabase() {
        String lastDatabaseName;
        if ((lastDatabaseName = CorePreferences.getLastDatabaseName()) != null) {
            CoreController.executeCommand(
                    Constants.NAC + Constants.DAT,
                    null,
                    new String[] {lastDatabaseName}
            );
            return true;
        }
        return false;
    }

    //Prikazy pred vytvorenim nebo nactenim databaze.
    public static boolean executeCommand(String command, String option, String[] arguments) {
        if (command.equals(Constants.VSE + Constants.DAT)) {
            String[] databaseNames;
            if ((databaseNames = Database.getDatabaseNames()) == null)
                ConsolePrinter.printLog("Není uložena žádná databáze.");
            else
                for (int i = 0; i < databaseNames.length; i++) {
                    boolean newline = i == databaseNames.length - 1;
                    ConsolePrinter.printPlainText(databaseNames[i], newline);
                }
            return true;
        }
        switch (command) {
            case Constants.NAC + Constants.DAT -> {
                if (arguments == null)
                    return false;
                if (!Database.itemsFileExists(arguments[0]))
                    ConsolePrinter.printLog("Databáze neexistuje.");
                else {
                    database = new Database(arguments[0]);
                    itemsController = new ItemsController();
                    itemsController.setItems(database.loadDatabase());
                    uniqueParameters.clear();
                    ConsolePrinter.printDatabaseHeader(arguments[0], itemsController.getItemsSize());
                    CorePreferences.setLastDatabaseName(arguments[0]);
                }
            }
            case Constants.PRID + Constants.DAT -> {
                if (arguments == null)
                    return false;
                if (Database.itemsFileExists(arguments[0]))
                    ConsolePrinter.printLog("Databáze již existuje.");
                else if (!arguments[0].matches("\\w+|(\\w+[-_]\\w+)+"))
                    ConsolePrinter.printLog("Jsou povoleny pouze písmena bez diakritiky, čísla, '-' a '_'.");
                else {
                    database = new Database(arguments[0]);
                    itemsController = new ItemsController();
                    uniqueParameters.clear();
                    ConsolePrinter.printDatabaseHeader(arguments[0], itemsController.getItemsSize());
                }
            }
            default -> {
                if (database == null && itemsController == null)
                    return false;
                else
                    return executeCommand2(command, option, arguments);
            }
        }
        return true;
    }

    //Prikazy po vytvoreni nebo nacteni databaze a pred vyhledanim alespon jednoho zaznamu.
    private static boolean executeCommand2(String command, String option, String[] arguments) {
        if (arguments != null && addIndexes) {
            if (!getArguments(arguments))
                return true;
        }
        if (option != null && option.contains(Constants.INDEX))
            addIndexes = true;
        switch (command) {
            case Constants.DAT ->
                    ConsolePrinter.printDatabaseHeader(database.getDatabaseName(), itemsController.getItemsSize());
            case Constants.UL + Constants.DAT -> {
                itemsController.getItemsWithDelimiter();
                if (!database.saveDatabase(itemsController.getItemsWithDelimiter()))
                    ConsolePrinter.printLog("Databázi se nepodařilo uložit.");
                else
                    CorePreferences.setLastDatabaseName(database.getDatabaseName());
            }
            case Constants.NAC + Constants.ZAZ -> {
                boolean preciseMatch = true;
                boolean mergeItems = false;
                boolean addIndexes = false;
                if (option != null) {
                    if (option.contains(Constants.SHROMAZDIT)) {
                        arguments = itemsController.getCollectedIds();
                        option = option.replaceAll(Constants.SHROMAZDIT, Constants.VICE);
                    }
                    if (option.contains(Constants.ZHRUBA))
                        preciseMatch = false;
                    if (option.contains(Constants.DOHROMADY))
                        mergeItems = true;
                    if (option.contains(Constants.INDEX)) {
                        uniqueParameters.clear();
                        addIndexes = true;
                    }
                }
                if (arguments != null) {
                    boolean isMatch;
                    if (option != null && option.contains(Constants.VICE))
                        isMatch = itemsController.findMoreItems(preciseMatch, arguments);
                    else
                        isMatch = itemsController.findItems(preciseMatch, arguments);
                    if (!isMatch)
                        ConsolePrinter.printLog("Nebyl nalezen žádný záznam.");
                } else {
                    if (!itemsController.findAllItems())
                        ConsolePrinter.printLog("V databázi nejsou žádné záznamy.");
                }
                alternativeCheckMethod = mergeItems;
                DatabaseController.addIndexes = addIndexes;
                if (checkFoundItems())
                    ConsolePrinter.printPlainText(
                            "Počet nalezených záznamů: " + itemsController.getFoundItemSize(),
                            true
                    );
            }
            case Constants.PRID + Constants.ZAZ -> {
                if (arguments == null && database.getDatabasePreferences().existsGroupNamesNode())
                    arguments = database.getDatabasePreferences().getGroupNames();
                if (arguments == null)
                    return false;
                String itemId;
                if (option != null && option.contains(Constants.KOPIROVAT)) {
                    Item duplicatedItem = itemsController.getDuplicatedItem(arguments[0]);
                    if (duplicatedItem != null)
                        itemId = duplicatedItem.getId();
                    else {
                        ConsolePrinter.printLog("Záznam nelze duplikovat.");
                        break;
                    }
                } else
                    itemId = itemsController.addItem(arguments);
                itemsController.findItems(true, itemId);
                checkFoundItems();
                if (option == null)
                    break;
                if (option.contains(Constants.ULOZIT))
                    database.getDatabasePreferences().setGroupNames(arguments);
                if (option.contains(Constants.SHROMAZDIT))
                    itemsController.collectId(itemId);
            }
            default -> {
                if (!itemsController.isFoundAnyItem())
                    return false;
                else
                    return executeCommand3(command, option, arguments);
            }
        }
        return true;
    }

    //Prikazy po vyhledani alespon jednoho zaznamu.
    private static boolean executeCommand3(String command, String option, String[] arguments) {
        String wrongArgumentMessageSingular = "Zadaný parametr je chybný.";
        String wrongArgumentMessagePlural = "Nejméně jeden zadaný parametr je chybný.";
        if (command.equals(Constants.SMAZ + Constants.ZAZ)) {
            if (arguments != null) {
                checkFoundItems(itemsController.deleteItem(arguments[0]), wrongArgumentMessageSingular);
            } else
                itemsController.deleteItems();
            return true;
        }
        if (arguments == null)
            return false;
        switch (command) {
            case Constants.PRID + Constants.SKUP -> {
                if (arguments.length >= 2) {
                    checkFoundItems(itemsController.addGroup(arguments[0], arguments[1]), wrongArgumentMessagePlural);
                } else
                    compareReturnedValues(
                            itemsController.addGroups(arguments[0]),
                            wrongArgumentMessageSingular,
                            true
                    );
            }
            case Constants.SMAZ + Constants.SKUP -> {
                if (arguments.length >= 2) {
                    checkFoundItems(itemsController.deleteGroup(
                            arguments[0], arguments[1]),
                            wrongArgumentMessagePlural
                    );
                } else
                    compareReturnedValues(
                            itemsController.deleteGroups(arguments[0]),
                            wrongArgumentMessageSingular,
                            true
                    );
            }
            case Constants.PRID + Constants.PAR, Constants.PRID + Constants.SOUB -> {
                if (command.equals(Constants.PRID + Constants.SOUB)) {
                    boolean renameFile = option != null && option.contains(Constants.PREJMENOVAT);
                    String fileName = database.transferFile(arguments[arguments.length - 1], renameFile);
                    if (fileName == null) {
                        checkFoundItems(false, "Soubor se nepodařilo přemístit.");
                        return true;
                    }
                    else
                        arguments[arguments.length - 1] = fileName;
                }
                if (arguments.length >= 3) {
                    checkFoundItems(
                            itemsController.addParameter(arguments[0], arguments[1], arguments[2]),
                            wrongArgumentMessagePlural
                    );
                } else if (arguments.length == 2) {
                    compareReturnedValues(itemsController.addParameters(arguments[0],
                            arguments[1]),
                            wrongArgumentMessagePlural,
                            true
                    );
                } else
                    return false;
            }
            case Constants.SMAZ + Constants.PAR -> {
                if (arguments.length >= 2) {
                    checkFoundItems(
                            itemsController.deleteParameter(arguments[0], arguments[1]),
                            wrongArgumentMessagePlural
                    );
                } else
                    compareReturnedValues(
                            itemsController.deleteParameters(arguments[0]),
                            wrongArgumentMessageSingular,
                            true
                    );
            }
            case Constants.ZMEN + Constants.PAR, Constants.ZMEN + Constants.SKUP -> {
                if (arguments.length >= 3) {
                    checkFoundItems(
                            itemsController.changeParameter(arguments[0], arguments[1], arguments[2]),
                            wrongArgumentMessagePlural
                    );
                } else if (arguments.length == 2) {
                    compareReturnedValues(
                            itemsController.changeParameters(arguments[0], arguments[1]),
                            wrongArgumentMessagePlural,
                            true
                    );
                } else
                    return false;
            }
            case Constants.NAC + Constants.SOUB -> {
                String fileErrorMessage = "Soubor se nepodařilo otevřít.";
                if (arguments.length >= 2)
                    compareReturnedValues(itemsController.openFile(database, arguments[0], arguments[1]),
                            fileErrorMessage,
                            false
                    );
                else
                    compareReturnedValues(itemsController.openFiles(database, arguments[0]),
                            fileErrorMessage,
                            false
                    );
            }
            case Constants.SMAZ + Constants.SOUB -> {
                String fileErrorMessage = "Soubor se nepodařilo smazat.";
                if (arguments.length >= 2)
                    compareReturnedValues(itemsController.deleteFile(database, arguments[0], arguments[1]),
                            fileErrorMessage,
                            false
                    );
                else
                    compareReturnedValues(itemsController.deleteFiles(database, arguments[0]),
                            fileErrorMessage,
                            false
                    );
            }
            default -> { return false; }
        }
        return true;
    }

    private static void compareReturnedValues(
            int[] matchesAndAttempts,
            String wrongArgumentMessage,
            boolean checkFoundItems
    ) {
        if (checkFoundItems)
            checkFoundItems();
        if (matchesAndAttempts[0] == matchesAndAttempts[1]) {
            if (matchesAndAttempts[0] == 0)
                ConsolePrinter.printLog(wrongArgumentMessage);
            return;
        }
        if (matchesAndAttempts[1] != 1) {
            wrongArgumentMessage = ((matchesAndAttempts[1] - matchesAndAttempts[0]) +
                    "/" +
                    matchesAndAttempts[1] +
                    ": " +
                    wrongArgumentMessage);
        }
        ConsolePrinter.printLog(wrongArgumentMessage);
    }

    private static void checkFoundItems(boolean isExecuted, String wrongArgumentMessage) {
        checkFoundItems();
        if (!isExecuted)
            ConsolePrinter.printLog(wrongArgumentMessage);
    }

    private static boolean checkFoundItems() {
        if (itemsController.isFoundAnyItem()) {
            UniqueParameters uniqueParameters = addIndexes ? DatabaseController.uniqueParameters : null;
            if (uniqueParameters != null)
                uniqueParameters.set(itemsController.getUniqueParameters());
            if (!alternativeCheckMethod)
                ConsolePrinter.printFoundItems(itemsController.getFoundItems(), null, uniqueParameters);
            else
                ConsolePrinter.printFoundItems(
                        itemsController.getMergedParametersASetParameterUsages(),
                        itemsController.getParameterUsages(),
                        uniqueParameters
                );
            return true;
        } else
            return false;
    }

    private static boolean getArguments(String[] arguments) {
        String regex = "\\d+";
        int indexCounter = 0;
        for (String argument : arguments)
            if (argument.matches(regex))
                indexCounter++;
        for (int i = 0; i < arguments.length; i++) {
            if (arguments[i].matches(regex)) {
                arguments[i] = uniqueParameters.getParameter(Integer.parseInt(arguments[i]));
                if (arguments[i] == null) {
                    if (indexCounter == 1)
                        ConsolePrinter.printLog("Zadaný index neodpovídá žádnému z vyhledaných parametrů.");
                    else
                        ConsolePrinter.printLog(
                                "Nejméně jeden zadaný index neodpovídá žádnému z vyhledaných parametrů."
                        );
                    return false;
                }
            }
            if (arguments[i].startsWith(Constants.SKIP))
                arguments[i] = arguments[i].substring(1);
        }
        return true;
    }

}
