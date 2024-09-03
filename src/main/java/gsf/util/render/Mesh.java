package gsf.util.render;

public abstract class Mesh
{
	public static final Mesh NONE = new Mesh() {
		@Override
		public void draw() {
			// Pass.
		}
		
		@Override
		public void release() {
			// Pass.
		}
		
		@Override
		public String toString() {
			return "Mesh::NONE";
		}
	};
	
	
	protected Mesh() { }
	
	public abstract void draw();
	
	public abstract void release();
}
