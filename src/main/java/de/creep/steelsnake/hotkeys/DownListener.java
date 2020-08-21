// SteelSnake: File was created on 19.08.2020 by Creep (Discord: Creep#4924)

package de.creep.steelsnake.hotkeys;

import com.tulskiy.keymaster.common.HotKey;
import com.tulskiy.keymaster.common.HotKeyListener;
import de.creep.steelsnake.SteelSnake;
import de.creep.steelsnake.utils.Direction;

public class DownListener implements HotKeyListener {

    public void onHotKey(HotKey hotKey) {
        SteelSnake.getInstance().setSnakeDirection(Direction.DOWN);
    }
}