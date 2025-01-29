package com.example.touchtyped.model;

import com.example.touchtyped.interfaces.KeyboardInterface;
import com.example.touchtyped.interfaces.KeypressListener;
import com.example.touchtyped.controller.GameViewController;

/**
 * Handles keyboard input events for the typing game.
 * Implements KeypressListener to receive keyboard events from KeyboardInterface.
 */
public class GameKeypressListener implements KeypressListener {
    private final GameViewController gameController;
    private final KeyboardInterface keyboardInterface;

    /**
     * Creates a new GameKeypressListener and registers it with the keyboard interface.
     * @param gameController The game controller to handle game logic
     * @param keyboardInterface The keyboard interface to listen to
     */
    public GameKeypressListener(GameViewController gameController, KeyboardInterface keyboardInterface) {
        this.gameController = gameController;
        this.keyboardInterface = keyboardInterface;
        // Register as a listener
        keyboardInterface.addKeypressListener(this);
    }

    /**
     * Called when a key is pressed.
     * Forwards the key press to the game controller if input is enabled.
     * @param key The key that was pressed
     */
    @Override
    public void onKeypress(String key) {
        if (!gameController.isInputDisabled()) {
            gameController.handleKeyPress(key);
        }
    }
} 