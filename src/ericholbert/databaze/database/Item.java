package ericholbert.databaze.database;

import java.text.Normalizer;
import java.util.ArrayList;

final class Item {

    private final String idName = "ID";
    private final String idPrefix = "I-";
    /*
    Za parameters jsou povazovany groupNames a parameters, ktere pod groupNames spadaji. Je-li zmenena, pridana
    nebo smazana polozka z groupNames, zmeny se projevi nejen v groupNames, ale take v parameters.
     */
    private final ArrayList<String> groupNames = new ArrayList<>();
    private final ArrayList<String> parameters = new ArrayList<>();

    Item(String itemWithDelimiters, String itemNumber) {
        addId(itemNumber);
        String groupDelimiter = "\\^\\^";
        for (String unsortedParameters : itemWithDelimiters.split(groupDelimiter)) {
            String parametersDelimiter = "\\^_";
            boolean isGroup = true;
            for (String parameter : unsortedParameters.split(parametersDelimiter)) {
                if (isGroup) {
                    groupNames.add(parameter);
                    isGroup = false;
                }
                parameters.add(parameter);
            }
        }
    }

    Item(String itemNumber) {
        addId(itemNumber);
    }

    Item(String itemNumber, ArrayList<String> groupNames, ArrayList<String> parameters) {
        addId(itemNumber);
        for (int i = 1; i < groupNames.size(); i++)
            this.groupNames.add(groupNames.get(i));
        for (int i = 2; i < parameters.size(); i++)
            this.parameters.add(parameters.get(i));
    }

    /*
    Funkcnost je patrne zavisla na pritomnosti idName a vlastniho ID v parameters. Rovnez se nesmi opakovat parametry,
    nebot tehdy nastane kolize mezi parametrem a skupinou parametru.
     */
    ArrayList<ArrayList<String>> getItem() {
        ArrayList<ArrayList<String>> item = new ArrayList<>();
        for (int i = 0; i < parameters.size(); i++)
            if (groupNames.contains(parameters.get(i)) && !isHidden(parameters.get(i))) {
                ArrayList<String> group = new ArrayList<>();
                group.add(parameters.get(i));
                for (int j = i + 1; j < parameters.size(); j++) {
                    if (groupNames.contains(parameters.get(j))) {
                        i = j - 1;
                        break;
                    }
                    group.add(parameters.get(j));
                }
                item.add(group);
            }
        return item;
    }

    //Vrati item bez ID.
    String getItemWithDelimiters() {
        StringBuilder itemWithDelimiters = new StringBuilder();
        for (int i = 2; i < parameters.size(); i++) {
            if (i != 2 && groupNames.contains(parameters.get(i))) {
                String groupDelimiter = "^^";
                itemWithDelimiters.append(groupDelimiter);
            }
            if (!groupNames.contains(parameters.get(i))) {
                String parametersDelimiter = "^_";
                itemWithDelimiters.append(parametersDelimiter);
            }
            itemWithDelimiters.append(parameters.get(i));
        }
        return itemWithDelimiters.toString();
    }

    //Metoda predpoklada, ze prvni dve polozky v parameters jsou idName a vlastni ID.
    boolean isItemEmpty() {
        return parameters.size() == 2;
    }

    void addGroup(String groupName) {
        this.groupNames.add(groupName);
        parameters.add(groupName);
    }

    //TODO: zkusit zkombinovat s addParameter().
    void deleteGroup(String groupName) {
        groupNames.remove(groupName);
        for (int i = 0; i < parameters.size(); i++)
            if (parameters.get(i).equals(groupName)) {
                for (int j = i; j < parameters.size(); j++) {
                    if (groupNames.contains(parameters.get(j))) {
                        i = j;
                        break;
                    }
                    parameters.remove(parameters.get(j));
                    j--;
                }
            }
    }

    ArrayList<String> getGroup(String groupName) {
        ArrayList<String> group = new ArrayList<>();
        int firstGroupIndex = parameters.indexOf(groupName);
        for (int i = firstGroupIndex + 1; i < parameters.size(); i++) {
            if (!groupNames.contains(parameters.get(i)))
                group.add(parameters.get(i));
            else
                break;
        }
        return group;
    }

    ArrayList<String> getVisibleGroupNames() {
        ArrayList<String> visibleGroupNames = new ArrayList<>();
        for (String groupName : groupNames)
            if (!isHidden(groupName))
                visibleGroupNames.add(groupName);
        return visibleGroupNames;
    }

    boolean hasGroup(String groupName) {
        return groupNames.contains(groupName);
    }

    /*
    Prida parameter pred prvni dalsi parameter, ktery je v groupNames. Pokud je parameter posledni polozka v
    groupNames, ziska tento posledni index.
     */
    void addParameter(String groupName, String parameter) {
        int groupIndex = groupNames.indexOf(groupName);
        int nextGroupIndex;
        if (groupIndex != groupNames.size() - 1)
            nextGroupIndex = parameters.indexOf(groupNames.get(groupIndex + 1));
        else
            nextGroupIndex = parameters.size();
        parameters.add(nextGroupIndex, parameter);
    }

    void deleteParameter(String parameter) {
        while (parameters.contains(parameter)) {
            parameters.remove(parameter);
        }
    }

    void changeParameter(String oldParameter, String newParameter) {
        parameters.set(parameters.indexOf(oldParameter), newParameter);
        if (groupNames.contains(oldParameter))
            groupNames.set(groupNames.indexOf(oldParameter), newParameter);
    }

    ArrayList<String> getParameters(ArrayList<String> groupNames) {
        ArrayList<String> parameters = new ArrayList<>();
        for (String groupName : groupNames) {
            parameters.add(groupName);
            parameters.addAll(getGroup(groupName));
        }
        return parameters;
    }

    ArrayList<String> getAllParameters() {
        return parameters;
    }

    /*
    V parameters nelze mit dalsi ID nebo quasi ID, protoze tehdy by nefungovalo spravne vyhledavani.
    TODO: opravdu?
     */
    boolean isParameterAllowed(String parameter) {
        return parameter.equals(idName) || parameter.startsWith(idPrefix);
    }

    boolean hasParameter(String parameter) {
        return parameters.contains(parameter);
    }

    //Vyhledava \d, \w, \s apod.
    String matchesParameter(String indefiniteParameter) {
        indefiniteParameter = indefiniteParameter.
                replaceAll("\\?", "\\\\?").
                replaceAll("\\.", "\\\\.").
                replaceAll("\\*", "\\\\*").
                replaceAll("\\|", "\\\\|").
                replaceAll("\\[", "\\\\[").
                replaceAll("\\{", "\\\\{").
                replaceAll("\\$", "\\\\$");
        for (String parameter : parameters) {
            parameter = prepareForMatching(parameter);
            String regex = ".*" + prepareForMatching(indefiniteParameter) + ".*";
            if (parameter.matches(regex))
                return parameter;
        }
        return null;
    }

    String getId() {
        return parameters.get(1);
    }

    //Je pocitano s tim, ze ID ma v parameters prvni index.
    boolean hasId(String id) {
        return parameters.indexOf(id) == 1;
    }

    //Kazdy zaznam ma specialni ID, ktere slouzi pouze pro ucely programu. Generuje se vzdy znovu a nikdy neni ulozeno.
    void addId(String itemNumber) {
        groupNames.add(idName);
        parameters.add(idName);
        parameters.add(idPrefix + itemNumber);
    }

    private String prepareForMatching(String text) {
        text = Normalizer.normalize(text, Normalizer.Form.NFD);
        text = text.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        text = text.toLowerCase();
        return text;
    }

    private boolean isHidden(String parameter) {
        ArrayList<String> hiddenParameters = ItemsController.getHiddenParameters();
        return hiddenParameters != null && hiddenParameters.contains(prepareForMatching(parameter));
    }

}
