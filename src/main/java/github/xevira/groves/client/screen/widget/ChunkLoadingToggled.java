package github.xevira.groves.client.screen.widget;

import github.xevira.groves.sanctuary.ClientGroveSanctuary;

@FunctionalInterface
public interface ChunkLoadingToggled {
    void onToggled(ClientGroveSanctuary.ChunkData data, boolean state);
}
