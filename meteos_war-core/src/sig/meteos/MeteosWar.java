package sig.meteos;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Random;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class MeteosWar extends ApplicationAdapter {
	final public static int BLOCK_SIZE = 16;
	final public static int SCREEN_WIDTH = BLOCK_SIZE*20; 
	final public static int SCREEN_HEIGHT = BLOCK_SIZE*14;
	
	SpriteBatch batch;
	public static Texture onebyone;
	Viewport view;
	Camera cam;
	Calendar lastCheck = Calendar.getInstance();
	int framesPassed=0;
	public static Random RANDOM = new Random();
	
	@Override
	public void create () {
		batch = new SpriteBatch();
		LoadImages();
		cam = new PerspectiveCamera();
		view = new FitViewport(SCREEN_WIDTH,SCREEN_HEIGHT,cam);
	}
	
	public void resize(int width, int height) {
		view.update(width, height);
	}

	private void LoadImages() {
		onebyone = new Texture("1x1.png");
	}

	@Override
	public void render () {
		run();
		
		Gdx.gl.glClearColor(0, 0, 0.3f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.begin();
		Planet.GEOLYTE.DrawField(batch);
		batch.end();
		FrameCounter();
	}

	private void run() {
		if (framesPassed%120==0) {
			Planet.GEOLYTE.SpawnRandomBlock();
		}
		Planet.GEOLYTE.run();
	}

	private void FrameCounter() {
		framesPassed++;
		if (lastCheck.getTime().getSeconds()!=Calendar.getInstance().getTime().getSeconds()) {
			System.out.println("FPS: "+framesPassed);
			framesPassed=0;
			lastCheck=Calendar.getInstance();
		}
	}
	
	@Override
	public void dispose () {
		batch.dispose();
		onebyone.dispose();
		Planet.UnloadTextures();
	}
}
