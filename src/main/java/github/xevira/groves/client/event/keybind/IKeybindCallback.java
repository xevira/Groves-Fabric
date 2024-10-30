package github.xevira.groves.client.event.keybind;

public interface IKeybindCallback {
    boolean onKeyAction(KeyAction action, Keybind key);
}
