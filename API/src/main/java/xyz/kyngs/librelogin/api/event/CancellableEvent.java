package xyz.kyngs.librelogin.api.event;

public interface CancellableEvent {
    
    /**
     * Abort this event
     *
     * @param cancelled If set to true, this event will be aborted
     */
    void setCancelled(boolean cancelled);

    /**
     * Checks if the event is aborted
     *
     * @return Whether is the event aborted
     * */
    boolean isCancelled();
    
}