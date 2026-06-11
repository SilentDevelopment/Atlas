package io.github.silentdevelopment.atlas.document;

import io.github.silentdevelopment.atlas.ConfigPath;

import java.util.List;

public interface MutableCommentedConfigDocument extends MutableConfigDocument, CommentedConfigDocument {

    void setHeaderComment(List<String> lines);

    void removeHeaderComment();

    void setComment(ConfigPath path, List<String> lines);

    void removeComment(ConfigPath path);

    void clearComments();

}