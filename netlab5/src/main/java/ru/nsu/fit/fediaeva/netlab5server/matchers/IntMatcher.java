package ru.nsu.fit.fediaeva.netlab5server.matchers;

public class IntMatcher implements Matcher {
    @Override
    public boolean match(String str) {
        try {
            Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        return obj != null && getClass() == obj.getClass();
    }
}
