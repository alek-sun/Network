package ru.nsu.fit.fediaeva.netlab5server.matchers;

import java.util.Objects;

public class StringMatcher implements Matcher {
    String matchedString;
    public StringMatcher(String string){
        matchedString = string;
    }

    @Override
    public boolean match(String str) {
        return str.equals(matchedString);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StringMatcher that = (StringMatcher) o;
        return Objects.equals(matchedString, that.matchedString);
    }

    @Override
    public int hashCode() {
        return Objects.hash(matchedString);
    }
}
