package catan.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;

public class UITools {
private static final float GRID_MIN = -30f;
private static final float GRID_MAX = 30f;
private static final float GRID_STEP = 1f;
private static final Model model;
private static final ModelInstance instance;

static {
	ModelBuilder builder = new ModelBuilder();
	builder.begin();
	MeshPartBuilder meshBuilder =
		builder.part("grid", GL20.GL_LINES, Usage.Position | Usage.ColorUnpacked, new Material());
	meshBuilder.setColor(Color.LIGHT_GRAY);
	for (float t = GRID_MIN; t <= GRID_MAX; t += GRID_STEP) {
	meshBuilder.line(t, 0, GRID_MIN, t, 0, GRID_MAX);
	meshBuilder.line(GRID_MIN, 0, t, GRID_MAX, 0, t);
	}
	meshBuilder =
		builder.part("axes", GL20.GL_LINES, Usage.Position | Usage.ColorUnpacked, new Material());
	meshBuilder.setColor(Color.RED);
	meshBuilder.line(0, 0, 0, 100, 0, 0);
	meshBuilder.setColor(Color.GREEN);
	meshBuilder.line(0, 0, 0, 0, 100, 0);
	meshBuilder.setColor(Color.BLUE);
	meshBuilder.line(0, 0, 0, 0, 0, 100);
	model = builder.end();
	instance = new ModelInstance(model);
}

protected static final ModelInstance getAxesInst() {
	return instance;
}
}
