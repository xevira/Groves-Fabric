package github.xevira.groves.client.screen.widget;

import github.xevira.groves.poi.GrovesPOI;

@FunctionalInterface
public interface ChunkLoadingToggled {
    void onToggled(GrovesPOI.ClientGroveSanctuary.ChunkData data, boolean state);
}
