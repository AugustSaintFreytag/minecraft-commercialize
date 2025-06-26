package net.saint.commercialize.library;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

public class TextureReference {
	public final Identifier texture;
	public final int u;
	public final int v;
	public final int width;
	public final int height;

	public TextureReference(Identifier texture, int u, int v, int width, int height) {
		this.texture = texture;
		this.u = u;
		this.v = v;
		this.width = width;
		this.height = height;
	}

	public void draw(DrawContext context, int x, int y) {
		context.drawTexture(texture, x, y, u, v, width, height);
	}
}
