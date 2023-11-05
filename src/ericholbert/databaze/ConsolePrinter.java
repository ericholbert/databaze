package ericholbert.databaze;

import java.util.ArrayList;
import java.util.Arrays;

import ericholbert.databaze.core.CorePreferences;
import ericholbert.databaze.database.UniqueParameters;

public final class ConsolePrinter {

    private static boolean isFoundItemPrintingEnabled = true;

    public static void printProgramHeader() {
        System.out.println();
        System.out.println(getCenteredLine("=====     /\\   ======    /\\    =====     /\\//  ====== ======"));
        System.out.println(getCenteredLine("||   |   // \\    ||     // \\   ||   |   // \\      //  ||    "));
        System.out.println(getCenteredLine("||   |  //===\\   ||    //===\\  ||===   //===\\    //   ||=== "));
        System.out.println(getCenteredLine("||   | //     \\  ||   //     \\ ||   | //     \\  //    ||    "));
        System.out.println(getCenteredLine("=====  ==    ==  ==   ==    == =====  ==    == ====== ======"));
        System.out.println();
    }

    //Je-li databaseName prislis dlouhe, zkrati se, aby nebyl zalomen radek.
    public static void printDatabaseHeader(String databaseName, int databaseSize) {
        String mutableWord = databaseSize == 1 ?
                "záznam" : databaseSize > 1 && databaseSize < 5 ?
                "záznamy" : "záznamů";
        String prefix = "| ";
        String sufix = " (" + databaseSize + " " + mutableWord + ") |";
        String mutableDatabaseName;
        int windowWidth = CorePreferences.getWindowWidth();
        if (prefix.length() + sufix.length() + databaseName.length() < windowWidth)
            mutableDatabaseName = databaseName;
        else
            mutableDatabaseName = databaseName.substring(0, windowWidth - sufix.length() - 3) + "...";
        String message = prefix + mutableDatabaseName + sufix;
        System.out.println(getCenteredLine(getMultipliedCharacter('~', message.length())) +
                "\n" +
                getCenteredLine(message) +
                "\n" +
                getCenteredLine(getMultipliedCharacter('~', message.length())));
        System.out.println();
    }

    public static void underlineCommand(int commandLength) {
        int windowWidth = CorePreferences.getWindowWidth();
        if (commandLength > windowWidth)
            commandLength = windowWidth;
        System.out.println(ConsolePrinter.getMultipliedCharacter('-', commandLength));
    }

    public static void printPlainText(String text, boolean newline) {
        System.out.println(getWrappedText(text, -1));
        if (newline)
            System.out.println();
    }

    public static void printLog(String message) {
        System.out.println(getWrappedText(message.substring(0, message.length() - 1) + "!", -1));
        System.out.println();
    }

    //Kazdy list musi mit stejny pocet polozek.
    public static void printTwoColumns(String[] columnOne, String[] columnTwo) {
        int columnOneWidth = CorePreferences.getWindowWidth() / 3;
        int columnTwoWidth = CorePreferences.getWindowWidth() / 3 * 2;
        ArrayList<ArrayList<String>> wrappedColumns = getWrappedColumns(
                getWrappedColumn(columnOneWidth, columnOne),
                getWrappedColumn(columnTwoWidth, columnTwo)
        );
        for (int i = 0; i < wrappedColumns.get(0).size(); i++) {
            printInColumnLine(columnOneWidth, wrappedColumns.get(0).get(i));
            printInColumnLine(columnTwoWidth, wrappedColumns.get(1).get(i));
            System.out.println();
        }
        System.out.println();
    }

    public static void printFoundItems(
            ArrayList<ArrayList<ArrayList<String>>> items,
            ArrayList<ArrayList<Integer>> parameterUsages,
            UniqueParameters uniqueParameters
    ) {
        if (!isFoundItemPrintingEnabled)
            return;
        for (ArrayList<ArrayList<String>> item : items) {
            ArrayList<String> columnOne = new ArrayList<>();
            ArrayList<String> columnTwo = new ArrayList<>();
            for (int i = 0; i < item.size(); i++) {
                StringBuilder parameterGroup = new StringBuilder();
                for (int j = 0; j < item.get(i).size(); j++) {
                    String parameter = item.get(i).get(j);
                    if (j == 0) {
                        if (uniqueParameters != null)
                            parameter = addIndex(parameter, uniqueParameters.getIndex(parameter));
                        if (parameterUsages != null)
                            parameter = addParameterUsage(parameter, parameterUsages.get(i).get(j));
                        columnOne.add(parameter);
                        continue;
                    }
                    if (j != 1) {
                        parameterGroup.append(" ~ ");
                    }
                    if (uniqueParameters != null)
                        parameter = addIndex(parameter, uniqueParameters.getIndex(parameter));
                    if (parameterUsages != null)
                        parameter = addParameterUsage(parameter, parameterUsages.get(i).get(j));
                    parameterGroup.append(parameter);
                }
                columnTwo.add(parameterGroup.toString());
            }
            printTwoColumns(columnOne.toArray(new String[0]), columnTwo.toArray(new String[0]));
        }
    }

    public static void setFoundItemPrinting(boolean b) {
        isFoundItemPrintingEnabled = b;
    }

    private static ArrayList<ArrayList<String>> getWrappedColumns(ArrayList<ArrayList<String>>... columns) {
        ArrayList<ArrayList<String>> wrappedColumns = new ArrayList<>();
        int[] linesCounter = new int[columns[0].size()];
        for (ArrayList<ArrayList<String>> column : columns)
            for (int j = 0; j < column.size(); j++)
                if (column.get(j).size() > linesCounter[j])
                    linesCounter[j] = column.get(j).size();
        for (ArrayList<ArrayList<String>> column : columns) {
            ArrayList<String> wrappedColumn = new ArrayList<>();
            for (int i = 0; i < column.size(); i++) {
                if (column.get(i).size() != linesCounter[i]) {
                    while (column.get(i).size() != linesCounter[i]) {
                        column.get(i).add("");
                    }
                }
                wrappedColumn.addAll(column.get(i));
            }
            wrappedColumns.add(wrappedColumn);
        }
        return wrappedColumns;
    }

    private static ArrayList<ArrayList<String>> getWrappedColumn(int columnWidth, String[] column) {
        ArrayList<ArrayList<String>> wrappedColumn = new ArrayList<>();
        for (String columnCell : column) {
            ArrayList<String> columnCellLines = new ArrayList<>();
            String[] lines = getWrappedText(columnCell, columnWidth).split("\n");
            columnCellLines.addAll(Arrays.asList(lines));
            wrappedColumn.add(columnCellLines);
        }
        return wrappedColumn;
    }

    private static String getWrappedText(String text, int columnWidth) {
        columnWidth = columnWidth == -1 ? CorePreferences.getWindowWidth() : columnWidth;
        /*
        Bude zalomen i radek, ktery se do columnWidth vejde beze zbytku, cimz vznikne na jeho konci mezera. Zmeni-li se
         columnWidth-- na columnWidth++, bude takovy radek zalomen.
         */
        columnWidth--;
        if (text.length() < columnWidth)
            return text;
        StringBuilder textWithSpaces = new StringBuilder();
        while (text.length() != 0) {
            String actualLine = text.length() > columnWidth ? text.substring(0, columnWidth) : text;
            int actualLineLength = actualLine.length();
            if (!actualLine.matches("(\\S* +\\S*)+") && actualLine.length() == columnWidth) {
                actualLine = actualLine.substring(0, actualLine.length() - 1);
                actualLine += " ";
                actualLineLength--;
            }
            text = text.substring(actualLineLength);
            textWithSpaces.append(actualLine);
        }
        text = textWithSpaces.toString();
        ArrayList<String> lines = new ArrayList<>();
        while (text.length() > columnWidth) {
            int lineBreakIndex = columnWidth;
            while (text.charAt(lineBreakIndex--) != ' ');
            String line = text.substring(0, ++lineBreakIndex);
            lines.add(line.trim());
            text = text.substring(lineBreakIndex);
        }
        lines.add(text.trim());
        StringBuilder editedText = new StringBuilder();
        for (int i = 0; i < lines.size(); i++) {
            editedText.append(lines.get(i));
            if (i != lines.size() - 1)
                editedText.append("\n");
        }
        return editedText.toString();
    }

    private static void printInColumnLine(int columnWidth, String columnLine) {
        System.out.print(columnLine + getMultipliedCharacter(' ', columnWidth - columnLine.length()));
    }

    private static String addParameterUsage(String parameter, int parameterUsage) {
        parameter += " (" + parameterUsage + "x)";
        return parameter;
    }

    private static String addIndex(String parameter, int index) {
        parameter += " [" + index + "]";
        return parameter;
    }

    /*
    Je-li delka line delsi nez screenWidth, nestane se nic. Funkce by proto mela byt volana pouze v pripade, je-li obsah
    line staticky.
     */
    private static String getCenteredLine(String line) {
        int windowWidth = CorePreferences.getWindowWidth();
        int leftGapSize = (windowWidth - line.length()) / 2;
        return getMultipliedCharacter(' ', leftGapSize) + line;
    }

    private static String getMultipliedCharacter(char character, int number) {
        number = number == -1 ? CorePreferences.getWindowWidth() : number;
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < number; i++)
            line.append(character);
        return line.toString();
    }

}
