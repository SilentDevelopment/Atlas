package io.github.silentdevelopment.atlas.node;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface MutableConfigNode extends ConfigNode {

    Optional<MutableConfigNode> mutableChild(String key);

    Optional<MutableConfigNode> mutableElement(int index);

    Map<String, MutableConfigNode> mutableChildren();

    List<MutableConfigNode> mutableElements();

    MutableConfigNode childOrCreate(String key);

    MutableConfigNode elementOrCreate(int index);

    void setChild(String key, Object value);

    void setElement(int index, Object value);

    boolean removeChild(String key);

    boolean removeElement(int index);

    void setScalar(Object value);

    void setNull();

    void clear();

}