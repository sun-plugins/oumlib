package dev.oum.oumlib.scheduler;

import java.util.function.BooleanSupplier;

public final class TaskHandle {

    private final Runnable cancelAction;
    private final BooleanSupplier isCancelledSupplier;

    public TaskHandle(Runnable cancelAction, BooleanSupplier isCancelledSupplier) {
        this.cancelAction = cancelAction;
        this.isCancelledSupplier = isCancelledSupplier;
    }

    public void cancel() {
        cancelAction.run();
    }

    public boolean isCancelled() {
        return isCancelledSupplier.getAsBoolean();
    }

    public boolean isRunning() {
        return !isCancelled();
    }
}