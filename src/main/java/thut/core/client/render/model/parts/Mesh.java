package thut.core.client.render.model.parts;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.Matrix3f;
import net.minecraft.client.renderer.Matrix4f;
import thut.api.maths.vecmath.Vector3f;
import thut.core.client.render.model.Vertex;
import thut.core.client.render.texturing.IPartTexturer;
import thut.core.client.render.texturing.TextureCoordinate;

public abstract class Mesh
{
    protected final boolean    hasTextures;
    public Vertex[]            vertices;
    public Vertex[]            normals;
    public TextureCoordinate[] textureCoordinates;
    public Integer[]           order;
    public int[]               rgbabro;
    Material                   material;
    public String              name;
    private final double[]     uvShift = { 0, 0 };
    final int                  GL_FORMAT;
    final Vertex[]             normalList;

    public Mesh(final Integer[] order, final Vertex[] vert, final Vertex[] norm, final TextureCoordinate[] tex,
            final int GL_FORMAT)
    {
        this.order = order;
        this.vertices = vert;
        this.normals = norm;
        this.textureCoordinates = tex;
        this.hasTextures = tex != null;
        this.GL_FORMAT = GL_FORMAT;
        this.normalList = new Vertex[this.order.length];

        Vertex vertex;
        Vertex normal;
        // Calculate the normals for each triangle.
        for (int i = 0; i < this.order.length; i += 3)
        {
            Vector3f v1, v2, v3;
            vertex = this.vertices[this.order[i]];
            v1 = new Vector3f(vertex.x, vertex.y, vertex.z);
            vertex = this.vertices[this.order[i + 1]];
            v2 = new Vector3f(vertex.x, vertex.y, vertex.z);
            vertex = this.vertices[this.order[i + 2]];
            v3 = new Vector3f(vertex.x, vertex.y, vertex.z);
            final Vector3f a = new Vector3f(v2);
            a.sub(v1);
            final Vector3f b = new Vector3f(v3);
            b.sub(v1);
            final Vector3f c = new Vector3f();
            c.cross(a, b);
            c.normalize();
            normal = new Vertex(c.x, c.y, c.z);
            if (Double.isNaN(normal.x))
            {
                normal.x = 0;
                normal.y = 0;
                normal.z = 1;
            }
            this.normalList[i] = normal;
            this.normalList[i + 1] = normal;
            this.normalList[i + 2] = normal;
        }

        // Initialize a "default" material for us
        this.material = new Material("auto:" + name);
    }

    protected void doRender(final MatrixStack mat, final IVertexBuilder buffer, final IPartTexturer texturer)
    {
        Vertex vertex;
        Vertex normal;

        TextureCoordinate textureCoordinate = new TextureCoordinate(0, 0);
        boolean flat = true;
        if (texturer != null) flat = texturer.isFlat(this.name);
        final int red = this.rgbabro[0];
        final int green = this.rgbabro[1];
        final int blue = this.rgbabro[2];
        final int alpha = this.rgbabro[3];
        final int lightmapUV = this.rgbabro[4];
        final int overlayUV = this.rgbabro[5];
        int n = 0;

        MatrixStack.Entry matrixstack$entry = mat.getLast();
        Matrix4f pos = matrixstack$entry.getPositionMatrix();
        Matrix3f norm = matrixstack$entry.getNormalMatrix();

        for (final Integer i : this.order)
        {
            if (this.hasTextures) textureCoordinate = this.textureCoordinates[i];
            vertex = this.vertices[i];
            normal = this.normals[i];
            if (flat) normal = this.normalList[n];

            float x = vertex.x;
            float y = vertex.y;
            float z = vertex.z;

            float nx = normal.x;
            float ny = normal.y;
            float nz = normal.z;

            float u = textureCoordinate.u + (float) uvShift[0];
            float v = textureCoordinate.v + (float) uvShift[1];

            // We use the default Item format, since that is what mobs use.
            // This means we need these in this order!
            buffer//@formatter:off
            .pos(pos, x, y, z)
            .color(red, green, blue, alpha)
            .tex(u, v)
            .overlay(overlayUV)
            .lightmap(lightmapUV)
            .normal(norm, nx, ny, nz)
            .endVertex();
            //@formatter:on
            n++;
        }
    }

    public void renderShape(final MatrixStack mat, IVertexBuilder buffer, final IPartTexturer texturer)
    {
        // Apply Texturing.
        if (texturer != null)
        {
            texturer.shiftUVs(this.name, this.uvShift);
        }
        if (this.material != null) buffer = this.material.preRender(mat, buffer);
        this.doRender(mat, buffer, texturer);
    }

    public void setMaterial(final Material material)
    {
        this.material = material;
        this.name = material.name;
    }
}
