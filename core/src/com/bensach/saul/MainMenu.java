package com.bensach.saul;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.ai.GdxAI;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

import java.util.ArrayList;

public class MainMenu extends ApplicationAdapter implements InputProcessor {

	private int levelWidth = 300, levelHeight = 300,
	numRooms = 30, maxRoomWidth = 50, minRoomWidth = 30, maxRoomHeight = 50, minRoomHeight = 30;

	private SpriteBatch batch;
	private OrthographicCamera camera;
	private Vector2 posCamera;
	private Vector3 posMouse;
	private Level level;
	private Player player;

	
	@Override
	public void create () {
		long start = System.currentTimeMillis();
		batch = new SpriteBatch();
		camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		posCamera = new Vector2((levelWidth * 16) / 2, (levelHeight * 16) / 2);
		posMouse = new Vector3();
		level = new Level(levelWidth,levelHeight,numRooms,maxRoomWidth,minRoomWidth,maxRoomHeight,minRoomHeight);
		player = new Player(level.start.x*16, level.start.y*16, level);
		camera.position.set(player.getX() + player.getWidth() / 2, player.getY()+ player.getHeight() / 2, 0);
		camera.zoom -= 0.5f;
		long endTime = System.currentTimeMillis();
		long total = endTime - start;
		System.out.println("Tiempo total generacion: "+total * 0.001 +" segundos");
		Gdx.input.setInputProcessor(this);
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(0f, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		camera.unproject(posMouse);
		updateCamera();
		player.update(Gdx.graphics.getDeltaTime());
		level.draw(camera);
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		player.draw(batch);
		batch.end();
		if(Gdx.input.isKeyPressed(Input.Keys.R)){
			long start = System.currentTimeMillis();
			level.generateNewWorld();
			long endTime = System.currentTimeMillis();
			long total = endTime - start;
			player = new Player(level.start.x*16, level.start.y*16, level);
			System.out.println("Tiempo total generacion: "+total * 0.001 +" segundos");
		}

	}

	private void updateCamera(){
		//camera.position.set(posCamera.x, posCamera.y, 0);
		camera.position.set(player.getX() + player.getWidth() / 2, player.getY()+ player.getHeight() / 2, 0);
		camera.update();
	}

	@Override
	public boolean keyDown(int keycode) {
		switch (keycode){
			//case Input.Keys.R: level.generateNewWorld();break;
			case Input.Keys.Z:
				if(level.debug){
					level.debug = false;
				}else{
					level.debug = true;
				}
				break;
			case Input.Keys.W: player.setUp(true);break;
			case Input.Keys.A: player.setLeft(true);break;
			case Input.Keys.S: player.setDown(true);break;
			case Input.Keys.D: player.setRight(true);break;
			case Input.Keys.RIGHT: player.setcRight(true);break;
			case Input.Keys.LEFT: player.setcLeft(true);break;
			case Input.Keys.PLUS: level.rayHandler.setAmbientLight(level.r, level.g, level.b, level.a += 0.010f);break;
			case Input.Keys.MINUS: level.rayHandler.setAmbientLight(level.r, level.g, level.b, level.a -= 0.010f);break;
			case Input.Keys.F:
				if(player.isLight()){
					player.setLight(false);
				}else{
					player.setLight(true);
				}
				break;
			case Input.Keys.SPACE:player.setShoot(true);break;
		}
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		switch (keycode){
			case Input.Keys.W: player.setUp(false);break;
			case Input.Keys.A: player.setLeft(false);break;
			case Input.Keys.S: player.setDown(false);break;
			case Input.Keys.D: player.setRight(false);break;
			case Input.Keys.RIGHT: player.setcRight(false);break;
			case Input.Keys.LEFT: player.setcLeft(false);break;
			case Input.Keys.SPACE:player.setShoot(false);break;
		}
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		switch (amount){
			case 1: camera.zoom += 0.51f;break;
			case -1: camera.zoom -= 0.51f;break;
		}
		return false;
	}
}
