package sig.meteos;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Random;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
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
	public static Random RANDOM = new Random(59);
	boolean singlePass=false;
	boolean singleFlag=false;
	static int targetFPS = 60;
	
	@Override
	public void create () {
		batch = new SpriteBatch();
		Planet.debugfont.setColor(Color.RED);
		LoadImages();
		cam = new PerspectiveCamera();
		view = new FitViewport(SCREEN_WIDTH,SCREEN_HEIGHT,cam);
		Gdx.input.setInputProcessor(new MouseProcessor());
	}
	
	public void resize(int width, int height) {
		view.update(width, height);
	}

	private void LoadImages() {
		onebyone = new Texture("1x1.png");
	}
	
	public static void scrollUp() {
		System.out.println("Target FPS is now: "+(++targetFPS));
	}
	
	public static void scrollDown() {
		System.out.println("Target FPS is now: "+(--targetFPS));
	}

	@Override
	public void render () {
		try {
			Thread.sleep(1000/targetFPS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		run();
		
		Gdx.gl.glClearColor(0, 0, 0.3f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.begin();
		Planet.GEOLYTE.DrawField(batch);
		batch.end();
		FrameCounter();
	}

	private void run() {
		if (framesPassed%60==0 && (!singlePass || (singlePass && !singleFlag))) {
			Planet.GEOLYTE.SpawnRandomBlock();
			singleFlag=true;
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
