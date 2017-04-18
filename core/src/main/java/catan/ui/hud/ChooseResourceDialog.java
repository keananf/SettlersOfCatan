package catan.ui.hud;

import catan.SettlersOfCatan;
import client.Client;
import client.Turn;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import enums.ResourceType;
import intergroup.Requests;


public class ChooseResourceDialog extends Dialog
{
	private final Client client;
	private ResourceType chosenResource;
	
	Dialog dialog;
	Skin skin;
	Stage stage;
	ChooseResourceDialog(Skin skin, Client client, HeadsUpDisplay hud)
	{
		super("Choose Resource", skin);
		this.client = client;

		/*VerticalGroup vert = new VerticalGroup();
		final Table root = new Table();
		root.setFillParent(true);
		hud.getResources();
		addActor(root);*/

		// Add label
		
		
		
	}
	
	
	
		public void create(){
			skin = SettlersOfCatan.getSkin();
			stage = new Stage();
			Gdx.input.setInputProcessor(stage);
			//stage.setViewport(300,400);
		
		
		
		
		
		
		TextField offering = new TextField("Choose Resource", SettlersOfCatan.getSkin());
		offering.setTextFieldListener((textField, c) -> textField.setText("Choose Resource"));
		//vert.addActor(offering).addButton;

		
		dialog.getButtonTable().add(offering).width(900);
		for (ResourceType r : ResourceType.values())
		{
			if (r.equals(ResourceType.Generic)) continue;

			dialog.getButtonTable().add(r.name()).width(900);
			
			
		}

	}

	private void addCheckBoxes(ResourceType r, Dialog dialog)
	{
		CheckBox checkBox = new CheckBox(r.name(), SettlersOfCatan.getSkin());
		checkBox.addListener(new InputListener()
		{
			@Override
			public void touchUp(InputEvent event, float x, float y, int pointer, int button)
			{
				super.touchUp(event, x, y, pointer, button);
				checkBox.toggle();
				chosenResource = r;

				System.out.println("Chosen Resource: " + chosenResource.name());
			}

			@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button)
			{
				super.touchUp(event, x, y, pointer, button);
				checkBox.toggle();
				chosenResource = r;

				System.out.println("Chosen Resource: " + chosenResource.name());
				return true;
			}
		});
		TextButton b = new TextButton("Voem",SettlersOfCatan.getSkin());
		dialog.getButtonTable().add(b);

		dialog.getButtonTable().add(chosenResource.name());
	}

	private void addConfirmButtons()
	{
		TextButton button = new TextButton("Submit", SettlersOfCatan.getSkin());
		button.addListener(new ClickListener()
		{
			@Override
			public void clicked(InputEvent event, float x, float y)
			{
				super.clicked(event, x, y);

				// Set up choose
				Turn turn = new Turn(Requests.Request.BodyCase.CHOOSERESOURCE);
				turn.setChosenResource(chosenResource);
				client.acquireLocksAndSendTurn(turn);
			}
		});
		button(button, true).button("Cancel", false);
	}

	

	@Override
	protected void setStage(Stage stage) {
		
		
		
	}

	@Override
	public Table getContentTable() {
		// TODO Auto-generated method stub
		return super.getContentTable();
	}

	@Override
	public Table getButtonTable() {
		// TODO Auto-generated method stub
		return super.getButtonTable();
	}

	@Override
	public Dialog text(String text) {
		// TODO Auto-generated method stub
		return super.text(text);
	}

	@Override
	public Dialog text(String text, LabelStyle labelStyle) {
		// TODO Auto-generated method stub
		return super.text(text, labelStyle);
	}

	@Override
	public Dialog text(Label label) {
		// TODO Auto-generated method stub
		return super.text(label);
	}

	@Override
	public Dialog button(String text) {
		// TODO Auto-generated method stub
		return super.button(text);
	}

	@Override
	public Dialog button(String text, Object object) {
		// TODO Auto-generated method stub
		return super.button(text, object);
	}

	@Override
	public Dialog button(String text, Object object, TextButtonStyle buttonStyle) {
		// TODO Auto-generated method stub
		return super.button(text, object, buttonStyle);
	}

	@Override
	public Dialog button(Button button) {
		// TODO Auto-generated method stub
		return super.button(button);
	}

	@Override
	public Dialog button(Button button, Object object) {

		
		return super.button(button, object);
	}

	@Override
	public Dialog show(Stage stage, Action action) {
		
		return super.show(stage, action);
	}

	@Override
	public Dialog show(Stage stage) {
		// TODO Auto-generated method stub
		return super.show(stage);
	}

	@Override
	public void hide(Action action) {
		// TODO Auto-generated method stub
		super.hide(action);
	}

	@Override
	public void hide() {
		// TODO Auto-generated method stub
		super.hide();
	}

	@Override
	public void setObject(Actor actor, Object object) {
		// TODO Auto-generated method stub
		super.setObject(actor, object);
	}

	@Override
	public Dialog key(int keycode, Object object) {
		// TODO Auto-generated method stub
		return super.key(keycode, object);
	}

	@Override
	protected void result(Object object) {
		// TODO Auto-generated method stub
		super.result(object);
	}

	@Override
	public void cancel() {
		// TODO Auto-generated method stub
		super.cancel();
	}
	
	
	
}