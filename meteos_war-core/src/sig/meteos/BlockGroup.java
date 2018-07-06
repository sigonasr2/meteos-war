package sig.meteos;

import java.util.ArrayList;
import java.util.List;

public class BlockGroup {
	List<Block> blocks = new ArrayList<Block>();
	float yvel;
	float weight;
	boolean landed=false;
	Planet planet;
	int id=0;
	
	public BlockGroup(float yvel) {
		this.yvel=yvel;
		this.id=Planet.GROUP_ID++;
	}
	
	public void setYvelocity(float yvel) {
		this.yvel = yvel;
	}
	
	public void setWeight(float weight) {
		this.weight=weight;
	}
	
	public void addBlocks(Block...bg) {
		for (Block b : bg) {
			blocks.add(b);
		}
	}
}
