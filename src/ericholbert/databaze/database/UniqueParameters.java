package ericholbert.databaze.database;

import java.util.ArrayList;

public final class UniqueParameters {

    private final ArrayList<String> uniqueParameters = new ArrayList<>();

    public int getIndex(String parameter) {
        if (uniqueParameters.contains(parameter))
            return uniqueParameters.indexOf(parameter);
        else
            return -1;
    }

    void set(ArrayList<String> parameters) {
        for (String parameter : parameters) {
            if (!uniqueParameters.contains(parameter))
                uniqueParameters.add(parameter);
        }
    }

    String getParameter(int index) {
        if (index < uniqueParameters.size())
            return uniqueParameters.get(index);
        else
            return null;
    }

    void clear() {
        uniqueParameters.clear();
    }

}
