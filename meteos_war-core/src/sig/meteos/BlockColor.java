package sig.meteos;

public enum BlockColor {
	BLUE(0),
	GREEN(1),
	PURPLE(2),
	RED(3),
	DARK_BLUE(4),
	YELLOW(5),
	ORANGE(6),
	PINK(7),
	DARK_GREEN(8),
	WHITE(9),
	IGNITED(10);
	
	int id;
	
	BlockColor(int id) {
		this.id=id;
	}
	
	public int getID() {
		return id;
	}
	
	public static BlockColor GetRandomColor(int maxVal) {
		int rand = MeteosWar.RANDOM.nextInt(maxVal);
		switch (rand%10) {
			case 0:{
				return BLUE;
			}
			case 1:{
				return GREEN;
			}
			case 2:{
				return PURPLE;
			}
			case 3:{
				return RED;
			}
			case 4:{
				return DARK_BLUE;
			}
			case 5:{
				return YELLOW;
			}
			case 6:{
				return ORANGE;
			}
			case 7:{
				return PINK;
			}
			case 8:{
				return DARK_GREEN;
			}
			case 9:{
				return WHITE;
			}
			default:
				System.out.println("Returning a default case for a random color that does not exist. This should NOT happen.");
				return BLUE;
		}
	}
}
