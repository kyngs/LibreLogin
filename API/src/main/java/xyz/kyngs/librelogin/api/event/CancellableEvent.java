/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.kyngs.librelogin.api.event;

/**
 * This interface is used to mark events that can be cancelled
 */
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