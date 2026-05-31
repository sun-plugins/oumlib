package dev.oum.oumlib.event;

public final class ListenerHandle {

    private final Runnable unregisterAction;
    private volatile boolean cancelled;

    public ListenerHandle(Runnable unregisterAction) {
        this.unregisterAction = unregisterAction;
    }

    public void unregister() {
        if (cancelled) return;
        unregisterAction.run();
        cancelled = true;
    }

    public boolean isActive() {
        return !cancelled;
    }
}