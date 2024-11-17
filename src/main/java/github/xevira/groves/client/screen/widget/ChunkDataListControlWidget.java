package github.xevira.groves.client.screen.widget;

import github.xevira.groves.sanctuary.ClientGroveSanctuary;

public class ChunkDataListControlWidget extends ListControlWidget<ChunkDataWidget> {
    public ChunkDataListControlWidget(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    public boolean removeEntry(ClientGroveSanctuary.ChunkData chunk)
    {
        return getEntries().removeIf(entry -> entry.isChunkData(chunk));
    }

    public void updateChunkLoading(ClientGroveSanctuary.ChunkData chunk)
    {
        getEntries().stream().filter(entry -> entry.isChunkData(chunk)).findFirst().ifPresent(ChunkDataWidget::updateToggle);
    }
}
