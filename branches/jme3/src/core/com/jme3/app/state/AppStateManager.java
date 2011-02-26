/*
 * Copyright (c) 2009-2010 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.jme3.app.state;

import com.jme3.app.Application;
import com.jme3.renderer.RenderManager;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The <code>AppStateManager</code> holds a list of {@link AppState}s which
 * it will update and render.<br/>
 * When an {@link AppState} is attached or detached, the
 * {@link AppState#stateAttached(com.jme3.app.state.AppStateManager) } and
 * {@link AppState#stateDetached(com.jme3.app.state.AppStateManager) } methods
 * will be called respectively. 
 *
 * @author Kirill Vainer
 */
public class AppStateManager {

    private static final Logger logger = Logger.getLogger(AppStateManager.class.getName());

    // List used for iteration only
    private final ArrayList<AppState> iterationTemp = new ArrayList<AppState>();

    private final ArrayList<AppState> addedStates = new ArrayList<AppState>();
    private final ArrayList<AppState> deletedStates = new ArrayList<AppState>();

    /**
     * States that are on this manager
     */
    private final ArrayList<AppState> states = new ArrayList<AppState>();

    /**
     * This list is maintained based on the {@link AppStateManager#addedStates}
     * and {@link AppStateManager#deletedStates} lists.
     */
    private final ArrayList<AppState> updateStates = new ArrayList<AppState>();
    private final Application app;

    public AppStateManager(Application app){
        this.app = app;
    }

    /**
     * Attach a state to the AppStateManager, the same state cannot be attached
     * twice.
     *
     * @param state The state to attach
     * @return True if the state was successfully attached, false if the state
     * was already attached.
     */
    public boolean attach(AppState state){
        synchronized (states){
            if (!states.contains(state)){
                assert !addedStates.contains(state);
                
                if (deletedStates.contains(state)){
                    deletedStates.remove(state);
                }

                addedStates.add(state);
                states.add(state);
                return true;
            }else{
                return false;
            }
        }
    }

    /**
     * Detaches the state from the AppStateManager. 
     *
     * @param state The state to detach
     * @return True if the state was detached successfully, false
     * if the state was not attached in the first place.
     */
    public boolean detach(AppState state){
        synchronized (states){
            if (states.contains(state)){
                states.remove(state);
                if (addedStates.contains(state)){
                    addedStates.remove(state);
                }
                if (updateStates.contains(state)){
                    deletedStates.add(state);
                }
                return true;
            }else{
                return false;
            }
        }
    }

    /**
     * Check if a state is attached or not.
     *
     * @param state The state to check
     * @return True if the state is currently attached to this AppStateManager.
     * 
     * @see AppStateManager#attach(com.jme3.app.state.AppState)
     */
    public boolean hasState(AppState state){
        synchronized (states){
            return states.contains(state);
        }
    }

    /**
     * Returns the first state that is an instance of subclass of the specified class.
     * Only returns states that have already been initialized
     *
     * @param <T>
     * @param stateClass
     * @return First attached state that is an instance of stateClass
     */
    public <T extends AppState> T getState(Class<T> stateClass){
        synchronized (states){
            int num = updateStates.size();
            for (int i = 0; i < num; i++){
                AppState state = updateStates.get(i);
                if (stateClass.isAssignableFrom(state.getClass())){
                    return (T) state;
                }
            }
        }
        return null;
    }

    /**
     * Calls update for attached states, do not call directly.
     * @param tpf Time per frame.
     */
    public void update(float tpf){
        synchronized (states){
            while (deletedStates.size() > 0 
                || addedStates.size() > 0){
                // Delete states requested for removal
                iterationTemp.addAll(deletedStates);
                deletedStates.clear();

                int num = iterationTemp.size();
                for (int i = 0; i < num; i++){
                    AppState state = iterationTemp.get(i);

                    // NOTE: This call could add/remove states,
                    // in that case, we go through the loop again.
                    logger.log(Level.INFO, "Detaching state: {0}", state);
                    state.stateDetached(this);
                    
                    updateStates.remove(state);
                }
                iterationTemp.clear();
            
                // Initialize any added states
                // Also call stateAttached
                iterationTemp.addAll(addedStates);
                addedStates.clear();

                num = iterationTemp.size();
                for (int i = 0; i < num; i++){
                    AppState state = iterationTemp.get(i);
                    if (!state.isInitialized()){
                        // NOTE: This call could add/remove states

                        logger.log(Level.INFO, "Initializing state: {0}", state);
                        state.initialize(this, app);
                    }

                    // NOTE: This call could add/remove states
                    logger.log(Level.INFO, "Attaching state: {0}", state);
                    state.stateAttached(this);
                    updateStates.add(state);
                }
                iterationTemp.clear();
            }

            int num = updateStates.size();
            for (int i = 0; i < num; i++){
                AppState state = updateStates.get(i);
                if (state.isActive()){
                    // NOTE: This call could add/remove states
                    // But it will be handled on the next iteration
                   state.update(tpf);
                }
            }
        }
    }

    /**
     * Calls render for all attached states, do not call directly.
     * @param rm The RenderManager
     */
    public void render(RenderManager rm){
        synchronized (states){
            int num = updateStates.size();
            for (int i = 0; i < num; i++){
                AppState state = updateStates.get(i);
                if (state.isActive()) {
                    // NOTE: This call could add/remove states
                    // But it will be handled on the next iteration
                   state.render(rm);
                }
            }
        }
    }

    /**
     * Calls render for all attached states, do not call directly.
     * @param rm The RenderManager
     */
    public void postRender(){
        synchronized (states){
            int num = updateStates.size();
            for (int i = 0; i < num; i++){
                AppState state = updateStates.get(i);
                if (state.isActive()) {
                    // NOTE: This call could add/remove states
                    // But it will be handled on the next iteration
                   state.postRender();
                }
            }
        }
    }

    /**
     * Calls cleanup on attached states, do not call directly.
     */
    public void cleanup(){
        synchronized (states){
            for (int i = 0; i < updateStates.size(); i++){
                AppState state = updateStates.get(i);
                if (state.isInitialized()){
                    // NOTE: This call could add/remove states
                    // But it will be handled on the next iteration
                    logger.log(Level.INFO, "Cleaning state: {0}", state);
                    state.cleanup();
                }
            }
            updateStates.clear();
            addedStates.clear();
            deletedStates.clear();
            states.clear();
        }
    }
}
