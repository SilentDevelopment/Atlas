package io.github.silentdevelopment.atlas.document;

import io.github.silentdevelopment.atlas.ConfigPath;

import java.util.List;
import java.util.Optional;

public interface CommentedConfigDocument extends ConfigDocument {

    Optional<List<String>> headerComment();

    List<String> comment(ConfigPath path);

}