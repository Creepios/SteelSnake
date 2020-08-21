// SteelSnake: File was created on 21.08.2020 by Creep (Discord: Creep#4924)

package de.creep.steelsnake.utils;

public class KBL {

    private final String key;
    private final String alternative;

    public KBL(String key, String alternative) {
        this.key = key;
        this.alternative = alternative;
    }

    public KBL(String key) {
        this.key = key;
        this.alternative = key;
    }

    public String getAlternative() {
        return alternative;
    }

    public String getKey() {
        return key;
    }
}
