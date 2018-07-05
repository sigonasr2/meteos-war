package sig.meteos;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public enum Planet {
	GEOLYTE(0,
			new Texture[]{
					new Texture("block1-1.png"),
					new Texture("block1-2.png"),
					new Texture("block1-3.png"),
					new Texture("block1-4.png"),
					new Texture("block_unknown.png"),
					new Texture("block_unknown.png"),
					new Texture("block_unknown.png"),
					new Texture("block_unknown.png"),
					new Texture("block_unknown.png"),
					new Texture("block_unknown.png"),
					new Texture("block1-11.png"),
			},
			0.04f,
			0.5f,
			0.5f,
			9,
			1.5f
			),
	;
	
	int id;
	Texture[] block_tex;
	float gravity;
	float launch_power;
	float launch_power_mult;
	float max_fall_spd;
	int field_width;
	List<Block> blocklist = new ArrayList<Block>();
	List<BlockGroup> grouplist = new ArrayList<BlockGroup>();
	
	Planet(int id, Texture[] textures, float gravity, float launch_power, float launch_power_mult, int field_width, float max_fall_spd) {
		this.id = id;
		this.block_tex = textures;
		this.gravity = gravity;
		this.launch_power = launch_power;
		this.launch_power_mult = launch_power_mult;
		this.field_width = field_width;
		this.max_fall_spd = max_fall_spd;
	}
	
	public void run() {
		for (Block b : blocklist) {
			if (!b.isOnGround()) {
				b.yspd = Math.min(max_fall_spd, b.yspd + gravity);
				Block collide = null;
				if ((collide=ObstructedByBlock(b))!=null) {
					b.ypos = collide.ypos + MeteosWar.BLOCK_SIZE;
					BlockLanded(b);
				} else
				if (b.ypos-b.yspd < 32+MeteosWar.BLOCK_SIZE) { //Has it reached the bottom of the playing field?
					b.ypos = 32+MeteosWar.BLOCK_SIZE;
					BlockLanded(b);
				} else {
					b.ypos -= b.yspd;
				}
			}
		}
	}
	
	private void BlockLanded(Block b) {
		b.onGround = true;
		b.yspd = 0;
		

		List<Block> matched_blocks = new ArrayList<Block>();
		//System.out.println(blocklist);
		if (MatchFound(b,matched_blocks)) {
			matched_blocks.add(b);
			IgniteBlocks(matched_blocks);
		}
	}

	private void IgniteBlocks(List<Block> matched_blocks) {
		for (Block b : matched_blocks) {
			b.ignited=true;
			b.col = BlockColor.IGNITED;
		}
	}

	private boolean MatchFound(Block checkblock,List<Block> detectedblocks) {
		List<Block> horizontal_matches = new ArrayList<Block>();
		List<Block> vertical_matches = new ArrayList<Block>();
		for (int i=1 ; i>=-1 ; i-=2) { //xdir check
			Block detect_block;
			int j=i;
			while ((detect_block = Block.BlockExists(blocklist,checkblock.xpos + j*MeteosWar.BLOCK_SIZE,checkblock.ypos,checkblock.col,checkblock))!=null) {
				horizontal_matches.add(detect_block);
				j+=Math.signum(i);
			}
		}
		if (horizontal_matches.size()<=1) {
			horizontal_matches.clear();
			//Could not find 2 or more matching blocks, clearing this list.
		}
		for (int i=1 ; i>=-1 ; i-=2) { //ydir check
			Block detect_block;
			int j=i;
			while ((detect_block = Block.BlockExists(blocklist,checkblock.xpos,checkblock.ypos + j*MeteosWar.BLOCK_SIZE,checkblock.col,checkblock))!=null) {
				vertical_matches.add(detect_block);
				j+=Math.signum(i);
			}
		}
		if (vertical_matches.size()<=1) {
			vertical_matches.clear();
			//Could not find 2 or more matching blocks, clearing this list.
		}
		detectedblocks.addAll(horizontal_matches);
		detectedblocks.addAll(vertical_matches);
		return detectedblocks.size()>=2;
	}

	/**
	 * Checks whether this block, falling at a certain speed, is falling into another block.
	 * Returns the collided block, otherwise returns null;
	 */
	private Block ObstructedByBlock(Block checkblock) {
		//TODO Improve speed by dividing block checks into columns.
		for (Block b : blocklist) {
			if (b!=checkblock) {
				if (b.xpos == checkblock.xpos && b.ypos+MeteosWar.BLOCK_SIZE > checkblock.ypos
						&& b.isOnGround()) {
					//Yes, there is a collision.
					return b;
				}
			}
		}
		return null;
	}

	public void DrawField(SpriteBatch batch) {
		batch.setColor(Color.GRAY);
		batch.draw(MeteosWar.onebyone, 
				MeteosWar.SCREEN_WIDTH/2 - ((field_width/2+1) * MeteosWar.BLOCK_SIZE),
				32,
				MeteosWar.BLOCK_SIZE,10*MeteosWar.BLOCK_SIZE);
		batch.draw(MeteosWar.onebyone, 
				MeteosWar.SCREEN_WIDTH/2 + ((field_width/2+1) * MeteosWar.BLOCK_SIZE),
				32,
				MeteosWar.BLOCK_SIZE,10*MeteosWar.BLOCK_SIZE);
		batch.draw(MeteosWar.onebyone, 
				MeteosWar.SCREEN_WIDTH/2 - (field_width/2 * MeteosWar.BLOCK_SIZE),
				32,
				field_width*MeteosWar.BLOCK_SIZE,MeteosWar.BLOCK_SIZE);
		batch.setColor(Color.WHITE);
		for (Block b : blocklist) {
			b.draw(batch);
		}
	}
	
	void AddBlock(Block b) {
		blocklist.add(b);
	}
	
	public static void UnloadTextures() {
		for (Planet p : Planet.values()) {
			for (Texture t : p.block_tex) {
				t.dispose();
			}
		}
	}

	public void SpawnRandomBlock() {
		int rand = MeteosWar.RANDOM.nextInt(field_width);
		
		int baseX = MeteosWar.SCREEN_WIDTH/2 - ((field_width/2+1) * MeteosWar.BLOCK_SIZE)
				+ (rand+1)*MeteosWar.BLOCK_SIZE;
		
		Block b = new Block(baseX,MeteosWar.SCREEN_HEIGHT,BlockColor.GetRandomColor(2),this);
		
		AddBlock(b);
	}
}
