package catan.ui;
import AI.LocalAIClient;
import catan.SettlersOfCatan;
import client.Client;
import client.LocalClient;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import enums.Difficulty;

public class GameScreenSettings implements Screen 
{

	final private SettlersOfCatan game;
	private boolean isAi;
    private int numAIs;
    private Difficulty difficulty = Difficulty.VERYEASY;
    private String username = "Player";
	final  Stage ui = new Stage(new ScreenViewport());

	
	GameScreenSettings(final SettlersOfCatan game){
		this.game = game;

		Gdx.input.setInputProcessor(ui);
		
		Image background = new Image(new Texture(Gdx.files.internal("splash.jpg")));
		background.setScaling(Scaling.fill);
		background.setFillParent(true);
		ui.addActor(background);

		VerticalGroup body = new VerticalGroup();
		body.setFillParent(true);
		body.padTop(50);
		body.space(60);
		ui.addActor(body);
		
		TextField settings = new TextField("Settings",SettlersOfCatan.getSkin());
		settings.setTextFieldListener((textField,c)-> textField.setText("Settings"));
		body.addActor(settings);
		
		addOptions(body);
		addSubmitButton(body);
		addBackButton(body);
		
		
	}
	
		private void addOptions(VerticalGroup body){
			TextField Name = new TextField("NAME", SettlersOfCatan.getSkin());
			Name.setTextFieldListener((textField,c)-> username = textField.getText());
			body.addActor(Name);
			
			CheckBox checkBox = new CheckBox("Play as AI", SettlersOfCatan.getSkin());
			checkBox.addListener(new InputListener(){
				public void touchUp(InputEvent event, float x, float y, int pointer, int button){
					super.touchUp(event, x, y, pointer, button);
					checkBox.toggle();
					isAi=checkBox.isChecked();
				}   public boolean touchDown(InputEvent event, float x, float y, int pointer, int button)
	            {
	                super.touchUp(event, x, y, pointer, button);
	                checkBox.toggle();
	                isAi = checkBox.isChecked();
	                return true;
	            }
	        });



			// Number of AIs to play with. Remaining are remote players implicitly
			TextField numAIsInput = new TextField("Number", SettlersOfCatan.getSkin());
			numAIsInput.setTextFieldListener(new TextField.TextFieldListener()
			{
				@Override
				public void keyTyped(TextField textField, char c)
				{
					textField.setText(String.valueOf(c));
					int num = 0;
					try
					{
						num = Integer.parseInt(textField.getText());
					}
					catch(NumberFormatException e)
					{
						textField.setText("Number");
					}
					numAIs = num;
				}
			});
			
	
			
		
			
			HorizontalGroup AISelection = new HorizontalGroup();
			CheckBox aiSettings = new CheckBox("Easy(Random)",SettlersOfCatan.getSkin());
			aiSettings.addListener(new InputListener(){
				 public void touchUp(InputEvent event, float x, float y, int pointer, int button)
		            {
		                super.touchUp(event, x, y, pointer, button);
						aiSettings.toggle();
		                difficulty = Difficulty.VERYEASY;
		            }

		            @Override
		            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button)
		            {
		                super.touchUp(event, x, y, pointer, button);
						aiSettings.toggle();
		                difficulty = Difficulty.VERYEASY;
		                return true;
		            }
		        });
		        CheckBox easySetting = new CheckBox("Medium", SettlersOfCatan.getSkin());
		        easySetting.addListener(new InputListener()
		        {
		            @Override
		            public void touchUp(InputEvent event, float x, float y, int pointer, int button)
		            {
		                super.touchUp(event, x, y, pointer, button);
		                easySetting.toggle();
		                difficulty = Difficulty.EASY;
		            }

		            @Override
		            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button)
		            {
		                super.touchUp(event, x, y, pointer, button);
		                easySetting.toggle();
		                difficulty = Difficulty.EASY;
		                return true;
		            }
		        });
				
			
			
		
			AISelection.addActor(numAIsInput);
			AISelection.addActor(aiSettings);
			AISelection.addActor(easySetting);
			body.addActor(checkBox);
			body.addActor(AISelection);
			
		}
		
		
		
		
		private void addBackButton(VerticalGroup body)
		{
			TextButton button = new TextButton("Main Menu", SettlersOfCatan.getSkin());
			button.addListener(new ChangeListener()
			{
				public void changed(ChangeEvent event, Actor actor)
				{
					game.setScreen(new MainMenuScreen(game));
				}
			});

			body.addActor(button);
		}

	private void addSubmitButton(VerticalGroup body)
	{
		TextButton button = new TextButton("Submit", SettlersOfCatan.getSkin());
		button.addListener(new ClickListener(){
			public void clicked(InputEvent event, float x, float y){
				super.clicked(event,x,y);
				Client c;
				if(isAi){
					c= new LocalAIClient(difficulty, game, username, numAIs);
				} else{
					c=new LocalClient(game,username,numAIs);
				}
				game.startNewServer(c);
				game.setScreen(new GameScreen(game));
			}
		});
		body.addActor(button);

	}
	@Override
	public void show()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void render(float delta)
	{
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		ui.act(delta);
		ui.draw();

	}

	@Override
	public void resize(int width, int height)
	{
		ui.getViewport().update(width, height, true);

	}

	@Override
	public void pause()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void resume()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void hide()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose()
	{
		ui.dispose();

	}

}
	
	
	
	

