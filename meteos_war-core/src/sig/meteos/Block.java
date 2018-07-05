package sig.meteos;

import java.lang.reflect.Field;
import java.util.List;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Block {
	BlockColor col;
	float xpos,ypos;
	float yspd=0;
	BlockGroup group=null;
	Planet planet;
	boolean onGround = false;
	boolean ignited=false;
	
	public Block(float xpos,float ypos, BlockColor col, Planet planet) {
		this.xpos = xpos;
		this.ypos = ypos;
		this.col = col;
		this.planet=planet;
	}

	public void draw(SpriteBatch batch) {
		batch.draw(
				(ignited) ? planet.block_tex[10]
				: planet.block_tex[col.getID()],xpos,ypos);
	}
	
	public boolean isOnGround() {
		return onGround;
	}
	
	public static Block BlockExists(List<Block> blocklist, float xpos, float ypos, BlockColor col, Block checkblock) {
		for (Block b : blocklist) {
			if (b!=checkblock && b.col == col && 
					b.xpos == xpos && b.ypos == ypos) {
				return b;
			}
		}
		return null;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.getClass().getName()+"(");
		boolean first=false;
		for (Field f : this.getClass().getDeclaredFields()) {
			if (!first) {
				try {
					sb.append(f.getName()+"="+f.get(this));
					first=true;
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			} else {
				try {
					sb.append(","+f.getName()+"="+f.get(this));
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
		sb.append(")");
		return sb.toString();
	}
}
