package ru.nsu.fit.fediaeva.netlab5server.requesthandlers;

import ru.nsu.fit.fediaeva.netlab5server.Message;
import ru.nsu.fit.fediaeva.netlab5server.ServerException;

public interface RequestHandler {
    Message getResponse(Message request) throws ServerException;
}
