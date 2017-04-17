package catan.ui;

import AI.RemoteAIClient;
import catan.SettlersOfCatan;
import client.Client;
import client.RemoteClient;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

class RemoteGameScreen extends GameSetupScreen
{
	RemoteGameScreen(final SettlersOfCatan game) {
		super("Join remote game", game);

		SaneTextField host = new SaneTextField("Host");
		addPrimary(host);

		// start game button
		setSubmitListener(new ChangeListener() {
			public void changed(ChangeEvent event, Actor actor) {
				boolean valid;
				Client client;

				// Create proper type of remote client
				if (playerIsAI()) {
					client = new RemoteAIClient(host.getText(), getChosenDifficulty(), getUsername(), game);
					valid = ((RemoteAIClient) client).isInitialised();
				} else {
					client = new RemoteClient(host.getText(), getUsername(), game);
					valid = ((RemoteClient) client).isInitialised();
				}

				// Start up new thread for the newly created client
				if (valid) {
					game.startNewRemoteClient(client);
					game.setScreen(new GameScreen(game));
				}
			}
		});
	}
}
