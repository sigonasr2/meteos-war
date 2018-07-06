package sig.meteos;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
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
			-0.04f,
			2.0f,
			0.5f,
			9,
			-1.5f
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
	ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	public static int BLOCK_ID=0;
	public static int GROUP_ID=0;
	public static BitmapFont debugfont = new BitmapFont(Gdx.files.internal("fonts/main.fnt"));
	
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
				if (b.group==null) {
					b.yspd = Math.max(max_fall_spd, b.yspd + gravity);
				}
				Block collide = null;
				if ((collide=ObstructedByBlock(b))!=null) {
					if (b.group!=null && b.group.id==2) {
						System.out.println("Collision from block "+b.id+" in group 2.");
					}
					if (b.ypos>collide.ypos && (b.group==null || b.group!=collide.group)) {
						if (b.group!=null && b.group.id==2) {
							System.out.println("Collision in group "+b.group.id);
						}
						b.ypos = collide.ypos + MeteosWar.BLOCK_SIZE;
						b.yspd = collide.yspd;
						BlockLanded(b,collide);
					}
				} else
				if (b.ypos+b.yspd < 32+MeteosWar.BLOCK_SIZE) { //Has it reached the bottom of the playing field?
					b.ypos = 32+MeteosWar.BLOCK_SIZE;
					if (b.group!=null) {
						b.group.yvel=0;
						b.group.landed=true;
					}
					BlockLanded(b);
				} else {
					if (b.group==null) {
						b.ypos += b.yspd;
					}
				}
			}
		}
		scheduler.schedule(()->{
			for (Block b: blocklist) {
				if (b.isOnGround() || b.group!=null) {
					List<Block> matched_blocks = new ArrayList<Block>();
					//System.out.println(blocklist);
					if (MatchFound(b,matched_blocks)) {
						matched_blocks.add(b);
						IgniteBlocks(matched_blocks);
					}
				}
			}
		}, 100, TimeUnit.MILLISECONDS);
		for (BlockGroup bg : grouplist) {
			if (!bg.landed) {
				bg.yvel = Math.max(max_fall_spd,  bg.yvel + gravity);
				for (Block b : bg.blocks) {
					b.yspd = bg.yvel;
					b.ypos += b.yspd;
					//System.out.println("Moving Block "+b);
				}
			} else {
				bg.yvel=0;
				//System.out.println("Block group has landed!");
			}
		}
	}
	
	private void BlockLanded(Block b) {
		BlockLanded(b,null);
	}
	
	private void BlockLanded(Block b, Block collide) {
		if (b.group==null || (b.group.landed)) {
			b.onGround = true;
			System.out.println("Block "+b+" is on the ground.");
		}
		b.yspd = 0;
		if (collide!=null) {
			if (collide.group!=null) {
				b.onGround = false;
				b.yspd = collide.group.yvel;
				if (b.group==null) {
					b.group = collide.group;
					b.group.addBlocks(b);
				} else {
					MergeGroup(b.group,collide.group);
				}
			}
		}
	}

	private void MergeGroup(BlockGroup group1, BlockGroup group2) {
		for (int i=0;i<group1.blocks.size();i++) {
			AddToBlockGroup(group1.blocks.get(i),group2);
			RemoveFromBlockGroup(group1.blocks.get(i--),group1);
		}
		if (grouplist.remove(group1)) {
			System.out.println("Group "+group1.id+" has been deleted.");	
		}
	}

	private void RemoveFromBlockGroup(Block b, BlockGroup group1) {
		group1.blocks.remove(b);
		System.out.println("Block "+b.id+" removed from group "+group1.id);
	}

	private void IgniteBlocks(List<Block> matched_blocks) {
		BlockGroup bg = new BlockGroup(launch_power);
		for (Block b : matched_blocks) {
			b.ignited = true;
			b.col = BlockColor.IGNITED;
			AddToBlockGroup(b,bg);
			b.onGround = false;
			b.yspd = bg.yvel;
			b.group = bg;
			for (Block bb : blocklist) {
				if (!matched_blocks.contains(bb) && 
						bb.isOnGround() && bb.xpos==b.xpos &&
						bb.ypos>b.ypos) {
					System.out.println("Block "+bb+" has been additionally found.");
					AddToBlockGroup(bb,bg);
					bb.onGround = false;
					bb.yspd =  bg.yvel;
					bb.group = bg;
				}
			}
		}
		grouplist.add(bg);
	}

	private void AddToBlockGroup(Block b, BlockGroup bg) {
		bg.addBlocks(b);
		b.group = bg;
		System.out.println("Block "+b+" added to Block Group "+bg.id+".");
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
				if (b.xpos == checkblock.xpos && 
						b.ypos+MeteosWar.BLOCK_SIZE > checkblock.ypos  &&
						b.ypos < checkblock.ypos+MeteosWar.BLOCK_SIZE
						/*&& b.isOnGround()*/) {
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
			debugfont.draw(batch, Integer.toString(b.id), b.xpos, b.ypos+12);
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
		
		/*for (int i=0;i<2;i++) {
			int baseX = MeteosWar.SCREEN_WIDTH/2 - ((field_width/2+1) * MeteosWar.BLOCK_SIZE)
					+ (0+1)*MeteosWar.BLOCK_SIZE;
			Block b = new Block(baseX,MeteosWar.SCREEN_HEIGHT-48+i*48,BlockColor.GetRandomColor(1),this);
			AddBlock(b);
			baseX = MeteosWar.SCREEN_WIDTH/2 - ((field_width/2+1) * MeteosWar.BLOCK_SIZE)
					+ (1+1)*MeteosWar.BLOCK_SIZE;
			b = new Block(baseX,MeteosWar.SCREEN_HEIGHT-48+i*48,BlockColor.GetRandomColor(1),this);
			AddBlock(b);
			baseX = MeteosWar.SCREEN_WIDTH/2 - ((field_width/2+1) * MeteosWar.BLOCK_SIZE)
					+ (2+1)*MeteosWar.BLOCK_SIZE;
			b = new Block(baseX,MeteosWar.SCREEN_HEIGHT-48+i*48,BlockColor.GetRandomColor(1),this);
			AddBlock(b);
			baseX = MeteosWar.SCREEN_WIDTH/2 - ((field_width/2+1) * MeteosWar.BLOCK_SIZE)
					+ (3+1)*MeteosWar.BLOCK_SIZE;
			b = new Block(baseX,MeteosWar.SCREEN_HEIGHT-48+i*48,BlockColor.GetRandomColor(1),this);
			AddBlock(b);
		}*/
		/*baseX = MeteosWar.SCREEN_WIDTH/2 - ((field_width/2+1) * MeteosWar.BLOCK_SIZE)
				+ (2+1)*MeteosWar.BLOCK_SIZE;
		b = new Block(baseX,MeteosWar.SCREEN_HEIGHT+64-48,BlockColor.GREEN,this);
		AddBlock(b);*/
	}
}
