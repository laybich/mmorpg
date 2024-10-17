package fontRendering;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import shaders.ShaderProgram;

public class FontShader extends ShaderProgram{

	private static final String VERTEX_FILE = "src/fontRendering/fontVertex.glsl";
	private static final String FRAGMENT_FILE = "src/fontRendering/fontFragment.glsl";

	private int location_colour;
	private int location_translation;
	private int location_width;
	private int location_edge;
	private int location_borderWidth;
	private int location_borderEdge;
	private int location_offset;
	private int location_outlineColour;

	public FontShader() {
		super(VERTEX_FILE, FRAGMENT_FILE);
	}

	@Override
	protected void getAllUniformLocations() {
		location_colour = super.getUniformLocation("colour");
		location_translation = super.getUniformLocation("translation");
		location_width = super.getUniformLocation("width");
		location_edge = super.getUniformLocation("edge");
		location_borderWidth = super.getUniformLocation("borderWidth");
		location_borderEdge = super.getUniformLocation("borderEdge");
		location_offset = super.getUniformLocation("offset");
		location_outlineColour = super.getUniformLocation("outlineColour");
	}

	@Override
	protected void bindAttributes() {
		super.bindAttribute(0, "position");
		super.bindAttribute(1, "textureCoords");
	}

	protected void loadDecor(float width, float edge, float borderWidth, float borderEdge,
									 Vector2f offset, Vector3f outlineColour) {
		super.loadFloat(location_width, width);
		super.loadFloat(location_edge, edge);
		super.loadFloat(location_borderWidth, borderWidth);
		super.loadFloat(location_borderEdge, borderEdge);
		super.load2DVector(location_offset, offset);
		super.loadVector(location_outlineColour, outlineColour);
	}

	protected void loadColour(Vector3f colour) {
		super.loadVector(location_colour, colour);
	}

	protected void loadTranslation(Vector2f translation) {
		super.load2DVector(location_translation, translation);
	}


}
