package ericholbert.databaze.database;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import ericholbert.databaze.Constants;

final class ItemsController {

    private static final ArrayList<String> hiddenParameters = new ArrayList<>();
    private final ArrayList<Item> items = new ArrayList<>();
    //Obsahuje kopie referenci z items.
    private final ArrayList<Item> foundItems = new ArrayList<>();
    private final ArrayList<String> collectedIds = new ArrayList<>();
    //Slouzi k vytvoreni ID.
    private int itemNumber = 1;
    private ArrayList<ArrayList<Integer>> parameterUsages;

    static ArrayList<String> getHiddenParameters() {
        return hiddenParameters;
    }

    String getItemsWithDelimiter() {
        StringBuilder itemsWithDelimiters = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            if (i != 0) {
                String itemsDelimiter = "^[";
                itemsWithDelimiters.append(itemsDelimiter);
            }
            itemsWithDelimiters.append(items.get(i).getItemWithDelimiters());
        }
        return itemsWithDelimiters.toString();
    }

    void setItems(String unsortedItems) {
        if (unsortedItems.isEmpty())
            return;
        String itemsDelimiter = "\\^\\[";
        for (String unsortedItem : unsortedItems.split(itemsDelimiter)) {
            Item item = new Item(unsortedItem, Integer.toString(itemNumber++));
            items.add(item);
        }
    }

    int getItemsSize() {
        return items.size();
    }

    /*
    Vsechny inputParameters musi byt pritomny v item, aby byl tento pridan do foundItems. Pokud jsou matches > pocet
    inputParameters, znamena to, ze Item ma duplicitni hodnoty. Tyto vsak v kodu nemuseji byt povoleny.
    */
    boolean findItems(boolean preciseMatch, String... inputParameters) {
        clearFoundItems();
        for (Item item : items) {
            int matches = 0;
            for (int j = 0; j < inputParameters.length; j++) {
                if (isFound(item, inputParameters[j], preciseMatch))
                    matches++;
                if (j == inputParameters.length - 1 && matches >= inputParameters.length)
                    foundItems.add(item);
            }
        }
        return !foundItems.isEmpty();
    }

    //Bude ulozen ten item, ktery ma alespon jednu polozku z inputParameters.
    boolean findMoreItems(boolean preciseMatch, String... inputParameters) {
        clearFoundItems();
        for (Item item : items)
            for (int j = 0; j < inputParameters.length; j++) {
                if (isFound(item, inputParameters[j], preciseMatch) && !foundItems.contains(item))
                    foundItems.add(item);
            }
        return !foundItems.isEmpty();
    }

    //Lze nahradit za hledani podle "ID", tj. skupiny parametru.
    boolean findAllItems() {
        clearFoundItems();
        foundItems.addAll(items);
        return !foundItems.isEmpty();
    }

    ArrayList<ArrayList<ArrayList<String>>> getFoundItems() {
        ArrayList<ArrayList<ArrayList<String>>> items = new ArrayList<>();
        for (Item foundItem : foundItems)
            items.add(foundItem.getItem());
        if (items.size() > 0)
            return items;
        else
            return null;
    }

    //TODO: rozdelit, zucelnit.
    ArrayList<ArrayList<ArrayList<String>>> getMergedParametersASetParameterUsages() {
        ArrayList<ArrayList<String>> mergedGroups = new ArrayList<>();
        ArrayList<String> groupNames = new ArrayList<>();
        ArrayList<String> parameters = new ArrayList<>();
        HashMap<String, Integer> groupNameUsages = new HashMap<>();
        for (Item foundItem : foundItems) {
            ArrayList<String> itemGroupNames = foundItem.getVisibleGroupNames();
            for (String groupName : itemGroupNames) {
                if (!groupNames.contains(groupName)) {
                    groupNames.add(groupName);
                    groupNameUsages.put(groupName, 1);
                } else
                    groupNameUsages.put(groupName, groupNameUsages.get(groupName) + 1);
            }
            parameters.addAll(foundItem.getParameters(itemGroupNames));
        }
        for (String groupName : groupNames) {
            ArrayList<String> group = new ArrayList<>();
            group.add(groupName);
            mergedGroups.add(group);
        }
        ArrayList<ArrayList<ArrayList<String>>> al = new ArrayList<>();
        al.add(mergedGroups);
        HashMap<String, Integer> parameterUsages = new HashMap<>();
        for (ArrayList<String> group : mergedGroups) {
            parameterUsages.put(group.get(0), groupNameUsages.get(group.get(0)));
            for (int i = 0; i < parameters.size(); i++) {
                if (group.get(0).equals(parameters.get(i))) {
                    for (int j = i + 1; j < parameters.size(); j++) {
                        if (groupNames.contains(parameters.get(j))) {
                            i = j;
                            break;
                        }
                        if (!group.contains(parameters.get(j))) {
                            group.add(parameters.get(j));
                            parameterUsages.put(parameters.get(j), 1);
                        }
                        else
                            parameterUsages.put(parameters.get(j), parameterUsages.get(parameters.get(j)) + 1);
                    }
                }
            }
        }
        ArrayList<ArrayList<Integer>> onlyUsages = new ArrayList<>();
        for (ArrayList<String> mergedGroup : mergedGroups) {
            ArrayList<Integer> group = new ArrayList<>();
            for (String parameter : mergedGroup) {
                group.add(parameterUsages.get(parameter));
            }
            onlyUsages.add(group);
        }
        this.parameterUsages = onlyUsages;
        return al;
    }

    //Ma se volat pouze po volani getMergedParametersASetParameterUsages().
    ArrayList<ArrayList<Integer>> getParameterUsages() {
        return parameterUsages;
    }

    int getFoundItemSize() {
        return foundItems.size();
    }

    boolean isFoundAnyItem() {
        return foundItems.size() != 0;
    }

    String addItem(String... inputGroups) {
        Item item = new Item(Integer.toString(itemNumber++));
        items.add(item);
        for (String group : inputGroups)
            if (!item.isParameterAllowed(group) && !item.hasGroup(group))
                item.addGroup(group);
        return item.getId();
    }

    boolean deleteItem(String id) {
        for (Item item : foundItems)
            if (item.hasId(id)) {
                items.remove(item);
                foundItems.remove(item);
                return true;
            }
        return false;
    }

    void deleteItems() {
        items.removeAll(foundItems);
        foundItems.clear();
    }

    void collectId(String id) {
        collectedIds.add(id);
    }

    String[] getCollectedIds() {
        return collectedIds.toArray(new String[0]);
    }

    Item getDuplicatedItem(String id) {
        for (Item item : items) {
            if (item.hasId(id)) {
                Item duplicatedItem = new Item(
                        Integer.toString(itemNumber++),
                        item.getVisibleGroupNames(),
                        item.getAllParameters()
                );
                items.add(duplicatedItem);
                return items.get(items.size() - 1);
            }
        }
        return null;
    }

    boolean addGroup(String id, String groupName) {
        for (Item item : foundItems)
            if (item.hasId(id) &&
                    !item.hasGroup(groupName) &&
                    !item.hasParameter(groupName) &&
                    !item.isParameterAllowed(groupName)) {
                item.addGroup(groupName);
                return true;
            }
        return false;
    }

    /*
    Vrati pocet vykonanych prikazu a pocet foundItems, aby mohla byt na zaklade techto vypocitana uspesnost prikazu.
     */
    int[] addGroups(String groupName) {
        int matchCounter = 0;
        for (Item item : foundItems)
            if (!item.hasGroup(groupName) &&
                    !item.hasParameter(groupName) &&
                    !item.isParameterAllowed(groupName)) {
                item.addGroup(groupName);
                matchCounter++;
            }
        return new int[] {matchCounter, foundItems.size()};
    }

    boolean deleteGroup(String id, String groupName) {
        for (Item item : foundItems)
            if (item.hasId(id) &&
                    item.hasGroup(groupName) &&
                    !item.isParameterAllowed(groupName)) {
                item.deleteGroup(groupName);
                if (item.isItemEmpty()) {
                    foundItems.remove(item);
                    items.remove(item);
                }
                return true;
            }
        return false;
    }

    int[] deleteGroups(String groupName) {
        int matchCounter = 0;
        int foundItemsSize = foundItems.size();
        Iterator<Item> i = foundItems.iterator();
        while (i.hasNext()) {
            Item item = i.next();
            if (item.hasGroup(groupName) && !item.isParameterAllowed(groupName)) {
                item.deleteGroup(groupName);
                if (item.isItemEmpty()) {
                    i.remove();
                    items.remove(item);
                }
                matchCounter++;
            }
        }
        return new int[] {matchCounter, foundItemsSize};
    }

    ArrayList<String> getGroup(String id, String groupName) {
        for (Item foundItem : foundItems) {
            if (foundItem.hasId(id) && foundItem.hasGroup(groupName))
                return foundItem.getGroup(groupName);
        }
        return null;
    }

    /*
    TODO: pridavat vice parametru by melo byt dovoleno. Bude vsak muset byt kontrolovano, aby pridavany parametr
    nebyl nazev skupiny, jinak vznikne chyba (viz SEZNAM ZMEN). Pote bude rovnez vhodne napsat novou metodu, ktera
    umozni pridat parametr do specificke skupiny, nikoliv pouze hromadne.
     */
    boolean addParameter(String id, String groupName, String parameter) {
        for (Item item : foundItems)
            if (item.hasId(id) &&
                    item.hasGroup(groupName) &&
                    !item.hasParameter(parameter) &&
                    !item.isParameterAllowed(groupName) &&
                    !item.isParameterAllowed(parameter)) {
                item.addParameter(groupName, parameter);
                return true;
            }
        return false;
    }

    //Viz addGroups(), addParameter().
    int[] addParameters(String groupName, String parameter) {
        int matchCounter = 0;
        for (Item item : foundItems)
            if (item.hasGroup(groupName) &&
            		!item.hasParameter(parameter) &&
                    !item.isParameterAllowed(groupName) &&
                    !item.isParameterAllowed(parameter)) {
                item.addParameter(groupName, parameter);
                matchCounter++;
            }
        return new int[] {matchCounter, foundItems.size()};
    }

    boolean deleteParameter(String id, String parameter) {
        for (Item item : foundItems)
            if (item.hasId(id) &&
                    item.hasParameter(parameter) &&
                    !item.hasGroup(parameter) &&
                    !item.isParameterAllowed(parameter)) {
                item.deleteParameter(parameter);
                return true;
            }
        return false;
    }

    //Viz addGroups().
    int[] deleteParameters(String parameter) {
        int matchCounter = 0;
        for (Item item : foundItems)
            if (item.hasParameter(parameter) &&
                    !item.hasGroup(parameter) &&
                    !item.isParameterAllowed(parameter)) {
                item.deleteParameter(parameter);
                matchCounter++;
            }
        return new int[] {matchCounter, foundItems.size()};
    }

    boolean changeParameter(String id, String oldParameter, String newParameter) {
        for (Item item : foundItems)
            if (item.hasId(id) &&
                    !(item.hasParameter(oldParameter) && item.hasParameter(newParameter)) &&
                    item.hasParameter(oldParameter) &&
                    !item.isParameterAllowed(oldParameter) &&
                    !item.isParameterAllowed(newParameter)) {
                item.changeParameter(oldParameter, newParameter);
                return true;
            }
        return false;
    }

    //Viz addGroups().
    int[] changeParameters(String oldParameter, String newParameter) {
        int matchCounter = 0;
        for (Item item : foundItems)
            if (item.hasParameter(oldParameter) &&
                    !(item.hasParameter(oldParameter) && item.hasParameter(newParameter)) &&
                    !item.isParameterAllowed(oldParameter) &&
                    !item.isParameterAllowed(newParameter)) {
                item.changeParameter(oldParameter, newParameter);
                matchCounter++;
            }
        return new int[] {matchCounter, foundItems.size()};
    }

    ArrayList<String> getUniqueParameters() {
        ArrayList<String> uniqueParameters = new ArrayList<>();
        for (Item item : foundItems) {
            for (String parameter : item.getParameters(item.getVisibleGroupNames()))
                if (!uniqueParameters.contains((parameter)))
                    uniqueParameters.add(parameter);
        }
        return uniqueParameters;
    }


    //Uspesnost prikazu je vypocitana na zaklade souhrneho poctu vsech parametru prislusne groupName.
    int[] openFile(Database database, String id, String groupName) {
        int matches = 0;
        int attempts = 0;
        ArrayList<String> group = getGroup(id, groupName);
        if (group == null)
            return new int[] {0, 0};
        for (String parameter : group) {
            attempts++;
            if (database.openFile(parameter))
                matches++;
        }
       return new int[] {matches, attempts};
    }

    //Viz openFile(). Ad: tyka se kazde polozky ve foundItems.
    int[] openFiles(Database database, String groupName) {
        int matches = 0;
        int attempts = 0;
        for (Item foundItem : foundItems) {
            int[] matchesAndAttempts = openFile(database, foundItem.getId(), groupName);
            matches += matchesAndAttempts[0];
            attempts += matchesAndAttempts[1];
        }
        return new int[] {matches, attempts};
    }

    //Viz openFile().
    int[] deleteFile(Database database, String id, String groupName) {
        int matches = 0;
        int attempts = 0;
        ArrayList<String> group = getGroup(id, groupName);
        if (group == null)
            return new int[] {0, 0};
        for (String parameter : group) {
            attempts++;
            if (database.deleteFile(parameter))
                matches++;
        }
        return new int[] {matches, attempts};
    }

    //Viz openFiles().
    int[] deleteFiles(Database database, String groupName) {
        int matches = 0;
        int attempts = 0;
        for (Item foundItem : foundItems) {
            int[] matchesAndAttempts = deleteFile(database, foundItem.getId(), groupName);
            matches += matchesAndAttempts[0];
            attempts += matchesAndAttempts[1];
        }
        return new int[] {matches, attempts};
    }

    private void clearFoundItems() {
        foundItems.clear();
        hiddenParameters.clear();
    }

    private boolean isFound(Item item, String parameter, boolean preciseMatch) {
        boolean hide = false;
        if (parameter.startsWith(Constants.HIDE) && parameter.length() > 1) {
            parameter = parameter.substring(1);
            hide = true;
        }
        boolean reverseMatch = parameter.startsWith(Constants.REVERSE) && parameter.length() > 1;
        if (reverseMatch) {
            parameter = parameter.substring(1);
        }
        String completeParameter = item.matchesParameter(parameter);
        boolean isFound = preciseMatch ?
                item.hasParameter(parameter) :
                completeParameter != null;
        if (hide)
            if (!hiddenParameters.contains(completeParameter))
                hiddenParameters.add(completeParameter);
        return reverseMatch != isFound;
    }

}
