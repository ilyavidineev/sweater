package ru.racoon.sweater.domain.util;

import ru.racoon.sweater.domain.User;

public abstract class MessageHelper {

    public static String getAuthorName(User author) {
        return author != null ? author.getUsername() : "<none>";
    }
}
