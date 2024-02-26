package com.example;

import com.google.inject.Provides;

import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.OverheadTextChanged;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetModalMode;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@Slf4j
@PluginDescriptor(name = "Example")
public class ExamplePlugin extends Plugin
{
	static final int DEFAULT_NOTIFICATION_TEXT_COLOR = -1;
	@Inject
	private ClientThread clientThread;

	@Inject
	private Client client;

	@Inject
	private ExampleConfig config;

	@Override
	protected void startUp() throws Exception
	{
		log.info("Example started!");
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOGGED_IN)
		{
			// test setting hint arrow above currently logged in player
			Player currentPlayer = client.getLocalPlayer();
			client.setHintArrow(currentPlayer);


			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Example says " + config.greeting(), null);
		}
	}

	@Subscribe
	public void onOverheadTextChanged(OverheadTextChanged e)
	{
		if (e.getActor().equals(client.getLocalPlayer()))
		{
			displayNotification("League task", "Test popup for league task");
		}
	}

	@Override
	protected void shutDown() throws Exception
	{
		log.info("Example stopped!");
	}

	/**
	 * Displays a notification with a given title, text
	 *
	 * @param title
	 * @param text
	 */
	private void displayNotification(String title, String text)
	{
		displayNotification(title, text, DEFAULT_NOTIFICATION_TEXT_COLOR);
	}

	/**
	 * Displays a notification with a given title, text and optional color.
	 *
	 * @param title
	 * @param text
	 * @param color
	 */
	private void displayNotification(String title, String text, int color)
	{
		final int NOTIFICATION_DISPLAY_INIT = 3343;
		final int componentId = ((303 << 16) | 2); // Main component
		final int interfaceId = 660; // Notification

		WidgetNode widgetNode = client.openInterface(componentId, interfaceId, WidgetModalMode.MODAL_CLICKTHROUGH);

		// Set the initial title and text for the current notification
		client.runScript(NOTIFICATION_DISPLAY_INIT, title, text, color);

		clientThread.invokeLater(() -> {
			Widget w = client.getWidget(interfaceId, 1);
			if (w.getWidth() > 0)
			{
				return false;
			}

			client.closeInterface(widgetNode, true);
			return true;
		});
	}

	@Provides
	ExampleConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ExampleConfig.class);
	}
}
