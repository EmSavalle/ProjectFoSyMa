package eu.su.mas.dedaleEtu.mas.knowledge;

public class Treasure {
	public String position;
	public String type;
	public int value;
	public Long lastSeen;
	public boolean isOpen;
	public boolean isEmpty;
	public int lockStr;
	public int str;
	
	public Treasure(String p, String t, int v,int open, int lockStr,int str,Long time) {
		this.position = p;
		this.lastSeen = time;
		this.type = t;
		this.value = v;
		if(open == 1) {
			this.isOpen = true;
		}else {
			this.isOpen = false;
		}
		if(v == 0) {
			this.isEmpty = true;
		}else {
			this.isEmpty = false;
		}
		this.lockStr = lockStr;
		this.str = str;
	}
	public String getPosition() {return this.position;}
	public String getType() {return this.type;}
	public int getValue() {return this.value;}
	public boolean getOpen() {return this.isOpen;}
	public int getOpenInt() {if(this.isOpen) {return 1;}else {return 0;}}
	public boolean getEmpty() {return this.isEmpty;}
	public int getLockStrength() {return this.lockStr;}
	public Long getLastSeen() {return this.lastSeen;}
	public int getStrength() {return this.str;}
	public void setOpen() {
		this.isOpen = true;
	}
	public boolean updateValue(Long time, int v) {
		if(time < this.lastSeen) {
			return false;
		}else {
			this.value = v;
			this.lastSeen = time;
			return true;
		}
	}
	public boolean updateOpen(Long time, int lockisopen) {
		if(time < this.lastSeen) {
			return false;
		}else {
			this.isOpen = (lockisopen==1);
			this.lastSeen = time;
			return true;
		}
	}
	public boolean reduceValue(int v) {
		if(v == -1) {
			this.value = 0;
			return true;
		}else {
			this.value = this.value-v;
			if(v == 0) {
				this.isEmpty = true;
			}else {
				this.isEmpty = false;
			}
			return (this.value == 0);
		}
	}
}
