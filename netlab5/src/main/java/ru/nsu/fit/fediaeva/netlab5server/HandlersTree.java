package ru.nsu.fit.fediaeva.netlab5server;

import ru.nsu.fit.fediaeva.netlab5server.matchers.Matcher;
import ru.nsu.fit.fediaeva.netlab5server.matchers.StringMatcher;
import ru.nsu.fit.fediaeva.netlab5server.requesthandlers.RequestHandler;

import java.util.ArrayList;
import java.util.Iterator;

class HandlersTree {
    private ArrayList<HandlersTree> childs;
    private RequestHandler handler;
    private Matcher matcher;

    HandlersTree(){
        childs = new ArrayList<>();
        handler = null;
        matcher = new StringMatcher("root");
    }

    HandlersTree(Matcher matcher, RequestHandler requestHandler){
        childs = new ArrayList<>();
        this.matcher = matcher;
        handler = requestHandler;
    }

    HandlersTree childExists(Matcher matcher){
        for (HandlersTree child : childs) {
            if (child.getMatcher().equals(matcher)) {
                return child;
            }
        }
        return null;
    }

    void addHandler(Iterator<Matcher> pathIterator, RequestHandler addedHandler){
        if (pathIterator.hasNext()){
            Matcher pathPart = pathIterator.next();
            HandlersTree child;
            if ((child = childExists(pathPart)) == null){
                child = new HandlersTree(pathPart, null);
                childs.add(child);
            }
            child.addHandler(pathIterator, addedHandler);
        }  else {
            handler = addedHandler;
        }
    }


    RequestHandler getHandler(Iterator<String> pathIterator){
        if (pathIterator.hasNext()){
            String pathPart = pathIterator.next();
            HandlersTree next = getBranch(pathPart);
            if (next == null) return null;
            return next.getHandler(pathIterator);
        } else {
            return handler;
        }
    }

    private HandlersTree getBranch(String pathPart) {
        for (HandlersTree t : childs) {
            if (t.getMatcher().match(pathPart)){
                return t;
            }
        }
        return null;
    }

    Matcher getMatcher() {
        return matcher;
    }
}
